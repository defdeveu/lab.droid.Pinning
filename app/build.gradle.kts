import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val labProperties = rootProject.file("lab.local.properties")
    .takeIf { it.exists() }
    ?.let { file ->
        Properties().apply { file.inputStream().use { stream -> load(stream) } }
    }

fun labValue(key: String, default: String): String = labProperties?.getProperty(key) ?: default

android {
    namespace = "dev.def.lab.droid.pinning"
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.def.lab.droid.pinning"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "LAB_PINNING_URL",
            "\"${labValue("lab.pinning.url", "https://zsk.labs.def.dev/pinning/success")}\"",
        )
        buildConfigField(
            "String",
            "LAB_CLEARTEXT_URL",
            "\"${labValue("lab.cleartext.url", "http://zsk.labs.def.dev/pinning/success")}\"",
        )
        buildConfigField(
            "String",
            "LAB_LEAF_PIN",
            "\"${labValue("lab.leaf.pin", "sha256/PlnlvxfcGEEUs/29JFwFdOb2lQKErvisb/6LqfXrlOw=")}\"",
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

kotlin {
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Werror")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.android)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
