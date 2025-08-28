plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id ("kotlin-parcelize")
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

//    implementation(libs.retrofit)
//    implementation (libs.converter.gson)
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation ("androidx.recyclerview:recyclerview:1.3.0")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
//    implementation("com.android.volley:volley:1.2.1")

    implementation(libs.androidx.runtime)
    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.androidx.core.i18n)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // gilde para imagenes
    implementation ("com.github.bumptech.glide:glide:4.16.0")
//
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
//
    implementation ("com.squareup.okhttp3:logging-interceptor:4.10.0") // Usa una versión compatible

    implementation ("com.kizitonwose.calendar:view:2.5.4")
//    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")


}