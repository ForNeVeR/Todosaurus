// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.ui.wizard

import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope

class TodosaurusWizardBuilder(private val project: Project, private val model: TodosaurusWizardContext, private val scope: CoroutineScope) {
    private var wizardTitle: String? = null
    private var finalButtonName: String? = null
    private var finalAction: (suspend () -> WizardResult)? = null
    private val steps: MutableList<TodosaurusWizardStep> = mutableListOf()

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

    fun addStep(step: TodosaurusWizardStep): TodosaurusWizardBuilder {
        if (steps.isNotEmpty()) {
            val previousStep = steps[steps.lastIndex]
            step.previousId = previousStep.id
            previousStep.nextId = step.id
        }

        steps.add(step)

        if (step is DynamicStepProvider)
            step.nextId = step.id

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
            scope,
            model,
            finalAction ?: error("Final action is required for wizard"))

        finalButtonName?.let {
            wizard.nextButtonName = it
        }

        steps.forEach {
            wizard.addStep(it)
        }

        return wizard
    }
}
