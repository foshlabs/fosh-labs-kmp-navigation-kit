import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kmpNativeCoroutines)
    id("maven-publish")
    id("signing")
}

group = "com.foshlabs.navigation"
version = "0.1.0"

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        publishLibraryVariants("release")
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

android {
    namespace = "com.foshlabs.navigation.core"
    compileSdk = 35
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    defaultConfig {
        minSdk = 24
    }
}

// Maven Central publishing configuration
publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set("Fosh Labs Navigation Core")
            description.set("KMP navigation architecture with BaseViewModel, NavigationState, and UseCase patterns")
            url.set("https://github.com/foshlabs/fosh-labs-kmp-navigation-kit")

            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }

            developers {
                developer {
                    id.set("foshlabs")
                    name.set("Fosh Labs")
                }
            }

            scm {
                url.set("https://github.com/foshlabs/fosh-labs-kmp-navigation-kit")
                connection.set("scm:git:git://github.com/foshlabs/fosh-labs-kmp-navigation-kit.git")
                developerConnection.set("scm:git:ssh://github.com/foshlabs/fosh-labs-kmp-navigation-kit.git")
            }
        }
    }

    repositories {
        maven {
            name = "mavenCentral"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME") ?: ""
                password = findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD") ?: ""
            }
        }
    }
}

signing {
    val signingKeyId = findProperty("signing.keyId") as String? ?: System.getenv("SIGNING_KEY_ID")
    val signingKey = findProperty("signing.key") as String? ?: System.getenv("SIGNING_KEY")
    val signingPassword = findProperty("signing.password") as String? ?: System.getenv("SIGNING_PASSWORD")

    if (signingKeyId != null && signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
        sign(publishing.publications)
    }
}
