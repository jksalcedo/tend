import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Load the version.properties file
val versionPropsFile: File = rootProject.file("version.properties")
val versionProps = Properties()
if (versionPropsFile.exists()) {
    versionProps.load(FileInputStream(versionPropsFile))
} else {
    throw GradleException("version.properties file not found! Please create it in the project root.")
}

// Safely parse the values
val vMajor = versionProps.getProperty("VERSION_MAJOR", "0").toInt()
val vMinor = versionProps.getProperty("VERSION_MINOR", "1").toInt()
val vPatch = versionProps.getProperty("VERSION_PATCH", "0").toInt()
val vStage = versionProps.getProperty("VERSION_STAGE", "beta").lowercase()
val vBuild = versionProps.getProperty("VERSION_BUILD", "1").toInt()

val stageWeight = when (vStage) {
    "alpha" -> 0
    "beta" -> 1
    "rc" -> 2
    "stable" -> 3
    else -> 0
}

val suffix = if (vStage == "stable") "" else "-$vStage.$vBuild"

val computedVersionCode =
    versionProps.getProperty("VERSION_CODE")?.toInt() ?: ((vMajor * 10000000) +
            (vMinor * 100000) +
            (vPatch * 1000) +
            (stageWeight * 100) +
            vBuild)

val computedVersionName =
    versionProps.getProperty("VERSION_NAME") ?: "v$vMajor.$vMinor.$vPatch$suffix"

tasks.register("updateVersionProperties") {
    doLast {
        val calculatedCode = (vMajor * 10000000) +
                (vMinor * 100000) +
                (vPatch * 1000) +
                (stageWeight * 100) +
                vBuild
        val calculatedName = "v$vMajor.$vMinor.$vPatch$suffix"

        versionProps.setProperty("VERSION_CODE", calculatedCode.toString())
        versionProps.setProperty("VERSION_NAME", calculatedName)
        versionProps.store(versionPropsFile.outputStream(), null)
    }
}

android {
    namespace = "com.jksalcedo.tend"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.jksalcedo.tend"
        minSdk = 26
        targetSdk = 37
        versionCode = computedVersionCode
        versionName = computedVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("main") {
            assets.srcDirs("$projectDir/schemas")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

afterEvaluate {
    tasks.named("assembleRelease") {
        doLast {
            val outDir = file("${layout.buildDirectory.get()}/outputs/apk/release")
            outDir.listFiles()?.forEach { apk ->
                if (apk.name.endsWith(".apk") && !apk.name.contains("-signed")) {
                    val newName = "Tend-${android.defaultConfig.versionName}-${android.defaultConfig.versionCode}.apk"
                    apk.renameTo(File(outDir, newName))
                }
            }
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.compose.material.icons.extended)

    // Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Gson
    implementation(libs.gson)

    // DataStore
    implementation(libs.androidx.datastore)

    // Qr
    implementation(libs.core)
    implementation(libs.quickie.bundled)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}