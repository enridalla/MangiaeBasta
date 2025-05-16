plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("kotlinx-serialization")
}

android {
    namespace = "com.example.mangiaebasta"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mangiaebasta"
        minSdk = 34
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // --- Room (Database) ---
    val room_version = "2.7.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")               // Coroutines support
    annotationProcessor("androidx.room:room-compiler:$room_version")     // Java annotation processor
    // (Opzionale) RxJava / Guava / Paging
    implementation("androidx.room:room-rxjava2:$room_version")
    implementation("androidx.room:room-rxjava3:$room_version")
    implementation("androidx.room:room-guava:$room_version")
    implementation("androidx.room:room-paging:$room_version")
    testImplementation("androidx.room:room-testing:$room_version")      // Test helpers

    // --- DataStore (Preferences) ---
    implementation("androidx.datastore:datastore-preferences:1.1.6")
    implementation("androidx.datastore:datastore-preferences-rxjava2:1.1.6")
    implementation("androidx.datastore:datastore-preferences-rxjava3:1.1.6")

    // --- Core KTX & AndroidX utilities ---
    implementation("androidx.core:core-ktx:1.12.0")
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // --- Jetpack Compose UI ---
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.ui:ui:1.6.0")
    implementation("androidx.compose.foundation:foundation:1.6.0")
    implementation("androidx.compose.ui:ui-text:1.6.0")
    implementation(libs.androidx.ui)                // Compose UI tooling
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)   // Preview & inspection
    debugImplementation(libs.androidx.ui.test.manifest)

    // --- Material3 & Icons ---
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.material:material-icons-extended:1.4.3")

    // --- Navigation Compose ---
    implementation("androidx.navigation:navigation-compose:2.6.0")

    // --- Image Loading (Coil) ---
    implementation("io.coil-kt:coil-compose:2.4.0")

    // --- Ktor HTTP Client & Serialization ---
    implementation(libs.ktor.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.logging)
    implementation(libs.io.ktor.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json.v210)
    implementation(libs.kotlinx.serialization.json)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
}