plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.agribot"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.agribot"
        minSdk = 24
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Google Play Services - Location API
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // OkHttp (for low-level network requests)
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    // JSON Parsing
    implementation("com.google.code.gson:gson:2.8.9")

    // Volley (for easy API requests)
    implementation("com.android.volley:volley:1.2.1")

    implementation ("com.squareup.picasso:picasso:2.8") // For weather icons

    implementation ("androidx.work:work-runtime:2.8.1")


    // AndroidX Core Libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.translate)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
