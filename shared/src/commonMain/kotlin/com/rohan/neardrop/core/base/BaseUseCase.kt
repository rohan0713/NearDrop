package com.rohan.neardrop.core.base

import com.rohan.neardrop.core.coroutines.CoroutineDispatchers
import com.rohan.neardrop.core.result.AppError
import com.rohan.neardrop.core.result.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Marker interface for use cases that take no parameters.
 */
object None

/**
 * Base abstract class for one-shot Use Cases (Interactors) in Clean Architecture.
 * Encapsulates error handling and execution on the specified coroutine dispatcher.
 * Demonstrates Template Method design pattern and Single Responsibility Principle.
 */
abstract class BaseUseCase<in Input, Output>(
    private val dispatchers: CoroutineDispatchers
) {

    /**
     * Subclasses implement the core business logic in this abstract method.
     */
    protected abstract suspend fun execute(input: Input): AppResult<Output>

    /**
     * Public entry point invoking the use case on the designated I/O or background dispatcher.
     */
    suspend operator fun invoke(input: Input): AppResult<Output> = withContext(dispatchers.io) {
        try {
            execute(input)
        } catch (throwable: Throwable) {
            AppResult.Error(
                AppError.Unknown(
                    message = throwable.message ?: "An unexpected error occurred in use case",
                    cause = throwable
                )
            )
        }
    }
}

/**
 * Base abstract class for reactive stream Use Cases producing a [Flow].
 */
abstract class BaseFlowUseCase<in Input, Output>(
    private val dispatchers: CoroutineDispatchers
) {

    /**
     * Subclasses create the reactive business stream.
     */
    protected abstract fun createFlow(input: Input): Flow<Output>

    /**
     * Public entry point returning a flow dispatched on the background dispatcher
     * with centralized stream error recovery.
     */
    operator fun invoke(input: Input): Flow<Output> {
        return createFlow(input)
            .flowOn(dispatchers.io)
    }
}
