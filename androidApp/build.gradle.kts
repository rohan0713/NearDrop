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
                ?: file("androidApp/keystore.properties").takeIf { it.exists() }
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

            fun findProp(vararg keys: String): String? {
                for (key in keys) {
                    keystoreProperties.getProperty(key)?.let { return it }
                    localProperties.getProperty(key)?.let { return it }
                    System.getenv(key)?.let { return it }
                }
                return null
            }

            val rawStorePath = findProp("storeFile", "store_file", "KEYSTORE_FILE", "KEYSTORE_PATH")
                ?: "neardrop.jks"

            val resolvedKeystoreFile = listOfNotNull(
                rawStorePath.let { file(it) },
                rawStorePath.let { rootProject.file(it) },
                rawStorePath.let { file("androidApp/$it") },
                rawStorePath.let { rootProject.file("androidApp/$it") },
                file("neardrop.jks"),
                rootProject.file("neardrop.jks"),
                rootProject.file("androidApp/neardrop.jks"),
                file("release.keystore"),
                rootProject.file("release.keystore")
            ).firstOrNull { it.exists() }

            if (resolvedKeystoreFile != null && resolvedKeystoreFile.exists()) {
                storeFile = resolvedKeystoreFile
                storePassword = findProp("storePassword", "store_password", "KEYSTORE_PASSWORD", "STORE_PASSWORD")
                keyAlias = findProp("keyAlias", "key_alias", "KEY_ALIAS", "ALIAS") ?: "key0"
                keyPassword = findProp("keyPassword", "key_password", "KEY_PASSWORD") ?: storePassword
            } else {
                // Fallback to debug keystore for development / testing builds
                val homeDir = System.getProperty("user.home")
                val debugKeystore = file("$homeDir/.android/debug.keystore")
                if (!debugKeystore.exists()) {
                    debugKeystore.parentFile?.mkdirs()
                    try {
                        val process = ProcessBuilder(
                            "keytool", "-genkey", "-v",
                            "-keystore", debugKeystore.absolutePath,
                            "-storepass", "android",
                            "-alias", "androiddebugkey",
                            "-keypass", "android",
                            "-keyalg", "RSA",
                            "-keysize", "2048",
                            "-validity", "10000",
                            "-dname", "CN=Android Debug,O=Android,C=US"
                        ).start()
                        process.waitFor()
                    } catch (_: Exception) {
                    }
                }
                if (debugKeystore.exists()) {
                    storeFile = debugKeystore
                    storePassword = "android"
                    keyAlias = "androiddebugkey"
                    keyPassword = "android"
                } else {
                    initWith(getByName("debug"))
                }
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

