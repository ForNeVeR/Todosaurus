package me.fornever.todosaurus.ui.wizard

interface DynamicStepProvider {
    fun createDynamicStep(): TodosaurusStep
}
