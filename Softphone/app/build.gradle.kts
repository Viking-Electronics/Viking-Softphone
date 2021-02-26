import java.net.URI

plugins {
    id("com.android.application")

    id("com.google.firebase.appdistribution")
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")

    id("dagger.hilt.android.plugin")

    kotlin("android")
    kotlin("kapt")
}

repositories {
    maven {
        // Replace snapshots by releases for releases !
        url = URI("https://linphone.org/releases/maven_repository")
    }
}

android {
    compileSdkVersion(30)
    buildToolsVersion("30.0.3")

    defaultConfig {
        applicationId = "com.vikingelectronics.softphone"
        minSdkVersion(24)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "0.1"

        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        compose = true
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
            ext.set("enableCrashlytics", false)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.0.0-beta01"
    }

    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
        freeCompilerArgs += "-Xallow-jvm-ir-dependencies"
    }
    kapt {
        correctErrorTypes = true
    }
    packagingOptions {
        exclude("META-INF/*")
    }
}

dependencies {

    implementation(project(":legacy"))

    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("androidx.core:core-ktx:1.3.2")

    implementation("androidx.activity:activity-compose:1.3.0-alpha02")
    implementation("androidx.compose.ui:ui:1.0.0-beta01")
    implementation("androidx.compose.ui:ui-tooling:1.0.0-beta01")
    implementation("androidx.compose.foundation:foundation:1.0.0-beta01")
    implementation("androidx.compose.material:material:1.0.0-beta01")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha02")

    implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.3")

    implementation("androidx.viewpager2:viewpager2:1.0.0")

    implementation("com.github.etiennelenhart.eiffel:eiffel:5.0.0")
    implementation("com.github.etiennelenhart.eiffel:eiffel-test:5.0.0")

    implementation("com.google.android.material:material:1.3.0")

    implementation("com.google.dagger:hilt-android:2.31.2-alpha")

    implementation(platform("com.google.firebase:firebase-bom:26.4.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    implementation("com.karumi:dexter:6.2.2")

    implementation("com.kirich1409.viewbindingpropertydelegate:viewbindingpropertydelegate:1.4.1")

    implementation("com.mikepenz:iconics-core:5.2.4")

    implementation("joda-time:joda-time:2.10")

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.30")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.4.30")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.30")

    implementation("org.linphone:linphone-sdk-android-debug:4.4.24")

    // For instrumentation tests
//    androidTestImplementation  'com.google.dagger:hilt-android-testing:<VERSION>'
//    androidTestAnnotationProcessor 'com.google.dagger:hilt-android-compiler:<VERSION>'
//
//    // For local unit tests
//    testImplementation 'com.google.dagger:hilt-android-testing:<VERSION>'
//    testAnnotationProcessor 'com.google.dagger:hilt-android-compiler:<VERSION>'

    kapt("androidx.hilt:hilt-compiler:1.0.0-alpha03")
    kapt("com.google.dagger:hilt-android-compiler:2.31.2-alpha")


    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}