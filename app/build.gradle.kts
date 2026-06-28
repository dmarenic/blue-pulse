import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.google.gms.google-services")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.2.10-2.0.2"
}

// Tajne (API ključevi) čitamo iz local.properties koji se NE commita u git.
// Tako ključevi nikad ne završe u source kodu (sigurnosni best practice).
val localProperties = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        FileInputStream(localFile).use { load(it) }
    }
}
// TheSportsDB API ključ. Besplatni dev ključ je "123"; promjenjiv u local.properties
// bez ikakve izmjene koda.
val sportsDbApiKey: String = localProperties.getProperty("SPORTSDB_API_KEY", "123")
val mapsApiKey: String = localProperties.getProperty("MAPS_API_KEY", "")
// imgbb besplatni image host (zamjena za Firebase Storage). Ključ: https://api.imgbb.com
val imgbbApiKey: String = localProperties.getProperty("IMGBB_API_KEY", "")

android {
    namespace = "com.dominik.bluepuls"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.dominik.bluepuls"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Ključ je dostupan u kodu preko BuildConfig.SPORTSDB_API_KEY
        buildConfigField("String", "SPORTSDB_API_KEY", "\"$sportsDbApiKey\"")
        buildConfigField("String", "IMGBB_API_KEY", "\"$imgbbApiKey\"")
        // Google Maps ključ ubacujemo u AndroidManifest preko placeholdera
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
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
    // Downloadable Google Fonts (Lexend, Roboto, Tektur)
    implementation("androidx.compose.ui:ui-text-google-fonts")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    // Firebase (BOM upravlja verzijama svih Firebase biblioteka)
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")

    // Mreža (REST API)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Učitavanje slika (logoi timova, galerija, profilne)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // DataStore - lokalno spremanje glasa za igrača
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Room - offline cache utakmica i igrača
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")
    ksp("androidx.room:room-compiler:2.7.2")

    // Google Maps + lokacija (GPS)
    implementation("com.google.maps.android:maps-compose:4.4.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")

    // WorkManager (podsjetnik za utakmicu)
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Ikone (prošireni Material set)
    implementation("androidx.compose.material:material-icons-extended")

    // Permissioni u Composeu (runtime dozvole za lokaciju/notifikacije)
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // Testiranje
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
}