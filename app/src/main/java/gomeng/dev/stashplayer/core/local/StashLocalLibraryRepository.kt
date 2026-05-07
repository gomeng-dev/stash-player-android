package gomeng.dev.stashplayer.core.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import gomeng.dev.stashplayer.core.model.SceneCardModel
import gomeng.dev.stashplayer.core.model.StashPersistedBrowseFilterState
import gomeng.dev.stashplayer.core.model.StashPersistedSearchFilterState
import gomeng.dev.stashplayer.core.model.StashSavedFilterRef
import gomeng.dev.stashplayer.core.model.StashVideoFilterState
import gomeng.dev.stashplayer.core.model.deserializeStashPersistedBrowseFilterState
import gomeng.dev.stashplayer.core.model.deserializeStashPersistedSearchFilterState
import gomeng.dev.stashplayer.core.model.deserializeStashVideoFilterState
import gomeng.dev.stashplayer.core.model.normalizeStashVideoFilterText
import gomeng.dev.stashplayer.core.model.promoteStashRecentVideoFilter
import gomeng.dev.stashplayer.core.model.toSavedFilterPayload
import gomeng.dev.stashplayer.core.model.withSavedFilterReference
import gomeng.dev.stashplayer.core.model.withGeneratedStashRandomShuffleSeedIfNeeded
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

private val LOCAL_DATABASE_NAME = "stash-local-library.db"
private val LOCAL_RECENT_FILTER_LIMIT = 5

private val Context.stashLocalFilterDataStore by preferencesDataStore(name = "stash_local_filter_state")

enum class LocalSceneListType(val id: String, val label: String) {
    Queue("queue", stashString(R.string.auto_kr_0004)),
    WatchLater("watch_later", stashString(R.string.auto_kr_0016)),
    PlaybackHistory("playback_history", "최근 재생 기록"),
}

data class LocalSavedVideoFilter(
    val id: String,
    val name: String,
    val filterState: StashVideoFilterState,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "local_favorite_scenes")
data class LocalFavoriteSceneEntity(
    @PrimaryKey val sceneId: String,
    val title: String,
    val studio: String,
    val durationText: String,
    val thumbnailUrl: String?,
    val progress: Float,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "local_scene_list_items", primaryKeys = ["listType", "sceneId"])
data class LocalSceneListItemEntity(
    val listType: String,
    val sceneId: String,
    val title: String,
    val studio: String,
    val durationText: String,
    val thumbnailUrl: String?,
    val progress: Float,
    val position: Long,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "local_saved_video_filters")
data class LocalSavedVideoFilterEntity(
    @PrimaryKey val id: String,
    val name: String,
    val serializedFilter: String,
    val createdAt: Long,
    val updatedAt: Long,
)

@Dao
interface StashLocalLibraryDao {
    @Query("SELECT sceneId FROM local_favorite_scenes ORDER BY updatedAt DESC")
    fun observeFavoriteSceneIds(): Flow<List<String>>

    @Query("SELECT * FROM local_favorite_scenes ORDER BY updatedAt DESC")
    fun observeFavoriteScenes(): Flow<List<LocalFavoriteSceneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFavoriteScene(scene: LocalFavoriteSceneEntity)

    @Query("DELETE FROM local_favorite_scenes WHERE sceneId = :sceneId")
    suspend fun deleteFavoriteScene(sceneId: String)

    @Query("DELETE FROM local_favorite_scenes WHERE sceneId IN (:sceneIds)")
    suspend fun deleteFavoriteScenes(sceneIds: List<String>)

    @Query("SELECT sceneId FROM local_scene_list_items WHERE listType = :listType ORDER BY position DESC, updatedAt DESC")
    fun observeSceneListIds(listType: String): Flow<List<String>>

    @Query("SELECT * FROM local_scene_list_items WHERE listType = :listType ORDER BY position DESC, updatedAt DESC")
    fun observeSceneListItems(listType: String): Flow<List<LocalSceneListItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSceneListItem(item: LocalSceneListItemEntity)

    @Query("DELETE FROM local_scene_list_items WHERE listType = :listType AND sceneId = :sceneId")
    suspend fun deleteSceneListItem(listType: String, sceneId: String)

    @Query("DELETE FROM local_scene_list_items WHERE sceneId IN (:sceneIds)")
    suspend fun deleteSceneListItemsForScenes(sceneIds: List<String>)

    @Query("DELETE FROM local_scene_list_items WHERE listType = :listType")
    suspend fun clearSceneList(listType: String)

