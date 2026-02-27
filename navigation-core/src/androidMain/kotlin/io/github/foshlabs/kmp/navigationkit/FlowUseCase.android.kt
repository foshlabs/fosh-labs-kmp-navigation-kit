package io.github.foshlabs.kmp.navigationkit

import kotlinx.coroutines.flow.Flow

actual abstract class FlowUseCase<in Input : Any, out Output : Any> {
    actual abstract operator fun invoke(input: Input): Flow<Output>
}

actual abstract class UnitFlowUseCase<out Output : Any> {
    actual abstract operator fun invoke(): Flow<Output>
}
