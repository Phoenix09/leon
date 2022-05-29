/*
 * Léon - The URL Cleaner
 * Copyright (C) 2022 Sven Jacobs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.adarshr.gradle.testlogger.theme.ThemeType.STANDARD
import java.util.*

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.parcelize")
    id("dagger.hilt.android.plugin")
    id("com.mikepenz.aboutlibraries.plugin")
    alias(libs.plugins.triplet.play)
    alias(libs.plugins.adarshr.test.logger)
}

android {
    compileSdk = 32
    buildToolsVersion = "32.0.0"

    defaultConfig {
        applicationId = "com.svenjacobs.app.leon"
        minSdk = 21
        targetSdk = 32
        versionCode = 230
        versionName = "0.7.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments(
                    mapOf(
                        "room.schemaLocation" to "$projectDir/schemas",
                        "room.incremental" to "true",
                        "room.expandProjection" to "true"
                    )
                )
            }
        }
    }

    signingConfigs {
        val signingFile = rootProject.file("signing.properties")
        if (!signingFile.exists()) {
            logger.warn("No signing properties found. Release signing not possible.")
            return@signingConfigs
        }

        create("release") {
            val props = Properties()
            signingFile.inputStream().use { props.load(it) }

            storeFile = rootProject.file("upload-keystore.jks")
            storePassword = props.getProperty("storePassword")
            keyPassword = props.getProperty("keyPassword")
            keyAlias = "upload"
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }

        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        freeCompilerArgs = listOf(
            "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xopt-in=kotlin.RequiresOptIn"
        )
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.get()
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

play {
    serviceAccountCredentials.set(rootProject.file("google-play-service-account.json"))
    defaultToAppBundles.set(true)
}

testlogger {
    theme = STANDARD
}

dependencies {
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)

    //region Compose
    implementation(libs.bundles.androidx.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    //endregion

    implementation(libs.androidx.startup.runtime)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.google.hilt.android)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.browser)
    implementation(libs.jakewharton.timber)
    implementation(libs.square.moshi)
    implementation(libs.mikepenz.aboutlibraries.ui)

    debugImplementation(libs.facebook.stetho)

    kapt(libs.google.hilt.android.compiler)
    kapt(libs.androidx.room.compiler)

    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.agent.jvm)
}
