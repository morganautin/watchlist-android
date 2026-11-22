plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt") // nécessaire pour générer le code Room
}

android {
    namespace = "fr.estiam.watchlist"
    compileSdk = 35
    defaultConfig {
        applicationId = "fr.estiam.watchlist"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        // Lanceur des tests Espresso (tests d'interface sur émulateur)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    // ViewBinding : accès typé aux vues XML (b.titleText au lieu de findViewById)
    buildFeatures { viewBinding = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
    testOptions {
        // Les tests JUnit peuvent appeler des classes Android sans planter (valeurs par défaut)
        unitTests.isReturnDefaultValues = true
        // Tests Espresso : animations désactivées sur l'émulateur pendant les tests (plus fiables)
        animationsDisabled = true
    }
}

dependencies {
    // --- Interface (écrans XML, Material Design, listes) ---
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.recyclerview:recyclerview:1.4.0")

    // --- Architecture : ViewModel + coroutines/Flow liés au cycle de vie ---
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.2")

    // --- Paging 3 : catalogue chargé page par page ---
    implementation("androidx.paging:paging-runtime-ktx:3.3.6")

    // --- Room : base de données locale ---
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")
    implementation("androidx.room:room-paging:2.7.2")
    kapt("androidx.room:room-compiler:2.7.2")

    // --- DataStore : préférences persistantes ---
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // --- Tests JUnit (sur l'ordinateur, sans émulateur) ---
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("org.json:json:20240303") // pour tester le parsing JSON hors Android

    // --- Tests Espresso (sur émulateur) ---
    // Versions 1.7 / 3.7 obligatoires pour les émulateurs Android 16 et plus récents :
    // les anciennes versions d'Espresso plantent (NoSuchMethodException sur InputManager).
    androidTestImplementation("androidx.test:core:1.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test:rules:1.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}
