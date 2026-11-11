import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

// Release signing comes from keystore.properties (never committed). See README.
val keystoreProperties = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

val prodApiBaseUrl = "https://spawn-app-back-end-production.up.railway.app/api/v1/"

// Google sign-in: the *Web* OAuth client ID from the same Google Cloud project as the backend.
val webClientId: String = localProperties.getProperty("WEB_CLIENT_ID")
    ?: System.getenv("SPAWN_WEB_CLIENT_ID") ?: ""

android {
    namespace = "com.danielagapov.spawn"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.danielagapov.spawn"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "WEB_CLIENT_ID", "\"$webClientId\"")
        buildConfigField("String", "SHARE_BASE_URL", "\"https://getspawn.com\"")
        buildConfigField(
            "String", "PRIVACY_POLICY_URL",
            "\"https://doc-hosting.flycricket.io/spawn-privacy-policy/8f254bc3-3403-4928-8353-f1f787ed6eec/privacy\""
        )
    }

    signingConfigs {
        create("release") {
            if (keystoreProperties.containsKey("storeFile")) {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // Point debug builds at a local backend by setting API_BASE_URL in local.properties,
            // e.g. API_BASE_URL=http://10.0.2.2:8080/api/v1/ (10.0.2.2 is the host machine from the emulator).
            val debugApi = localProperties.getProperty("API_BASE_URL") ?: prodApiBaseUrl
            buildConfigField("String", "API_BASE_URL", "\"$debugApi\"")
        }
        release {
            buildConfigField("String", "API_BASE_URL", "\"$prodApiBaseUrl\"")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystoreProperties.containsKey("storeFile")) {
                signingConfig = signingConfigs.getByName("release")
            }
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Networking
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Images
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Secure token storage
    implementation(libs.androidx.security.crypto)

    // Sign in with Google
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.google.id)

    // Maps + location
    implementation(libs.mapbox.maps)
    implementation(libs.mapbox.maps.compose)
    implementation(libs.mapbox.search.discover)
    implementation(libs.play.services.location)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
