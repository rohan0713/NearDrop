package com.rohan.neardrop.core.base

import com.rohan.neardrop.core.coroutines.CoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Marker interface for immutable UI state.
 */
interface UiState

/**
 * Marker interface for user actions or events dispatched to the ViewModel.
 */
interface UiIntent

/**
 * Marker interface for one-off side effects (e.g., navigation, toast notifications).
 */
interface UiEffect

/**
 * Base ViewModel implementing MVI (Model-View-Intent) pattern with strict OOP encapsulation.
 *
 * Encapsulates internal mutable state and channels while exposing read-only [StateFlow] and [Flow].
 * Platform-agnostic: Can be bound to Jetpack Compose on Android and SwiftUI on iOS.
 */
abstract class BaseViewModel<S : UiState, I : UiIntent, E : UiEffect>(
    initialState: S,
    protected val dispatchers: CoroutineDispatchers
) {

    private val job = SupervisorJob()
    protected val viewModelScope: CoroutineScope = CoroutineScope(job + dispatchers.main)

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    /**
     * Polymorphic handler for UI intents dispatched from native platforms.
     */
    abstract fun onIntent(intent: I)

    /**
     * Atomically updates the UI state.
     */
    protected fun updateState(reducer: S.() -> S) {
        _state.update { currentState -> currentState.reducer() }
    }

    /**
     * Dispatches a single-time side effect to the UI.
     */
    protected fun emitEffect(effect: E) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    /**
     * Cross-platform state watcher for native iOS (Swift) integration.
     */
    fun watchState(block: (S) -> Unit): Closeable {
        val watchJob = SupervisorJob()
        val scope = CoroutineScope(watchJob + dispatchers.main)
        scope.launch {
            state.collect { block(it) }
        }
        return Closeable { watchJob.cancel() }
    }

    /**
     * Cross-platform effect watcher for native iOS (Swift) integration.
     */
    fun watchEffect(block: (E) -> Unit): Closeable {
        val watchJob = SupervisorJob()
        val scope = CoroutineScope(watchJob + dispatchers.main)
        scope.launch {
            effect.collect { block(it) }
        }
        return Closeable { watchJob.cancel() }
    }

    /**
     * Cleans up coroutines when the ViewModel is destroyed.
     * Can be invoked from Android ViewModel onCleared or iOS deinit.
     */
    open fun onCleared() {
        job.cancel()
    }
}

/**
 * Functional interface allowing Swift to easily close observation subscriptions.
 */
fun interface Closeable {
    fun close()
}

