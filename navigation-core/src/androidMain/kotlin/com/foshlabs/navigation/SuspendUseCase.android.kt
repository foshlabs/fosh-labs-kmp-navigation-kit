package com.foshlabs.navigation

actual abstract class SuspendUseCase<in Input : Any, out Output : Any> {
    actual abstract suspend operator fun invoke(input: Input): Output
}

actual abstract class UnitSuspendUseCase<out Output : Any> {
    actual abstract suspend operator fun invoke(): Output
}
