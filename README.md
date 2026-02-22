# Fosh Labs KMP Navigation Kit

A Kotlin Multiplatform navigation architecture library providing shared ViewModel-driven navigation for iOS and Android.

## Modules

### `navigation-core`
KMP module (commonMain + androidMain + iosMain) containing:
- `Scene` — Marker interface for navigation destinations
- `NavigationState` — Sealed interface for navigation actions (Push, Pop, PresentSheet, etc.)
- `BaseViewModel` — Base ViewModel with state and navigation management
- `SceneRepository` — Pattern for passing data between scenes
- `UseCase`, `SuspendUseCase`, `FlowUseCase` — Base use case classes

### `navigation-compose`
Android-only module with Jetpack Compose navigation integration:
- `HandleNavigation` — Composable that observes ViewModel navigation events
- `NavigationManager` — Modal stack tracking for Android

## Usage

### Gradle (Android / KMP shared module)

```kotlin
// In your shared module
dependencies {
    api("com.foshlabs.navigation:navigation-core:0.1.0")
}

// In your Android app module
dependencies {
    implementation("com.foshlabs.navigation:navigation-compose:0.1.0")
}
```

### Define your scenes

```kotlin
// In your project's shared module
sealed interface AppScene : Scene {
    data object Home : AppScene
    data object Settings : AppScene
    data object Onboarding : AppScene
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
