// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false


}
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.gradle)
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.20")
        classpath(libs.google.services)
        classpath("org.owasp:dependency-check-gradle:12.1.0") {
            exclude(group = "org.apache.commons", module = "commons-compress")
        }
        classpath("org.apache.commons:commons-compress:1.27.1")
    }
}
