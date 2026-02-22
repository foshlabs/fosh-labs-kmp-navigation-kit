package com.foshlabs.navigation

/**
 * Marker interface for navigation destinations.
 *
 * Consuming projects should define their own sealed interface extending this:
 * ```
 * sealed interface AppScene : Scene {
 *     data object Home : AppScene
 *     data object Settings : AppScene
 * }
 * ```
 */
interface Scene
