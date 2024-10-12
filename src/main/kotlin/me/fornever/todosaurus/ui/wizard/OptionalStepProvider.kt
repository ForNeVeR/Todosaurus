package me.fornever.todosaurus.ui.wizard

interface OptionalStepProvider {
    val optionalSteps: MutableList<TodosaurusStep>

    fun chooseOptionalStepId(): Any
}
