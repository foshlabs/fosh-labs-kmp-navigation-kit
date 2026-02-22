package com.foshlabs.navigation

abstract class UseCase<Input, Output> {
    abstract operator fun invoke(input: Input): Output
}

abstract class UnitUseCase<Output> {
    abstract operator fun invoke(): Output
}
