import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
}

val localProps = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProps.load(FileInputStream(localPropertiesFile))
}

val ncpKeyId = localProps.getProperty("ncp.access.key.id") ?: ""
val ncpSecretKey = localProps.getProperty("ncp.secret.key") ?: ""
val baseServerUrl = localProps.getProperty("server.base.url") ?: ""
val naverMapClientId = localProps.getProperty("naver.maps.client.id") ?: ""
val naverMapClientSecret = localProps.getProperty("naver.maps.client.secret") ?: ""
val naverDevClientId = localProps.getProperty("naver.dev.client.id") ?: ""
val naverDevClientSecret = localProps.getProperty("naver.dev.client.secret") ?: ""

val tossClientKey = localProps.getProperty("TOSS_CLIENT_KEY") ?: ""

android {
    namespace = "com.example.bisit"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.bisit"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "NCP_KEY_ID", "\"$ncpKeyId\"")
        buildConfigField("String", "NCP_SECRET_KEY", "\"$ncpSecretKey\"")
        buildConfigField("String", "BASE_SERVER_URL", "\"$baseServerUrl\"")

        buildConfigField("String", "NAVER_MAP_CLIENT_ID", "\"$naverMapClientId\"")
        buildConfigField("String", "NAVER_MAP_CLIENT_SECRET", "\"$naverMapClientSecret\"")
        buildConfigField("String", "NAVER_DEV_CLIENT_ID", "\"$naverDevClientId\"")
        buildConfigField("String", "NAVER_DEV_CLIENT_SECRET", "\"$naverDevClientSecret\"")

        buildConfigField("String", "TOSS_CLIENT_KEY", "\"$tossClientKey\"")

        manifestPlaceholders["NAVER_CLIENT_ID"] = naverMapClientId
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // Material Design
    implementation("com.google.android.material:material:1.12.0")

    // Naver Maps & Location
    implementation("com.naver.maps:map-sdk:3.23.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Retrofit & Gson
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.8.9")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // Toss Payments SDK
    implementation("com.github.tosspayments:payment-sdk-android:0.1.21")

    // log
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}
