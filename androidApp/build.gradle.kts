plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    id("io.gitlab.arturbosch.detekt").version("1.22.0")
    id("org.jlleitschuh.gradle.ktlint").version("11.3.2")
}

android {
    namespace = "com.donxux.ppong.android"
    compileSdk = 33
    defaultConfig {
        applicationId = "com.donxux.ppong.android"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    val composeUiVersion = "1.3.3"
    val composeFoundationVersion = "1.3.1"
    val composeMaterial3Version = "1.0.1"
    val cameraxVersion = "1.2.1"
    val accompanistPermissionsVersion = "0.23.1"
    val hiltVersion = "2.45"
    val materialIconVersion = "1.4.1"

    implementation(project(":shared"))
    implementation("androidx.compose.ui:ui:$composeUiVersion")
    implementation("androidx.compose.ui:ui-tooling:$composeUiVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeUiVersion")
    implementation("androidx.compose.foundation:foundation:$composeFoundationVersion")
    implementation("androidx.compose.material3:material3:$composeMaterial3Version")
    implementation("androidx.compose.material:material-icons-core:$materialIconVersion")
    implementation("androidx.compose.material:material-icons-extended:$materialIconVersion")
    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-video:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-extensions:$cameraxVersion")
    implementation("com.google.accompanist:accompanist-permissions:$accompanistPermissionsVersion")
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    kapt("com.google.dagger:hilt-android-compiler:$hiltVersion")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.20")
    implementation("com.squareup:javapoet:1.13.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation("androidx.activity:activity-compose:1.7.0")
}