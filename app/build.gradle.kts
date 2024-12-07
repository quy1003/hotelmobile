plugins {
//    id("com.android.application")
    id("com.google.gms.google-services")

    alias(libs.plugins.androidApplication)

}

android {
    namespace = "com.example.hotelmobile"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.hotelmobile"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    //Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database")
    implementation ("com.google.firebase:firebase-storage:20.2.1")


    //Cloudinary
    implementation ("com.cloudinary:cloudinary-android:3.0.2")
    implementation ("com.cloudinary:cloudinary-android-preprocess:3.0.2")
    //Picasso
    implementation ("com.squareup.picasso:picasso:2.71828")
    //Gson
    implementation ("com.google.code.gson:gson:2.8.8")
    //Glide
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    //Zalopay
    implementation(fileTree(mapOf(
        "dir" to "C:\\hotelmobile/ZaloPayLib",
        "include" to listOf("*.aar", "*.jar"),
        "exclude" to listOf("")
    )))
    implementation("com.squareup.okhttp3:okhttp:4.6.0")
    implementation("commons-codec:commons-codec:1.14")
    //DatePicker
    implementation ("com.google.android.material:material:1.9.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}