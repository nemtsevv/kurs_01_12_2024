plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.kurs_01_12_2024"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.kurs_01_12_2024"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    // Работа с WorkManager
    implementation("androidx.work:work-runtime:2.7.1")

    // Для поддержки уведомлений и других функций
    implementation("androidx.core:core-ktx:1.10.0")  // Исправлена ошибка с лишними пробелами и кавычками

    // Зависимости для RecyclerView, Material и других UI компонентов
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation(libs.appcompat)  // Использование алиаса
    implementation(libs.material)   // Использование алиаса
    implementation(libs.activity)   // Использование алиаса
    implementation(libs.constraintlayout) // Использование алиаса

    // Тестирование
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
