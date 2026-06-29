// Top-level build file where you can add configuration options common to all subprojects/modules.
buildscript {
    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.57.2")
        classpath ("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
    }
}


plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
//    alias(libs.plugins.kotlin.serialization) apply false
    id("com.google.dagger.hilt.android") version "2.57.2" apply false
    alias(libs.plugins.google.devtools.ksp) apply false

}