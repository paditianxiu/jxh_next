import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "1.9.22"
}

android {
    namespace = "me.padi.jxh"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "me.padi.jxh"
        minSdk = 24
        targetSdk = 36
        versionCode = 3
        versionName = "1.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val buildTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        buildConfigField("String", "BUILD_TIME", "\"$buildTime\"")
    }

    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }

        androidComponents {
            onVariants(selector().all()) { variant ->
                variant.outputs.map { it as com.android.build.api.variant.impl.VariantOutputImpl }
                    .forEach { output ->
                        output.outputFileName =
                            "江小航_v${output.versionName.get()}(${variant.name}).apk"
                    }
            }
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
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.compose.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation("top.yukonga.miuix.kmp:miuix-android:0.8.0")
    // 可选：添加 miuix-icons 以获取更多图标
    implementation("top.yukonga.miuix.kmp:miuix-icons-android:0.8.0")

    implementation("androidx.navigation3:navigation3-runtime:1.1.0-alpha03")
    implementation("top.yukonga.miuix.kmp:miuix-navigation3-ui:0.8.0")
    implementation("top.yukonga.miuix.kmp:miuix-navigation3-adaptive:0.8.0")

    implementation(libs.bundles.ktor)

    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)

    implementation("org.jsoup:jsoup:1.17.2")

    implementation("dev.whyoleg.cryptography:cryptography-core:0.5.0")
    implementation("dev.whyoleg.cryptography:cryptography-provider-optimal:0.5.0")

    implementation("com.tencent:mmkv:2.3.0")

    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    implementation("io.github.panpf.zoomimage:zoomimage-compose-glide:1.4.0")
    implementation("io.coil-kt.coil3:coil-compose:3.3.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")

    implementation("io.github.kevinnzou:compose-webview:0.33.6")
    implementation("io.github.afreakyelf:Pdf-Viewer:2.3.7")


}