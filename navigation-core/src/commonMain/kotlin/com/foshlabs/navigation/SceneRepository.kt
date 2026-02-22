package com.foshlabs.navigation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface SceneRepository<T : AppScene> {
    fun storeScene(scene: T)
    fun observeScene(): Flow<T?>
    fun getScene(): T?
    fun clearScene()
}

abstract class SceneRepositoryImpl<S : AppScene> : SceneRepository<S> {
    private var scene: S? = null
    private val sceneFlow = MutableStateFlow<S?>(null)

    override fun storeScene(scene: S) {
        this.scene = scene
        sceneFlow.value = scene
    }

    override fun getScene(): S? {
        return scene
    }

    override fun observeScene(): Flow<S?> = sceneFlow

    override fun clearScene() {
        this.scene = null
        sceneFlow.value = null
    }
}
