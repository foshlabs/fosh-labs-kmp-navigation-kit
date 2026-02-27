package io.github.foshlabs.kmp.navigationkit

/**
 * Navigation states for handling different types of navigation in the app.
 *
 * Usage examples:
 * - Push: navigate(NavigationState.Push(AppScene.Onboarding))
 * - Present Sheet: navigate(NavigationState.PresentSheet(AppScene.Settings))
 * - Replace Root: navigate(NavigationState.ReplaceRoot(AppScene.TabBar))
 */
sealed interface NavigationState {
    data object None : NavigationState
    data class ReplaceRoot(val destination: AppScene) : NavigationState

    data class PresentSheet(val destination: AppScene) : NavigationState
    data class PresentFullScreen(val destination: AppScene) : NavigationState
    data object Dismiss : NavigationState

    data class Push(val destination: AppScene) : NavigationState
    data class PopTo(val destination: AppScene?, val inclusive: Boolean = false) : NavigationState
    data object Pop : NavigationState
    data object PopToRoot : NavigationState
}
