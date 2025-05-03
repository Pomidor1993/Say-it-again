import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.github.ben-manes.versions")
    id("org.owasp.dependencycheck")
    id("com.google.firebase.crashlytics")
}
val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
android {
    namespace = "com.tomato.sayitagain"
    compileSdk = 35

    signingConfigs {
        create("release") {
            storeFile = file(localProperties.getProperty("STORE_FILE"))
            storePassword = localProperties.getProperty("STORE_PASSWORD")
            keyAlias = localProperties.getProperty("KEY_ALIAS")
            keyPassword = localProperties.getProperty("KEY_PASSWORD")

        }
    }


    defaultConfig {
        applicationId = "com.tomato.sayitagain"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        testOptions {
            unitTests.isReturnDefaultValues = true
            animationsDisabled = true
        }
        buildConfigField(
            "String",
            "CRASH_REPORTING_API_KEY",
            "\"${project.findProperty("GOOGLE_CRASH_REPORTING_API_KEY")}\""
        )
    }

    buildTypes {
        debug {
            versionNameSuffix = "-debug"
            isDebuggable = true
            buildConfigField("boolean", "ENABLE_LOGS", "true")

        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("boolean", "ENABLE_LOGS", "false")

        }
    }

    configurations.all {
        resolutionStrategy {
            force("org.apache.commons:commons-compress:_")
            force("io.netty:netty-buffer:_")
            force("io.netty:netty-codec-http:_")
            force("io.netty:netty-codec-socks:_")
            force("io.netty:netty-handler-proxy:_")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    packaging.resources {
        excludes += setOf(
            "/META-INF/{AL2.0,LGPL2.1}",
            "**/attach_hotspot_windows.dll",
            "META-INF/versions/9/previous-compilation-data.bin"
        )
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        animationsDisabled = true
    }
}


dependencies {
    // Media3
    implementation(AndroidX.media3.exoPlayer)
    implementation(AndroidX.media3.ui)
    implementation(AndroidX.media3.dataSource)
    implementation(AndroidX.media3.database)

    //SplashScreen
    implementation("androidx.core:core-splashscreen:_")

    // GIF
    implementation("pl.droidsonroids.gif:android-gif-drawable:_")

    // JSON
    implementation("com.google.code.gson:gson:_")

    // Firebase
    implementation(platform(Firebase.bom))
    implementation(Firebase.cloudStorageKtx)
    implementation("com.google.firebase:firebase-appcheck-playintegrity:_")
    implementation ("com.google.firebase:firebase-appcheck:_")
    implementation(Firebase.cloudStorageKtx)
    implementation("com.google.firebase:firebase-appcheck-ktx:_")
    implementation("com.google.firebase:firebase-crashlytics")


    // Compose
    implementation(platform(AndroidX.compose.bom))
    implementation(AndroidX.activity.compose)
    implementation(AndroidX.compose.material3)
    implementation(AndroidX.compose.material.icons.extended)
    implementation(AndroidX.core.ktx)
    androidTestImplementation(platform(AndroidX.compose.bom))
    implementation(AndroidX.lifecycle.viewModelCompose)


    // CameraX
    implementation(AndroidX.camera.core)
    implementation(AndroidX.camera.camera2)
    implementation(AndroidX.camera.lifecycle)
    implementation(AndroidX.camera.view)

    // ML Kit
    implementation(Google.android.playServices.mlKit.vision.barcodeScanning)

    // Lottie
    implementation("com.airbnb.android:lottie-compose:_")

    // Test
    testImplementation(Testing.junit4)
    testImplementation(Testing.mockito.core)
    testImplementation(KotlinX.coroutines.test)

    // Android Test
    androidTestImplementation(AndroidX.test.ext.junit)
    androidTestImplementation(AndroidX.test.espresso.core)
    androidTestImplementation(AndroidX.compose.ui.testJunit4)
    debugImplementation(AndroidX.compose.ui.tooling)
    debugImplementation(AndroidX.compose.ui.testManifest)

    // gRPC
    implementation("io.grpc:grpc-netty:_") {
        exclude(group = "io.netty")
    }
    implementation("io.grpc:grpc-protobuf:_")
    implementation("io.grpc:grpc-stub:_")
    implementation("com.google.protobuf:protobuf-java:_")
}
dependencyCheck {
    nvd {
        apiKey = localProperties.getProperty("OWASP_API_KEY", "")
    }
    analyzers {
        assemblyEnabled = false
    }
    suppressionFiles = listOf("owasp-suppressions.xml")
}