    @Query("DELETE FROM local_scene_list_items WHERE listType = :listType AND sceneId NOT IN (SELECT sceneId FROM local_scene_list_items WHERE listType = :listType ORDER BY position DESC, updatedAt DESC LIMIT :limit)")
    suspend fun trimSceneListToLimit(listType: String, limit: Int)

    @Query("SELECT COUNT(*) FROM local_scene_list_items WHERE listType = :listType")
    suspend fun sceneListCount(listType: String): Int

    @Query("SELECT * FROM local_saved_video_filters ORDER BY updatedAt DESC")
    fun observeSavedVideoFilters(): Flow<List<LocalSavedVideoFilterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSavedVideoFilter(filter: LocalSavedVideoFilterEntity)

    @Query("DELETE FROM local_saved_video_filters WHERE id = :id")
    suspend fun deleteSavedVideoFilter(id: String)
}

@Database(
    entities = [
        LocalFavoriteSceneEntity::class,
        LocalSceneListItemEntity::class,
        LocalSavedVideoFilterEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class StashLocalDatabase : RoomDatabase() {
    abstract fun localLibraryDao(): StashLocalLibraryDao

    companion object {
        @Volatile
        private var instance: StashLocalDatabase? = null

        fun get(context: Context): StashLocalDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                StashLocalDatabase::class.java,
                LOCAL_DATABASE_NAME,
            ).build().also { instance = it }
        }
    }
}

class StashLocalLibraryRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dao = StashLocalDatabase.get(appContext).localLibraryDao()

    val favoriteSceneIds: Flow<Set<String>> = dao.observeFavoriteSceneIds().map { it.toSet() }
    val favoriteScenes: Flow<List<SceneCardModel>> = dao.observeFavoriteScenes().map { scenes ->
        scenes.map { it.toSceneCardModel(isInWatchLater = false) }
    }
    val watchLaterSceneIds: Flow<Set<String>> = dao.observeSceneListIds(LocalSceneListType.WatchLater.id).map { it.toSet() }
    val queueSceneIds: Flow<Set<String>> = dao.observeSceneListIds(LocalSceneListType.Queue.id).map { it.toSet() }
    val queueScenes: Flow<List<SceneCardModel>> = dao.observeSceneListItems(LocalSceneListType.Queue.id).map { items ->
        items.map { it.toSceneCardModel(isInWatchLater = false) }
    }
    val watchLaterScenes: Flow<List<SceneCardModel>> = dao.observeSceneListItems(LocalSceneListType.WatchLater.id).map { items ->
        items.map { it.toSceneCardModel(isInWatchLater = true) }
    }
    val playbackHistoryScenes: Flow<List<SceneCardModel>> = dao.observeSceneListItems(LocalSceneListType.PlaybackHistory.id).map { items ->
        items.map { it.toSceneCardModel(isInWatchLater = false) }
    }
    val savedVideoFilters: Flow<List<LocalSavedVideoFilter>> = dao.observeSavedVideoFilters().map { filters ->
        filters.map { it.toModel() }
    }
    val persistedBrowseFilterState: Flow<StashPersistedBrowseFilterState?> = appContext.stashLocalFilterDataStore.data.map { prefs ->
        deserializeStashPersistedBrowseFilterState(prefs[LocalFilterKeys.Browse].orEmpty())
    }
    val persistedSearchFilterState: Flow<StashPersistedSearchFilterState?> = appContext.stashLocalFilterDataStore.data.map { prefs ->
        deserializeStashPersistedSearchFilterState(prefs[LocalFilterKeys.Search].orEmpty())
    }
    val recentBrowseVideoFilters: Flow<List<StashVideoFilterState>> = appContext.stashLocalFilterDataStore.data.map { prefs ->
        prefs[LocalFilterKeys.RecentBrowse]
            .orEmpty()
            .parseRecentFilters()
    }
    val recentSearchVideoFilters: Flow<List<StashVideoFilterState>> = appContext.stashLocalFilterDataStore.data.map { prefs ->
        prefs[LocalFilterKeys.RecentSearch]
            .orEmpty()
            .parseRecentFilters()
    }

    suspend fun setFavorite(scene: SceneCardModel, isFavorite: Boolean, nowMillis: Long = System.currentTimeMillis()) {
        if (isFavorite) {
            dao.upsertFavoriteScene(scene.toFavoriteEntity(nowMillis))
        } else {
            dao.deleteFavoriteScene(scene.id)
        }
    }

    suspend fun setWatchLater(scene: SceneCardModel, isInWatchLater: Boolean, nowMillis: Long = System.currentTimeMillis()) {
        if (isInWatchLater) {
            dao.upsertSceneListItem(scene.toListItemEntity(LocalSceneListType.WatchLater, nowMillis))
        } else {
            dao.deleteSceneListItem(LocalSceneListType.WatchLater.id, scene.id)
        }
    }

    suspend fun addToQueue(scene: SceneCardModel, nowMillis: Long = System.currentTimeMillis()) {
        dao.upsertSceneListItem(scene.toListItemEntity(LocalSceneListType.Queue, nowMillis))
    }

    suspend fun recordPlaybackHistory(scene: SceneCardModel, nowMillis: Long = System.currentTimeMillis()) {
        dao.upsertSceneListItem(scene.toListItemEntity(LocalSceneListType.PlaybackHistory, nowMillis))
        dao.trimSceneListToLimit(LocalSceneListType.PlaybackHistory.id, localPlaybackHistoryDisplayLimit())
    }

    suspend fun addAllToQueue(scenes: List<SceneCardModel>, nowMillis: Long = System.currentTimeMillis()) {
        scenes.distinctBy { it.id }.forEachIndexed { index, scene ->
            dao.upsertSceneListItem(scene.toListItemEntity(LocalSceneListType.Queue, nowMillis - index))
        }
    }

    suspend fun removeFromQueue(sceneId: String) {
        dao.deleteSceneListItem(LocalSceneListType.Queue.id, sceneId)
    }

    suspend fun removeFromWatchLater(sceneId: String) {
        dao.deleteSceneListItem(LocalSceneListType.WatchLater.id, sceneId)
    }

    suspend fun clearQueue() {
        dao.clearSceneList(LocalSceneListType.Queue.id)
    }

    suspend fun queueSceneCount(): Int = dao.sceneListCount(LocalSceneListType.Queue.id)

    suspend fun restoreQueue(scenes: List<SceneCardModel>, nowMillis: Long = System.currentTimeMillis()) {
        scenes.distinctBy { it.id }.forEachIndexed { index, scene ->
            dao.upsertSceneListItem(scene.toListItemEntity(LocalSceneListType.Queue, nowMillis - index))
        }
    }

    suspend fun removeScenesFromLocalSnapshots(sceneIds: Collection<String>) {
        val ids = sceneIds.distinct()
        if (ids.isEmpty()) return
        dao.deleteFavoriteScenes(ids)
        dao.deleteSceneListItemsForScenes(ids)
    }

    suspend fun saveVideoFilter(
        name: String,
        filterState: StashVideoFilterState,
        nowMillis: Long = System.currentTimeMillis(),
        overwriteExisting: Boolean = true,
    ): LocalSavedVideoFilter {
        val normalizedName = normalizeStashVideoFilterText(name).ifBlank { stashString(R.string.auto_kr_0017) }
        val id = buildLocalSavedVideoFilterIdForSave(
            name = normalizedName,
            nowMillis = nowMillis,
            savedFilter = filterState.savedFilter,
            overwriteExisting = overwriteExisting,
        )
        val payload = filterState.toSavedFilterPayload().serializeForStorage()
        val entity = LocalSavedVideoFilterEntity(
            id = id,
            name = normalizedName,
            serializedFilter = payload,
            createdAt = nowMillis,
            updatedAt = nowMillis,
        )
        dao.upsertSavedVideoFilter(entity)
        return entity.toModel()
    }

    suspend fun deleteSavedVideoFilter(id: String) {
        dao.deleteSavedVideoFilter(id)
    }

    suspend fun saveBrowseFilterState(state: StashPersistedBrowseFilterState) {
        appContext.stashLocalFilterDataStore.edit { prefs ->
            prefs[LocalFilterKeys.Browse] = state.serializeForStorage()
        }
    }

    suspend fun saveSearchFilterState(state: StashPersistedSearchFilterState) {
        appContext.stashLocalFilterDataStore.edit { prefs ->
            prefs[LocalFilterKeys.Search] = state.serializeForStorage()
        }
    }

    suspend fun saveRecentBrowseVideoFilter(filterState: StashVideoFilterState) {
        saveRecentFilter(LocalFilterKeys.RecentBrowse, filterState)
    }

    suspend fun saveRecentSearchVideoFilter(filterState: StashVideoFilterState) {
        saveRecentFilter(LocalFilterKeys.RecentSearch, filterState)
    }

    private suspend fun saveRecentFilter(
        key: androidx.datastore.preferences.core.Preferences.Key<String>,
        filterState: StashVideoFilterState,
    ) {
        val recentSnapshot = filterState
            .copy(savedFilter = null)
            .withGeneratedStashRandomShuffleSeedIfNeeded()
        appContext.stashLocalFilterDataStore.edit { prefs ->
            val updated = promoteStashRecentVideoFilter(
                existing = prefs[key].orEmpty().parseRecentFilters(),
                candidate = recentSnapshot,
                limit = LOCAL_RECENT_FILTER_LIMIT,
            )
            prefs[key] = updated.joinToString("\n") { it.serializeForStorage() }
        }
    }

    private object LocalFilterKeys {
        val Browse = stringPreferencesKey("browse_filter_state")
        val Search = stringPreferencesKey("search_filter_state")
        val RecentBrowse = stringPreferencesKey("recent_browse_filter_state")
        val RecentSearch = stringPreferencesKey("recent_search_filter_state")
    }
}

