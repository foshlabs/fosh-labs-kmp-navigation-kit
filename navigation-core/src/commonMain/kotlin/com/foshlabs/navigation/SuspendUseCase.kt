package com.foshlabs.navigation

expect abstract class SuspendUseCase<in Input : Any, out Output : Any> constructor() {
    abstract suspend operator fun invoke(input: Input): Output
}

expect abstract class UnitSuspendUseCase<out Output : Any> constructor() {
    abstract suspend operator fun invoke(): Output
}
