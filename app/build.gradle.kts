plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") // KSP for Moshi
}

android {
    namespace = "com.example.paymentdemoandroid"
    compileSdk = 34 // Use a recent SDK

    defaultConfig {
        applicationId = "com.example.paymentdemoandroid"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Retrieve BETA API key from gradle.properties
        val betaApiKey: String = project.properties["BETA_API_KEY"] as? String ?: "your_beta_api_key_here"

        // Expose BETA API key to BuildConfig
        buildConfigField("String", "BETA_API_KEY", "\"$betaApiKey\"")
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
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true // Enable BuildConfig generation
    }
    
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.jvmArgs("-Dnet.bytebuddy.experimental=true")
            }
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0") // Updated core-ktx version
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0") // Updated material version
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0") // For lifecycleScope

    // Networking - Retrofit & Moshi
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0") // Ensure Moshi Kotlin support
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.0") // Moshi codegen processor
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0") // For logging network calls

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.0")
    
    // ByteBuddy compatibility for Java 23
    testImplementation("net.bytebuddy:byte-buddy:1.15.10")
    testImplementation("net.bytebuddy:byte-buddy-agent:1.15.10")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
