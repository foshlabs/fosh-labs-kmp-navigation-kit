package io.github.foshlabs.kmp.navigationkit.compose

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavController

/**
 * Navigation manager that tracks modal navigation stacks.
 *
 * This mirrors the iOS ModalNavigationManager behavior where:
 * - PresentFullScreen/PresentSheet creates a new modal context
 * - Push/Pop within a modal operates within that modal's stack
 * - Dismiss pops the entire modal stack back to the entry point
 */
class NavigationManager {

    // MARK: - Properties

    /**
     * Stack of back stack entry IDs that mark the start of modal presentations.
     * When a modal is presented, the current back stack entry ID is pushed here.
     * When Dismiss is called, we pop back to the entry at the top of this stack.
     */
    private val modalEntryStack = mutableListOf<String>()

    /**
     * Whether we're currently inside a modal context.
     */
    val isInModalContext: Boolean
        get() = modalEntryStack.isNotEmpty()

    // MARK: - Actions

    /**
     * Called when presenting a modal (PresentFullScreen or PresentSheet).
     * Saves the current back stack entry so we can dismiss back to it later.
     */
    fun onModalPresented(navController: NavController) {
        val currentEntry = navController.currentBackStackEntry
        val entryId = currentEntry?.id
        if (entryId != null) {
            modalEntryStack.add(entryId)
        }
    }

    /**
     * Called when dismissing a modal.
     * Pops the entire modal stack back to the entry point.
     *
     * @return true if a modal was dismissed, false if there was no modal to dismiss
     */
    fun onModalDismissed(navController: NavController): Boolean {
        if (modalEntryStack.isEmpty()) {
            return false
        }

        val modalEntryId = modalEntryStack.removeAt(modalEntryStack.lastIndex)

        val backQueue = navController.currentBackStack.value
        val targetEntry = backQueue.find { it.id == modalEntryId }

        if (targetEntry != null) {
            val route = targetEntry.destination.route
            if (route != null) {
                navController.popBackStack(route, inclusive = false)
                return true
            }
        }

        navController.popBackStack()
        return true
    }

    /**
     * Clears all modal tracking. Should be called when replacing root.
     */
    fun clearModalStack() {
        modalEntryStack.clear()
    }
}

/**
 * CompositionLocal for providing NavigationManager throughout the app.
 */
val LocalNavigationManager = compositionLocalOf { NavigationManager() }