private fun String.parseRecentFilters(): List<StashVideoFilterState> = lineSequence()
    .map { it.trim() }
    .filter { it.isNotBlank() }
    .map(::deserializeStashVideoFilterState)
    .toList()

fun buildLocalSavedVideoFilterId(name: String, nowMillis: Long): String {
    val slug = normalizeStashVideoFilterText(name)
        .lowercase()
        .replace(Regex(stashString(R.string.auto_kr_0018)), "-")
        .trim('-')
        .ifBlank { "filter" }
        .take(40)
    return "local-$nowMillis-$slug"
}

fun buildLocalSavedVideoFilterIdForSave(
    name: String,
    nowMillis: Long,
    savedFilter: StashSavedFilterRef?,
    overwriteExisting: Boolean,
): String = if (overwriteExisting && savedFilter != null) {
    savedFilter.id
} else {
    buildLocalSavedVideoFilterId(name, nowMillis)
}

fun LocalSavedVideoFilter.appliedFilterState(): StashVideoFilterState = filterState
    .withSavedFilterReference(StashSavedFilterRef(id = id, name = name))
    .withGeneratedStashRandomShuffleSeedIfNeeded()

private fun LocalSavedVideoFilterEntity.toModel(): LocalSavedVideoFilter = LocalSavedVideoFilter(
    id = id,
    name = name,
    filterState = deserializeStashVideoFilterState(serializedFilter),
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun SceneCardModel.toFavoriteEntity(nowMillis: Long): LocalFavoriteSceneEntity = LocalFavoriteSceneEntity(
    sceneId = id,
    title = title,
    studio = studio,
    durationText = durationText,
    thumbnailUrl = thumbnailUrl.scrubLocalCredentialQueryParameters(),
    progress = progress,
    createdAt = nowMillis,
    updatedAt = nowMillis,
)

private fun SceneCardModel.toListItemEntity(type: LocalSceneListType, nowMillis: Long): LocalSceneListItemEntity = LocalSceneListItemEntity(
    listType = type.id,
    sceneId = id,
    title = title,
    studio = studio,
    durationText = durationText,
    thumbnailUrl = thumbnailUrl.scrubLocalCredentialQueryParameters(),
    progress = progress,
    position = nowMillis,
    createdAt = nowMillis,
    updatedAt = nowMillis,
)

private fun LocalFavoriteSceneEntity.toSceneCardModel(isInWatchLater: Boolean): SceneCardModel = SceneCardModel(
    id = sceneId,
    title = title,
    durationText = durationText,
    studio = studio,
    progress = progress,
    isInWatchLater = isInWatchLater,
    thumbnailUrl = thumbnailUrl,
)

private fun LocalSceneListItemEntity.toSceneCardModel(isInWatchLater: Boolean): SceneCardModel = SceneCardModel(
    id = sceneId,
    title = title,
    durationText = durationText,
    studio = studio,
    progress = progress,
    isInWatchLater = isInWatchLater,
    thumbnailUrl = thumbnailUrl,
)

fun List<SceneCardModel>.applyLocalFavoriteFilter(
    favoriteOnly: Boolean,
    favoriteSceneIds: Set<String>,
): List<SceneCardModel> = if (favoriteOnly) {
    filter { it.id in favoriteSceneIds }
} else {
    this
}

fun String?.scrubLocalCredentialQueryParameters(): String? = this?.replace(
    Regex("([?&])apikey=[^&#]*", RegexOption.IGNORE_CASE),
    "$1",
)?.replace("&&", "&")
    ?.replace("?&", "?")
    ?.replace(Regex("[?&](#|$)"), "$1")
