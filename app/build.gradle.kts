plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.amkj.appreservascab"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.amkj.appreservascab"
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
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures{
        viewBinding = true

    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
//    recycle views
    implementation(libs.androidx.recyclerview)
// //    recycler view seleccionable
//    implementation("androidx.recyclerview:recyclerview-selection:1.2.0")

    // retrofit
<<<<<<< HEAD
//    implementation(libs.retrofit)
//    implementation (libs.converter.gson)

=======
    implementation(libs.retrofit)
    implementation (libs.converter.gson)
//    implementation("com.android.volley:volley:1.2.1")
>>>>>>> cd4810b805f411e97cae0baa3b90201d8a46e73d
    implementation(libs.androidx.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)



        implementation ("com.squareup.okhttp3:logging-interceptor:4.10.0") // Usa una versión compatible

//    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")


}