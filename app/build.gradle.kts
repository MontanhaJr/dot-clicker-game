import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Lógica para ler o local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

val adMobAppId = localProperties.getProperty("admob.app.id") ?: ""
val adMobBannerId = localProperties.getProperty("admob.banner.id") ?: ""
val adMobInterstitialId = localProperties.getProperty("admob.interstitial.id") ?: ""
val playGamesProjectId = localProperties.getProperty("playgames.project.id") ?: ""
val achievementFounderId = localProperties.getProperty("achievement.founder.id") ?: ""

android {
    namespace = "com.montanhajr.pointgame"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.montanhajr.pointgame"
        minSdk = 24
        targetSdk = 36
        versionCode = 14
        versionName = "1.4.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Passa o ID do Play Games para o Manifest
        manifestPlaceholders["playGamesProjectId"] = playGamesProjectId
        manifestPlaceholders["adMobAppId"] = adMobAppId
    }

    buildTypes {
        debug {
            buildConfigField("String", "AD_UNIT_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField("String", "INTERSTITIAL_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "ACHIEVEMENT_FOUNDER_ID", "\"CgkIw8SWm9MZEAIQAQ\"") // ID de teste ou real
            manifestPlaceholders["adMobAppId"] = "ca-app-pub-3940256099942544~3347511713" // ID de Teste AdMob
        }
        
        create("staging") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
            buildConfigField("String", "AD_UNIT_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField("String", "INTERSTITIAL_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "ACHIEVEMENT_FOUNDER_ID", "\"$achievementFounderId\"")
            signingConfig = signingConfigs.getByName("debug")
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "AD_UNIT_ID", "\"$adMobBannerId\"")
            buildConfigField("String", "INTERSTITIAL_AD_UNIT_ID", "\"$adMobInterstitialId\"")
            buildConfigField("String", "ACHIEVEMENT_FOUNDER_ID", "\"$achievementFounderId\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
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
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.google.play.services.ads)
    implementation(libs.google.play.services.games)
    implementation(libs.billing.ktx)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.compose.foundation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
