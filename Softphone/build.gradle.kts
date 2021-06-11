// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }

    }
    dependencies {
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.3.5")

        classpath("com.android.tools.build:gradle:7.0.0-beta03")

        classpath("com.diffplug.spotless:spotless-plugin-gradle:5.9.0")

        classpath("com.google.dagger:hilt-android-gradle-plugin:2.36")
        classpath("com.google.firebase:firebase-appdistribution-gradle:2.1.2")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.7.0")
        classpath("com.google.gms:google-services:4.3.8")

        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10")


        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven { url = uri("https://jitpack.io") }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
