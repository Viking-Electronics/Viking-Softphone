import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    id("com.android.application")

    id("com.google.firebase.appdistribution")
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")
    id("com.google.protobuf") version "0.8.12"

    id("dagger.hilt.android.plugin")



    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")

    id("androidx.navigation.safeargs.kotlin")
}

repositories {
    maven {
        // Replace snapshots by releases for releases !
        url = uri("https://linphone.org/releases/maven_repository")
    }
    maven{
        url = uri("https://jitpack.io")
    }
}

android {
    compileSdk = 30
    buildToolsVersion = "30.0.3"

    defaultConfig {
        applicationId = "com.vikingelectronics.softphone"
        minSdk = 26
        targetSdk = 30
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
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
//        getByName("qa") {
//            minifyEnabled(true)
//            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
//        }
        debug { 
            isMinifyEnabled = false
            ext.set("enableCrashlytics", false)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }


    composeOptions {
        kotlinCompilerExtensionVersion = "1.0.0-beta08"
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"

    }
    kapt {
        correctErrorTypes = true
    }
    packagingOptions {
        exclude("META-INF/*")
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.14.0"
    }

//    generateProtoTasks {
//        all().forEach { task ->
//            task.builtins {
//
//                java {
//                    this.toolchain.implementation = "lite"
//                    option = "lite"
//                }
//            }
//        }
//    }
}

dependencies {

//    implementation(project(":legacy"))

    implementation("androidx.appcompat:appcompat:1.3.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("androidx.core:core-ktx:1.5.0")

    implementation("androidx.activity:activity-compose:1.3.0-beta01")
    implementation("androidx.compose.ui:ui:1.0.0-beta08")
    implementation("androidx.compose.ui:ui-tooling:1.0.0-beta08")
    implementation("androidx.compose.ui:ui-viewbinding:1.0.0-beta08")
    implementation("androidx.compose.foundation:foundation:1.0.0-beta08")
    implementation("androidx.compose.material:material:1.0.0-beta08")
    //This aar is large, if compile and install times get too long we can pull the icons we need
    implementation("androidx.compose.material:material-icons-extended:1.0.0-beta08")
    implementation("androidx.compose.runtime:runtime-livedata:1.0.0-beta08")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha06")

    implementation("androidx.datastore:datastore-core:1.0.0-beta01")
    implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0-alpha02")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.media:media:1.3.1")
    implementation("androidx.navigation:navigation-compose:2.4.0-alpha02")
    implementation("androidx.paging:paging-runtime:3.0.0")
    implementation("androidx.paging:paging-compose:1.0.0-alpha10")
    implementation("androidx.security:security-crypto:1.1.0-alpha03")

    //If the app ever starts crashing for no apparent reason try updating these two libraries before you pull your hair out
    implementation("com.google.accompanist:accompanist-coil:0.11.1")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.11.1")
    implementation("com.google.android.material:material:1.3.0")

    implementation("com.google.crypto.tink:tink-android") {
        version {
            strictly("1.4.0")
        }
    }

    implementation(platform("com.google.firebase:firebase-bom:26.6.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")

    implementation("com.github.DavidProdinger:weekdays-selector:1.1.1")
    debugImplementation("com.github.pandulapeter.beagle:ui-bottom-sheet:2.6.0")
    implementation("com.github.pandulapeter.beagle:log:2.6.0")
    releaseImplementation("com.github.pandulapeter.beagle:noop:2.6.0")
    implementation("com.github.tfcporciuncula.flow-preferences:flow-preferences:1.4.0")
    implementation("com.jakewharton.timber:timber:4.7.1")
    implementation("com.karumi:dexter:6.2.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.12.0")

    implementation("io.github.vanpra.compose-material-dialogs:datetime:0.4.2")

    implementation("joda-time:joda-time:2.10")

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.10")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.1.1")

    implementation("org.linphone:linphone-sdk-android-debug:5.0+")

    // For instrumentation tests
//    androidTestImplementation  'com.google.dagger:hilt-android-testing:<VERSION>'
//    androidTestAnnotationProcessor 'com.google.dagger:hilt-android-compiler:<VERSION>'
//
//    // For local unit tests
//    testImplementation 'com.google.dagger:hilt-android-testing:<VERSION>'
//    testAnnotationProcessor 'com.google.dagger:hilt-android-compiler:<VERSION>'

    implementation("com.google.dagger:hilt-android:2.36")
    kapt("com.google.dagger:hilt-compiler:2.36")


    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}