import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

android {
    namespace = "com.example.quizzly"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.quizzly"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        manifestPlaceholders["facebookAppId"] = localProperties.getProperty("FACEBOOK_APP_ID", "")
        manifestPlaceholders["fbLoginProtocolScheme"] = localProperties.getProperty("FB_LOGIN_PROTOCOL_SCHEME", "")
        manifestPlaceholders["facebookClientToken"] = localProperties.getProperty("FACEBOOK_CLIENT_TOKEN", "")

        buildConfigField("String", "FACEBOOK_APP_ID", "\"${localProperties.getProperty("FACEBOOK_APP_ID", "")}\"")
        buildConfigField("String", "FACEBOOK_CLIENT_TOKEN", "\"${localProperties.getProperty("FACEBOOK_CLIENT_TOKEN", "")}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
        viewBinding = true
        buildConfig = true  // ← cần thêm để dùng buildConfigField
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(platform("com.google.firebase:firebase-bom:34.14.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation(libs.firebase.auth)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.facebook.android.sdk)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}