import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    kotlin("plugin.serialization") version "1.5.20"
    id("com.android.library")
}

version = "1.0"

repositories {
    maven {
        // Replace snapshots by releases for releases !
        url = uri("https://linphone.org/releases/maven_repository")
    }
    maven{
        url = uri("https://jitpack.io")
    }
}

kotlin {
    android()

    val iosTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget =
        if (System.getenv("SDK_NAME")?.startsWith("iphoneos") == true)
            ::iosArm64
        else
            ::iosX64

    iosTarget("ios") {}

    cocoapods {
        summary = "Shared Code between the Android and iOS apps"
        homepage = ""
        ios.deploymentTarget = "14.1"
        frameworkName = "shared"
        // set path to your ios project podfile, e.g. podfile = project.file("../iosApp/Podfile")
    }
    
    sourceSets {
        all {
            languageSettings.apply {
                apiVersion = "1.5"
                languageVersion = "1.5"
                progressiveMode = true
                useExperimentalAnnotation("kotlinx.coroutines.ExperimentalCoroutinesApi")
                useExperimentalAnnotation("kotlinx.serialization.InternalSerializationApi")
            }
        }
        val commonMain by getting{
            dependencies {
                implementation("dev.icerock.moko:parcelize:0.7.0")
                implementation("dev.gitlive:firebase-firestore:1.3.1")
//                implementation("dev.gitlive:firebase-storage:1.3.1")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.8")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
                implementation("org.linphone:linphone-sdk-android:5.0+")
                implementation(kotlin("reflect"))
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
            }
        }
        val iosMain by getting
        val iosTest by getting
    }
}

android {
    compileSdk = 30
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 26
        targetSdk = 30 
    }
}
