plugins {
    id("com.android.application")

    id("com.google.firebase.appdistribution")
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")

    id("dagger.hilt.android.plugin")

    kotlin("android")
    kotlin("kapt")
}

android {
    compileSdkVersion(30)
    buildToolsVersion("30.0.3")

    defaultConfig {
        applicationId = "com.vikingelectronics.softphone"
        minSdkVersion(24)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled(true)
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
//        getByName("qa") {
//            minifyEnabled(true)
//            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
//        }
        debug { 
            minifyEnabled(false)
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    kapt {
        correctErrorTypes = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("com.google.android.material:material:1.2.1")

    implementation("com.google.dagger:hilt-android:2.31.2-alpha")
    kapt("com.google.dagger:hilt-android-compiler:2.31.2-alpha")

    implementation(platform("com.google.firebase:firebase-bom:26.4.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    // For instrumentation tests
//    androidTestImplementation  'com.google.dagger:hilt-android-testing:<VERSION>'
//    androidTestAnnotationProcessor 'com.google.dagger:hilt-android-compiler:<VERSION>'
//
//    // For local unit tests
//    testImplementation 'com.google.dagger:hilt-android-testing:<VERSION>'
//    testAnnotationProcessor 'com.google.dagger:hilt-android-compiler:<VERSION>'


    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}