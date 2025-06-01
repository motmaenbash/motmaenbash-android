plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    alias(libs.plugins.compose.compiler)
    id("kotlin-parcelize")

}

android {
    namespace = "nu.milad.motmaenbash"
    compileSdk = 36

    defaultConfig {
        applicationId = "nu.milad.motmaenbash"
        minSdk = 21
        targetSdk = 36
        versionCode = 6
        versionName = "1.0.2-beta"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

    }

    androidResources {
        localeFilters += listOf("en", "fa")
    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true

    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
        debug {
//            applicationIdSuffix = ".debug"
       
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8

    }


    kotlinOptions {
        jvmTarget = "1.8"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {


    implementation(libs.material)

    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.datastore.core.android)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.work.runtime.ktx)


    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.inappmessaging.display)
    //
    releaseImplementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)

    // Android Studio Preview support
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.tooling.preview)

    //Compose
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    // Compose dependencies
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.ui)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    //Permissions for Jetpack Compose
    implementation(libs.accompanist.permissions)

    // Coil for Jetpack Compose
    implementation(libs.coil.compose)

}