// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "_" apply false
    id("org.jetbrains.kotlin.android") version "_" apply false
    id("com.google.gms.google-services") version "_" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "_" apply false
    id("com.github.ben-manes.versions") version "_"
    id("org.owasp.dependencycheck") version "_"
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(Android.tools.build.gradlePlugin)           // AGP
        classpath(Kotlin.gradlePlugin) // Kotlin
        classpath(Google.playServicesGradlePlugin)            // Google Services
        classpath("org.owasp:dependency-check-gradle:_") {       // OWASP
            exclude(group = "org.apache.commons", module = "commons-compress")
        }
        classpath("org.apache.commons:commons-compress:_")
        classpath("com.google.firebase:firebase-crashlytics-gradle:_")

    }
}