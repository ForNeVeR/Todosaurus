package me.fornever.todosaurus.ui.wizard

import com.intellij.openapi.project.Project

class TodosaurusWizardBuilder(private val project: Project) {
    private var wizardTitle: String? = null
    private var finalButtonName: String? = null
    private var finalAction: (suspend () -> WizardResult)? = null
    val steps: MutableList<TodosaurusStep> = mutableListOf()

    fun setTitle(title: String): TodosaurusWizardBuilder {
        if (title.isEmpty())
            return this

        wizardTitle = title

        return this
    }

    fun setFinalButtonName(name: String): TodosaurusWizardBuilder {
        if (name.isEmpty())
            return this

        finalButtonName = name

        return this
    }

    fun addStep(step: TodosaurusStep): TodosaurusWizardBuilder {
        if (steps.isNotEmpty()) {
            val lastOptionalStepProvider = steps.lastOrNull { it is OptionalStepProvider } as? OptionalStepProvider
            val previousStep = steps[steps.lastIndex]

            if (lastOptionalStepProvider == null) {
                step.previousId = previousStep.id
            }
            else {
                lastOptionalStepProvider
                    .optionalSteps
                    .forEach {
                        it.nextId = step.id
                    }
            }

            previousStep.nextId = step.id
        }

        steps.add(step)

        if (step is OptionalStepProvider) {
            step.optionalSteps
                .forEach {
                    it.previousId = step.id
                    steps.add(step)
                }
        }

        return this

        /*val isWizardEmpty = steps.isEmpty()

        steps.add(step)

        if (step is OptionalStepProvider) {
            step.nextId = step.id

            step.optionalSteps.forEach {
                it.previousId = step.id
                steps.add(it)
            }
        }

        if (isWizardEmpty)
            return this

        steps.filterIsInstance(OptionalStepProvider::class.java)
            .lastOrNull()
            ?.optionalSteps
            ?.forEach {
                it.nextId = step.id
            }

        steps.lastOrNull { it !is OptionalStepProvider }
            ?.let {
                it.nextId = step.id

                if (it.id != step.id)
                    step.previousId = it.id
            }

        return this*/
    }

    fun setFinalAction(action: suspend () -> WizardResult): TodosaurusWizardBuilder {
        finalAction = action

        return this
    }

    fun build(): TodosaurusWizard {
        val wizard = TodosaurusWizard(
            wizardTitle ?: error("Title is required for wizard"),
            project,
            steps,
            finalAction ?: error("Final action is required for wizard"))

        finalButtonName?.let {
            wizard.setFinalNameButton(it)
        }

        return wizard
    }
}
