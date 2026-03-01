package io.github.foshlabs.kmp.navigationkit

import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import io.github.foshlabs.kmp.architecturekit.BaseViewModel
import io.github.foshlabs.kmp.architecturekit.ViewModelState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class NavigationViewModel<S : ViewModelState>(initialState: S) : BaseViewModel<S>(initialState) {

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
