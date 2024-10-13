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
        if (steps.isNotEmpty() && step !is EmptyStep) {
            val previousStep = steps[steps.lastIndex]
            step.previousId = previousStep.id
            previousStep.nextId = step.id
        }

        steps.add(step)

        if (step is DynamicStepProvider) {
            step.nextId = step.id
        }

        return this
    }

    fun setFinalAction(action: suspend () -> WizardResult): TodosaurusWizardBuilder {
        finalAction = action

        return this
    }

    fun build(): TodosaurusWizard {
        if (steps.isEmpty())
            error("Steps is required for wizard")

        val wizard = TodosaurusWizard(
            wizardTitle ?: error("Title is required for wizard"),
            project,
            finalAction ?: error("Final action is required for wizard"))

        finalButtonName?.let {
            wizard.nextButtonName = it
        }

        if (steps.size == 1 && steps[0] is DynamicStepProvider) {
            // Dirty hack to prevent removing Previous button for dynamic steps
            addStep(EmptyStep())
        }

        steps.forEach {
            wizard.addStep(it)
        }

        return wizard
    }
}
