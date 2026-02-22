# Fosh Labs KMP Navigation Kit

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

```kotlin
// In your shared module's build.gradle.kts
commonMain.dependencies {
    api("com.foshlabs.navigation:navigation-core:0.1.0")
}

// Export for iOS framework visibility
iosTarget.binaries.framework {
    export("com.foshlabs.navigation:navigation-core:0.1.0")
}

// In your Android app module's build.gradle.kts
androidMain.dependencies {
    implementation("com.foshlabs.navigation:navigation-compose:0.1.0")
}
```

Currently distributed via `mavenLocal()`. Add `mavenLocal()` to your `settings.gradle.kts` repositories and publish with:

```bash
./gradlew publishToMavenLocal
```

## Usage

### 1. Define your scenes

In your project's shared module, create a sealed interface extending `AppScene`:

```kotlin
import com.foshlabs.navigation.AppScene

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
import com.foshlabs.navigation.BaseViewModel
import com.foshlabs.navigation.NavigationState
import com.foshlabs.navigation.ViewModelState

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
import com.foshlabs.navigation.AppScene
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
import com.foshlabs.navigation.BaseViewModel
import com.foshlabs.navigation.ViewModelState
import com.foshlabs.navigation.compose.HandleNavigation as LibraryHandleNavigation

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
import com.foshlabs.navigation.compose.LocalNavigationManager
import com.foshlabs.navigation.compose.NavigationManager

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

Configured for Maven Central. Set credentials via `gradle.properties` or environment variables:

```properties
ossrhUsername=your-username
ossrhPassword=your-password
signing.keyId=your-key-id
signing.key=your-ascii-armored-key
signing.password=your-key-password
```
