plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.trinity"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.trinity"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "2.4.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    //o que eu coloquei
    buildFeatures{
        viewBinding = true
    }
//    viewBinding{
//        enable = true
//
//    }
    dataBinding{
        enable = true
    }

}

dependencies {

    implementation(files("C:\\Users\\TeresaShySmile\\Downloads\\subsampling-scale-image-view-3.10.0.aar"))
    implementation(files("C:/Users/TeresaShySmile/Downloads/jsoup-1.18.1.jar"))
    val work_version = "2.9.0"
    // (Java only)
    implementation("androidx.work:work-runtime:$work_version")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.gson)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("androidx.webkit:webkit:1.9.0")
//    implementation ("com.github.chrisbanes:PhotoView:2.3.0")
}