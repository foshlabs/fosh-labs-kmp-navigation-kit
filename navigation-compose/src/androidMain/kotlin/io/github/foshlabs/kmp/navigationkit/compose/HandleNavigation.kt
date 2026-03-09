package io.github.foshlabs.kmp.navigationkit.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import io.github.foshlabs.kmp.navigationkit.NavigationViewModel
import io.github.foshlabs.kmp.navigationkit.NavigationState
import io.github.foshlabs.kmp.navigationkit.AppScene
import io.github.foshlabs.kmp.architecturekit.ViewModelState
import kotlinx.coroutines.flow.filter

/**
 * Composable function that handles navigation events from a NavigationViewModel.
 *
 * @param viewModel The ViewModel emitting navigation events
 * @param navController The NavController to perform navigation on
 * @param sceneMapper Maps an [AppScene] to a navigation destination object (e.g., a @Serializable route)
 */
@Composable
fun <S : ViewModelState> HandleNavigation(
    viewModel: NavigationViewModel<S>,
    navController: NavController,
    sceneMapper: (AppScene) -> Any
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val navigationManager = LocalNavigationManager.current

    LaunchedEffect(viewModel, lifecycleOwner) {
        viewModel.navigationStates
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .filter { it !is NavigationState.None }
            .collect { navigationState ->
                processNavigationState(navigationState, navController, navigationManager, sceneMapper)
                viewModel.consumeNavigation()
            }
    }
}

/**
 * Process a navigation state and perform the corresponding navigation action.
 *
 * Sets the appropriate [NavigationTransitionType] on the [NavigationManager] before
 * each navigation call so that NavHost transition lambdas can read it to determine
 * the correct animation.
 *
 * Modal behavior (matching iOS):
 * - PresentFullScreen/PresentSheet: Marks the current position as a modal entry point
 * - Push/Pop: Regular navigation within the current context
 * - Dismiss: Pops the entire modal stack back to the entry point
 */
private fun processNavigationState(
    navigationState: NavigationState,
    navController: NavController,
    navigationManager: NavigationManager,
    sceneMapper: (AppScene) -> Any
) {
    when (navigationState) {
        is NavigationState.ReplaceRoot -> {
            navigationManager.clearModalStack()
            navigationManager.clearTransitionHistory()
            navigationManager.pushTransition(NavigationTransitionType.Replace)

            val destination = sceneMapper(navigationState.destination)
            val startDestination = navController.graph.startDestinationRoute
            if (startDestination != null) {
                navController.popBackStack(startDestination, true)
            } else {
                while (navController.popBackStack()) { /* pop all */ }
            }
            navController.navigate(destination)
        }

        is NavigationState.Push -> {
            navigationManager.pushTransition(NavigationTransitionType.Push)
            val destination = sceneMapper(navigationState.destination)
            navController.navigate(destination)
        }

        is NavigationState.Pop -> {
            navigationManager.popTransition()
            navController.popBackStack()
        }

        is NavigationState.PopToRoot -> {
            if (navigationManager.isInModalContext) {
                val historySize = navigationManager.transitionHistorySize
                navigationManager.onModalDismissed(navController)
                navigationManager.popTransitionsTo(0)
            } else {
                navigationManager.popTransitionsTo(0)
                val startDestination = navController.graph.startDestinationRoute
                if (startDestination != null) {
                    navController.popBackStack(startDestination, false)
                }
            }
        }

        is NavigationState.PopTo -> {
            val destination = navigationState.destination?.let { sceneMapper(it) }
            if (destination != null) {
                // Pop transition for the current screen
                navigationManager.popTransition()
                navController.popBackStack(destination, navigationState.inclusive)
            } else if (navigationState.inclusive) {
                navigationManager.popTransitionsTo(0)
                val startDestination = navController.graph.startDestinationRoute
                if (startDestination != null) {
                    navController.popBackStack(startDestination, false)
                }
            }
        }

        is NavigationState.PresentSheet -> {
            navigationManager.onModalPresented(navController)
            navigationManager.pushTransition(NavigationTransitionType.Modal)
            val destination = sceneMapper(navigationState.destination)
            navController.navigate(destination)
        }

        is NavigationState.PresentFullScreen -> {
            navigationManager.onModalPresented(navController)
            navigationManager.pushTransition(NavigationTransitionType.Modal)
            val destination = sceneMapper(navigationState.destination)
            navController.navigate(destination)
        }

        is NavigationState.Dismiss -> {
            val historySize = navigationManager.transitionHistorySize
            if (!navigationManager.onModalDismissed(navController)) {
                navigationManager.popTransition()
                navController.popBackStack()
            } else {
                // Pop all transitions that were part of the dismissed modal
                val newBackStackSize = navController.currentBackStack.value.size
                navigationManager.popTransitionsTo(newBackStackSize.coerceAtLeast(0))
            }
        }

        NavigationState.None -> {
            // No action needed
        }
    }
}
