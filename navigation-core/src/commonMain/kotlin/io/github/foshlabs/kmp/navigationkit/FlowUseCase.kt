package io.github.foshlabs.kmp.navigationkit

import kotlinx.coroutines.flow.Flow

expect abstract class FlowUseCase<in Input : Any, out Output : Any> constructor() {
    abstract operator fun invoke(input: Input): Flow<Output>
}

expect abstract class UnitFlowUseCase<out Output : Any> constructor() {
    abstract operator fun invoke(): Flow<Output>
}
