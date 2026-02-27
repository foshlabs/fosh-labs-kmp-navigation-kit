# Fosh Labs KMP Navigation Kit

[![Maven Central](https://img.shields.io/maven-central/v/io.github.foshlabs.kmp.navigationkit/navigation-core)](https://central.sonatype.com/artifact/io.github.foshlabs.kmp.navigationkit/navigation-core)

A Kotlin Multiplatform navigation architecture library providing shared ViewModel-driven navigation for iOS and Android.

## Modules

### `navigation-core`
KMP module (commonMain + androidMain + iosMain) containing:
- `AppScene` — Marker interface for navigation destinations
- `NavigationState` — Sealed interface for navigation actions (Push, Pop, PresentSheet, etc.)
- `BaseViewModel` — Base ViewModel with state and navigation management
- `SceneRepository` — Pattern for passing data between scenes
- `UseCase`, `SuspendUseCase`, `FlowUseCase` — Base use case classes

### `navigation-compose`
Android-only module with Jetpack Compose navigation integration:
- `HandleNavigation` — Composable that observes ViewModel navigation events
- `NavigationManager` — Modal stack tracking for Android

## Installation

The library is published to [Maven Central](https://central.sonatype.com/artifact/io.github.foshlabs.kmp.navigationkit/navigation-core). Add the dependency to your project:

**Option 1: Kotlin DSL (`build.gradle.kts`)**

```kotlin
// In your shared KMP module's build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            api("io.github.foshlabs.kmp.navigationkit:navigation-core:0.1.0")
        }
        androidMain.dependencies {
            implementation("io.github.foshlabs.kmp.navigationkit:navigation-compose:0.1.0")
        }
    }
}
// For iOS: add export("io.github.foshlabs.kmp.navigationkit:navigation-core:0.1.0") to your framework block
```

**Option 2: Version catalog (`libs.versions.toml`)**

```toml
[versions]
foshlabs-navigation = "0.1.0"

[libraries]
foshlabs-navigation-core = { group = "io.github.foshlabs.kmp.navigationkit", name = "navigation-core", version.ref = "foshlabs-navigation" }
foshlabs-navigation-compose = { group = "io.github.foshlabs.kmp.navigationkit", name = "navigation-compose", version.ref = "foshlabs-navigation" }
```

```kotlin
// In your build.gradle.kts
commonMain.dependencies {
    api(libs.foshlabs.navigation.core)
}
androidMain.dependencies {
    implementation(libs.foshlabs.navigation.compose)
}
```

> Maven Central is included by default in most Gradle projects. If needed, ensure `mavenCentral()` is in your `repositories` block.

## Usage

### 1. Define your scenes

In your project's shared module, create a sealed interface extending `AppScene`:

```kotlin
import io.github.foshlabs.kmp.navigationkit.AppScene

sealed interface MyProjectScene : AppScene {
    data object Home : MyProjectScene
    data object Settings : MyProjectScene
    data object Onboarding : MyProjectScene
    data object Paywall : MyProjectScene
}
```

### 2. Navigate from ViewModels

ViewModels extend `BaseViewModel` and call `navigate()` with `NavigationState` actions:

```kotlin
import io.github.foshlabs.kmp.navigationkit.BaseViewModel
import io.github.foshlabs.kmp.navigationkit.NavigationState
import io.github.foshlabs.kmp.navigationkit.ViewModelState

class HomeViewModel : BaseViewModel<HomeViewModel.State>(State()) {

    data class State(
        val title: String = "Home"
    ) : ViewModelState

    fun onTapSettings() {
        navigate(NavigationState.Push(MyProjectScene.Settings))
    }

    fun onTapPaywall() {
        navigate(NavigationState.PresentSheet(MyProjectScene.Paywall))
    }
}
```

Available navigation actions:
- `NavigationState.Push(destination)` — Push onto the navigation stack
- `NavigationState.Pop` — Pop the current screen
- `NavigationState.PopToRoot` — Pop to the root of the current context
- `NavigationState.PopTo(destination, inclusive)` — Pop to a specific destination
- `NavigationState.PresentSheet(destination)` — Present as a sheet/modal
- `NavigationState.PresentFullScreen(destination)` — Present as a full-screen modal
- `NavigationState.Dismiss` — Dismiss the current modal (pops entire modal stack)
- `NavigationState.ReplaceRoot(destination)` — Replace the entire navigation stack

### 3. Android — Map scenes to Compose destinations

Create `@Serializable` route objects and a mapper function:

```kotlin
import io.github.foshlabs.kmp.navigationkit.AppScene
import kotlinx.serialization.Serializable

@Serializable object HomeRoute
@Serializable object SettingsRoute
@Serializable object OnboardingRoute
@Serializable object PaywallRoute

fun mapToDestination(scene: AppScene): Any {
    val myScene = scene as MyProjectScene
    return when (myScene) {
        MyProjectScene.Home -> HomeRoute
        MyProjectScene.Settings -> SettingsRoute
        MyProjectScene.Onboarding -> OnboardingRoute
        MyProjectScene.Paywall -> PaywallRoute
    }
}
```

### 4. Android — Create a convenience wrapper

Wrap the library's `HandleNavigation` with your scene mapper so call sites stay clean:

```kotlin
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import io.github.foshlabs.kmp.navigationkit.BaseViewModel
import io.github.foshlabs.kmp.navigationkit.ViewModelState
import io.github.foshlabs.kmp.navigationkit.compose.HandleNavigation as LibraryHandleNavigation

@Composable
fun <S : ViewModelState> HandleNavigation(
    viewModel: BaseViewModel<S>,
    navController: NavController
) {
    LibraryHandleNavigation(
        viewModel = viewModel,
        navController = navController,
        sceneMapper = { scene -> mapToDestination(scene) }
    )
}
```

### 5. Android — Set up the NavHost

Provide `NavigationManager` via `CompositionLocalProvider` and use `HandleNavigation` in each screen:

```kotlin
import io.github.foshlabs.kmp.navigationkit.compose.LocalNavigationManager
import io.github.foshlabs.kmp.navigationkit.compose.NavigationManager

@Composable
fun App() {
    val navController = rememberNavController()

    CompositionLocalProvider(LocalNavigationManager provides NavigationManager()) {
        NavHost(navController = navController, startDestination = HomeRoute) {
            composable<HomeRoute> {
                val viewModel: HomeViewModel = koinViewModel()
                HandleNavigation(viewModel, navController)
                HomeScreen(viewModel)
            }
            composable<SettingsRoute> {
                val viewModel: SettingsViewModel = koinViewModel()
                HandleNavigation(viewModel, navController)
                SettingsScreen(viewModel)
            }
            // ... more destinations
        }
    }
}
```

### iOS companion package

See [fosh-labs-kmp-navigation-kit-ios](https://github.com/foshlabs/fosh-labs-kmp-navigation-kit-ios) for the SwiftUI navigation components.

## Dependencies

| Dependency | Version |
|---|---|
| Kotlin | 2.2.0 |
| KMP Observable ViewModel | 1.0.0-BETA-7 |
| KMP Native Coroutines | 1.0.0-ALPHA-45 |
| Koin | 4.0.0 |
| AndroidX Navigation Compose | 2.8.3 |

## Publishing

Publishing to Maven Central is configured. Credentials are loaded from `gradle-secrets.properties` (gitignored).

**Important:** The vanniktech plugin reads `mavenCentralUsername`/`mavenCentralPassword` via Gradle properties (not from `ext`). Use the wrapper script:

```bash
./publishToMavenCentral.sh
```

This reads `ossrhUsername`/`ossrhPassword` from `gradle-secrets.properties` and passes them to Gradle. Alternatively, run with `-PmavenCentralUsername=… -PmavenCentralPassword=…` or set `ORG_GRADLE_PROJECT_mavenCentralUsername` and `ORG_GRADLE_PROJECT_mavenCentralPassword` env vars.
