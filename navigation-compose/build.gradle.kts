import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.vanniktechMavenPublish)
}

group = "io.github.foshlabs.kmp.navigationkit"
version = "0.1.0"

kotlin {
    androidLibrary {
        namespace = "io.github.foshlabs.kmp.navigationkit.compose"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(project(":navigation-core"))
            implementation(libs.androidx.navigation.compose)
            implementation(compose.runtime)
            implementation(compose.foundation)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates("io.github.foshlabs.kmp.navigationkit", "navigation-compose", version.toString())
    pom {
        name.set("Fosh Labs Navigation Compose")
        description.set("Jetpack Compose navigation integration for Fosh Labs Navigation Kit")
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
