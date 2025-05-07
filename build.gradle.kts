// Top-level build file where you can add configuration options common to all sub-projects/modules.


buildscript {
    repositories {
        gradlePluginPortal()
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