import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.drop.near"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.drop.near"
        minSdk = 24
        targetSdk = 37
        versionCode = 2
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystorePropertiesFile = file("keystore.properties").takeIf { it.exists() }
                ?: rootProject.file("keystore.properties").takeIf { it.exists() }
            val keystoreProperties = Properties().apply {
                if (keystorePropertiesFile != null) {
                    load(FileInputStream(keystorePropertiesFile))
                }
            }

            val localPropertiesFile = rootProject.file("local.properties")
            val localProperties = Properties().apply {
                if (localPropertiesFile.exists()) {
                    load(FileInputStream(localPropertiesFile))
                }
            }

            fun findProp(propKey: String, envKey: String): String? {
                return keystoreProperties.getProperty(propKey)
                    ?: localProperties.getProperty(propKey)
                    ?: System.getenv(envKey)
            }

            val rawStorePath = findProp("storeFile", "KEYSTORE_FILE")
                ?: (if (file("release.keystore").exists()) file("release.keystore").absolutePath else null)
                ?: (if (rootProject.file("release.keystore").exists()) rootProject.file("release.keystore").absolutePath else null)

            val resolvedKeystoreFile = rawStorePath?.let { path ->
                val directFile = file(path)
                if (directFile.exists()) directFile else rootProject.file(path).takeIf { it.exists() }
            }

            if (resolvedKeystoreFile != null && resolvedKeystoreFile.exists()) {
                storeFile = resolvedKeystoreFile
                storePassword = findProp("storePassword", "KEYSTORE_PASSWORD")
                keyAlias = findProp("keyAlias", "KEY_ALIAS")
                keyPassword = findProp("keyPassword", "KEY_PASSWORD")
            } else {
                // Fallback to debug keystore for development / testing builds
                initWith(getByName("debug"))
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.kotlin.test)
}

