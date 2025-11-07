plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.youcanrun.app"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.youcanrun"
        minSdk = 24
        targetSdk = 36
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Include project modules
    implementation(project(":core"))
    implementation(project(":ar"))
    implementation(project(":audio"))
    implementation(project(":ui"))
    implementation(project(":sensors"))
    implementation(project(":utils"))
}