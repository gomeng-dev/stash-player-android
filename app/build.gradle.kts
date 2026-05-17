import java.util.Base64
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.isFile) {
        file.inputStream().use(::load)
    }
}

fun localProperty(vararg names: String): String? = names.asSequence()
    .mapNotNull { name -> localProperties.getProperty(name)?.trim()?.takeIf { it.isNotBlank() } }
    .firstOrNull()

fun environmentVariable(name: String): String? = providers.environmentVariable(name).orNull
    ?.trim()
    ?.takeIf { it.isNotBlank() }

val releaseSigningKeystoreBase64 = environmentVariable("ANDROID_SIGNING_KEYSTORE_BASE64")
val releaseSigningStorePassword = environmentVariable("ANDROID_SIGNING_STORE_PASSWORD")
    ?: localProperty("ANDROID_SIGNING_STORE_PASSWORD", "android.signing.storePassword", "storePassword")
val releaseSigningKeyAlias = environmentVariable("ANDROID_SIGNING_KEY_ALIAS")
    ?: localProperty("ANDROID_SIGNING_KEY_ALIAS", "android.signing.keyAlias", "keyAlias")
val releaseSigningKeyPassword = environmentVariable("ANDROID_SIGNING_KEY_PASSWORD")
    ?: localProperty("ANDROID_SIGNING_KEY_PASSWORD", "android.signing.keyPassword", "keyPassword")
val localReleaseSigningKeystoreFile = localProperty("ANDROID_SIGNING_KEYSTORE_FILE", "android.signing.storeFile", "storeFile")
    ?.let { rootProject.file(it) }
    ?: rootProject.file("stash-player-release.jks")
val generatedReleaseSigningKeystoreFile = layout.buildDirectory.file("signing/release-keystore.jks").get().asFile
val releaseSigningKeystoreFile = if (releaseSigningKeystoreBase64 != null) {
    generatedReleaseSigningKeystoreFile
} else {
    localReleaseSigningKeystoreFile
}
val hasReleaseSigningKeystore = releaseSigningKeystoreBase64 != null || localReleaseSigningKeystoreFile.isFile
val hasReleaseSigningConfig = hasReleaseSigningKeystore &&
    listOf(
        releaseSigningStorePassword,
        releaseSigningKeyAlias,
        releaseSigningKeyPassword,
    ).all { !it.isNullOrBlank() }

if (releaseSigningKeystoreBase64 != null) {
    generatedReleaseSigningKeystoreFile.parentFile.mkdirs()
    generatedReleaseSigningKeystoreFile.writeBytes(Base64.getDecoder().decode(releaseSigningKeystoreBase64))
}

android {
    namespace = "gomeng.dev.stashplayer"
    compileSdk = 35

    defaultConfig {
        applicationId = "gomeng.dev.stashplayer"
        minSdk = 29
        targetSdk = 35
        versionCode = 28
        versionName = "1.7.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    signingConfigs {
        if (hasReleaseSigningConfig) {
            create("release") {
                storeFile = releaseSigningKeystoreFile
                storePassword = releaseSigningStorePassword
                keyAlias = releaseSigningKeyAlias
                keyPassword = releaseSigningKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        release {
            if (hasReleaseSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")

    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.4.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")
    implementation("androidx.media3:media3-session:1.4.1")

    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-gif:2.7.0")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
