import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
//    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.serialization)

    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.islamnotify"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.islamnotify"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())
        val apiKey = properties.getProperty("TIME_ZONE_API_KEY") ?: ""
        buildConfigField("String", "TIME_ZONE_API_KEY", "\"$apiKey\"")

    }


    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("debug")

            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    val composeBom = "2025.12.01"
    implementation(platform("androidx.compose:compose-bom:$composeBom"))
//    implementation("androidx.compose.foundation:foundation")

    implementation("org.maplibre.gl:android-sdk:12.3.1")
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.compose.material3)

    val lifecycleVersion = "2.9.4"
    val roomVersion = "2.8.3"
    val hiltVersion = "2.57.2"
    val workVersion = "2.11.0"
    val coroutinesVersion = "1.10.2"
    val retrofitVersion = "3.0.0"

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$coroutinesVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")

    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion") // coroutines support

    implementation("androidx.work:work-runtime-ktx:$workVersion")

    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")

    implementation("com.google.dagger:hilt-android:$hiltVersion")
    kapt("com.google.dagger:hilt-compiler:$hiltVersion")

    implementation("androidx.hilt:hilt-work:1.3.0")
    kapt("androidx.hilt:hilt-compiler:1.3.0")

    implementation(libs.play.services.location)
    implementation("com.google.maps.android:maps-compose:4.4.1")
    implementation("com.google.android.gms:play-services-maps:19.0.0")

    implementation(libs.batoul.adhan2)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation("androidx.datastore:datastore-preferences:1.1.7")

    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.appcompat:appcompat-resources:1.7.1")

    val media3Version = "1.2.1"
    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-session:$media3Version")
    implementation("androidx.media:media:1.7.0")

//    implementation(libs.androidx.navigation3.ui)
//    implementation(libs.androidx.navigation3.runtime)
//    implementation(libs.kotlinx.serialization.json)
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.9.0") // For serializing navigation keys
//    implementation("androidx.lifecycle:lifecycle-viewmodel-navigation3:1.0.0")

    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // If using Hilt for ViewModels
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
//    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}