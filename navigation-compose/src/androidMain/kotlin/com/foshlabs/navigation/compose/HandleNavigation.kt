package com.foshlabs.navigation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import com.foshlabs.navigation.BaseViewModel
import com.foshlabs.navigation.NavigationState
import com.foshlabs.navigation.Scene
import com.foshlabs.navigation.ViewModelState
import kotlinx.coroutines.flow.filter

/**
 * Composable function that handles navigation events from a BaseViewModel.
 *
 * @param viewModel The ViewModel emitting navigation events
 * @param navController The NavController to perform navigation on
 * @param sceneMapper Maps a [Scene] to a navigation destination object (e.g., a @Serializable route)
 */
@Composable
fun <S : ViewModelState> HandleNavigation(
    viewModel: BaseViewModel<S>,
    navController: NavController,
    sceneMapper: (Scene) -> Any
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
 * Modal behavior (matching iOS):
 * - PresentFullScreen/PresentSheet: Marks the current position as a modal entry point
 * - Push/Pop: Regular navigation within the current context
 * - Dismiss: Pops the entire modal stack back to the entry point
 */
private fun processNavigationState(
    navigationState: NavigationState,
    navController: NavController,
    navigationManager: NavigationManager,
    sceneMapper: (Scene) -> Any
) {
    when (navigationState) {
        is NavigationState.ReplaceRoot -> {
            navigationManager.clearModalStack()

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
            val destination = sceneMapper(navigationState.destination)
            navController.navigate(destination)
        }

        is NavigationState.Pop -> {
            navController.popBackStack()
        }

        is NavigationState.PopToRoot -> {
            if (navigationManager.isInModalContext) {
                navigationManager.onModalDismissed(navController)
            } else {
                val startDestination = navController.graph.startDestinationRoute
                if (startDestination != null) {
                    navController.popBackStack(startDestination, false)
                }
            }
        }

        is NavigationState.PopTo -> {
            val destination = navigationState.destination?.let { sceneMapper(it) }
            if (destination != null) {
                navController.popBackStack(destination, navigationState.inclusive)
            } else if (navigationState.inclusive) {
                val startDestination = navController.graph.startDestinationRoute
                if (startDestination != null) {
                    navController.popBackStack(startDestination, false)
                }
            }
        }

        is NavigationState.PresentSheet -> {
            navigationManager.onModalPresented(navController)
            val destination = sceneMapper(navigationState.destination)
            navController.navigate(destination)
        }

        is NavigationState.PresentFullScreen -> {
            navigationManager.onModalPresented(navController)
            val destination = sceneMapper(navigationState.destination)
            navController.navigate(destination)
        }

        is NavigationState.Dismiss -> {
            if (!navigationManager.onModalDismissed(navController)) {
                navController.popBackStack()
            }
        }

        NavigationState.None -> {
            // No action needed
        }
    }
}
