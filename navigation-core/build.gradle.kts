import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kmpNativeCoroutines)
    alias(libs.plugins.vanniktechMavenPublish)
}

group = "io.github.foshlabs.navigation"
version = "0.1.0"

kotlin {
    androidLibrary {
        namespace = "com.foshlabs.navigation.core"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "NavigationCore"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kmp.observable.viewmodel)
            implementation(libs.koin.core)
        }

        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
            languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(group.toString(), "navigation-core", version.toString())
    pom {
        name.set("Fosh Labs Navigation Core")
        description.set("KMP navigation architecture with BaseViewModel, NavigationState, and UseCase patterns")
        inceptionYear.set("2025")
        url.set("https://github.com/foshlabs/fosh-labs-kmp-navigation-kit")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("foshlabs")
                name.set("Fosh Labs")
                url.set("https://github.com/foshlabs/")
            }
        }
        scm {
            url.set("https://github.com/foshlabs/fosh-labs-kmp-navigation-kit")
            connection.set("scm:git:git://github.com/foshlabs/fosh-labs-kmp-navigation-kit.git")
            developerConnection.set("scm:git:ssh://git@github.com/foshlabs/fosh-labs-kmp-navigation-kit.git")
        }
    }
}
