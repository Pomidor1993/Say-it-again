plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"
    id("com.github.ben-manes.versions") version "0.52.0"
    id("org.owasp.dependencycheck")
}
dependencyCheck {
    nvd {
        apiKey = "0d066371-de43-4aa0-940f-997ef0d665b4"
    }
    analyzers {
        assemblyEnabled = false
    }
}

android {

    namespace = "com.example.sayitagain"
    compileSdk = 35
    buildFeatures {
        compose = true
    }
    defaultConfig {
        applicationId = "com.example.sayitagain"
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
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    configurations.all {
        resolutionStrategy {
            force("org.apache.commons:commons-compress:1.27.1")
            force("io.netty:netty-buffer:4.2.0.Final")
            force("io.netty:netty-codec-http:4.2.0.Final")
            force("io.netty:netty-codec-socks:4.2.0.Final")
            force("io.netty:netty-handler-proxy:4.2.0.Final")
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
        animationsDisabled = true // Dodano tę linię
    }
}

dependencies {
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.datasource)
    implementation(libs.androidx.media3.database)
    implementation(libs.pl.droidsonroids.gif)
    implementation(libs.google.gson)

    // Firebase
    implementation(platform(libs.firebase.bom.v3400))
    implementation(libs.google.firebase.storage.ktx)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose.v190)
    implementation(libs.material3)
    implementation(libs.androidx.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // CameraX
    implementation(libs.androidx.camera.core.v140alpha04)
    implementation(libs.androidx.camera.camera2.v140alpha04)
    implementation(libs.androidx.camera.lifecycle.v140alpha04)
    implementation(libs.androidx.camera.view.v140alpha04)

    // ML Kit
    implementation(libs.play.services.mlkit.barcode.scanning.v1830)

    // Lottie
    implementation(libs.lottie.compose)


    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.kotlinx.coroutines.test)

    // Dependencies for Android instrumentation tests (UI tests)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    //gRPC i inne zależności związane z protokołem gRPC
    implementation("io.grpc:grpc-netty:1.71.0"){
        exclude(group = "io.netty") }
    implementation("io.grpc:grpc-protobuf:1.71.0")
    implementation("io.grpc:grpc-stub:1.71.0")
    implementation("com.google.protobuf:protobuf-java:4.30.2")
}