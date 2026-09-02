import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.vastavik.computer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vastavik.computer"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        multiDexEnabled = true
        vectorDrawables {
            useSupportLibrary = true
        }
        
        val properties = Properties()
        // Safe load: local.properties may be missing in CI
        val localPropsFile = project.rootProject.file("local.properties")
        if (localPropsFile.exists()) {
            properties.load(localPropsFile.inputStream())
        }
        buildConfigField("String", "GEMINI_API_KEY", "\"${properties.getProperty("GEMINI_API_KEY", "")}\"")
        buildConfigField("String", "MISTRAL_API_KEY", "\"${properties.getProperty("MISTRAL_API_KEY", "")}\"")
        buildConfigField("Boolean", "SECURITY_CHECK_ENABLED", "false")
        // Backend connection — override in local.properties if needed
        // Prod (Render): https://vastavik-admin-backend.onrender.com  |  Emulator: http://10.0.2.2:3001  |  LAN: http://192.168.x.x:3001
        buildConfigField("String", "BACKEND_BASE_URL", "\"${properties.getProperty("BACKEND_BASE_URL", "https://vastavik-admin-backend.onrender.com")}\"")
        buildConfigField("String", "API_KEY_ID", "\"${properties.getProperty("API_KEY_ID", "android-prod")}\"")
        buildConfigField("String", "API_KEY_SECRET", "\"${properties.getProperty("API_KEY_SECRET", "dev-secret-android-32bytes-hex-0000")}\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file("keystore.jks")
            storePassword = ""
            keyAlias = ""
            keyPassword = ""
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.animation:animation")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.52")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    kapt("com.google.dagger:hilt-compiler:2.52")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.android.gms:play-services-auth:21.3.0")

    // Coil Image Loading
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // YouTube Player
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.1")

    // Google Generative AI
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // Backend API — Retrofit + OkHttp + Serialization
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")

    // Lottie
    implementation("com.airbnb.android:lottie-compose:6.6.2")

    // ML Kit Text Recognition
    implementation("com.google.mlkit:text-recognition:16.0.0")

    // CameraX for in-app camera capture
    val cameraXVersion = "1.4.1"
    implementation("androidx.camera:camera-core:$cameraXVersion")
    implementation("androidx.camera:camera-camera2:$cameraXVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraXVersion")
    implementation("androidx.camera:camera-view:$cameraXVersion")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}

kapt {
    correctErrorTypes = true
}