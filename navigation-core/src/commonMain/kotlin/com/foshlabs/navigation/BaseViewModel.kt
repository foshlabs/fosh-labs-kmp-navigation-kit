package com.foshlabs.navigation

import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel<S : ViewModelState>(initialState: S): ViewModel() {

    // MARK: - State

    private val _state = MutableStateFlow(
        viewModelScope = viewModelScope,
        value = initialState
    )

    protected var state: S
        get() = _state.value
        set(value) { _state.value = value }

    @NativeCoroutinesState
    val states: StateFlow<S> = _state.asStateFlow()

    // MARK: - Navigation

    private val _navigationState = MutableStateFlow<NavigationState>(
        viewModelScope = viewModelScope,
        value = NavigationState.None
    )

    protected fun navigate(value: NavigationState) {
        _navigationState.value = value
    }

    fun consumeNavigation() {
        _navigationState.value = NavigationState.None
    }

    @NativeCoroutinesState
    val navigationStates: StateFlow<NavigationState> = _navigationState.asStateFlow()
}

interface ViewModelState
