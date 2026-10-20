plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Версию можно передать при сборке: ./gradlew assembleRelease -PappVersion=1.2
// versionCode считается из версии (1.2.3 -> 10203), поэтому каждая новая версия ставится поверх старой.
val appVersionName = (findProperty("appVersion") as String?)?.removePrefix("v") ?: "1.1"
val appVersionCode = appVersionName.split(".").map { it.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }
    .let { parts -> parts.getOrElse(0) { 0 } * 10000 + parts.getOrElse(1) { 0 } * 100 + parts.getOrElse(2) { 0 } }

android {
    namespace = "com.example.cycletracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.cycletracker"
        minSdk = 26
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName
        buildConfigField("String", "UPDATE_REPO", "\"zaxar1231/cycle-tracker\"")
    }

    // Все сборки (Android Studio, CI, debug и release) подписываются одним ключом из signing/release.p12,
    // поэтому любая новая версия ставится поверх старой. Ключ лежит в репозитории открыто — так решил
    // владелец, чтобы не настраивать секреты. Свой ключ можно подставить через переменные окружения.
    val keystoreFile = System.getenv("RELEASE_KEYSTORE_FILE")
    signingConfigs {
        create("shared") {
            if (keystoreFile != null) {
                storeFile = file(keystoreFile)
                storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("RELEASE_KEY_ALIAS")
                keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
            } else {
                storeFile = rootProject.file("signing/release.p12")
                storePassword = "cycletracker"
                keyAlias = "cycletracker"
                keyPassword = "cycletracker"
            }
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("shared")
        }
        release {
            signingConfig = signingConfigs.getByName("shared")
            isMinifyEnabled = false
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
        buildConfig = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
}
