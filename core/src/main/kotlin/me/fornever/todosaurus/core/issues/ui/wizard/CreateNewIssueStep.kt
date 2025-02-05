// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT
package me.fornever.todosaurus.core.issues.ui.wizard

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import me.fornever.todosaurus.core.TodosaurusCoreBundle
import me.fornever.todosaurus.core.issues.ToDoItem
import me.fornever.todosaurus.core.issues.ToDoService
import me.fornever.todosaurus.core.ui.wizard.TodosaurusWizardContext
import me.fornever.todosaurus.core.ui.wizard.TodosaurusWizardStep
import me.fornever.todosaurus.core.ui.wizard.memoization.ForgettableStep
import me.fornever.todosaurus.core.ui.wizard.memoization.UserChoiceStore
import javax.swing.JComponent

class CreateNewIssueStep(private val project: Project, private val model: TodosaurusWizardContext<ToDoItem.New>) : TodosaurusWizardStep(), ForgettableStep {

    override val id: String = CreateNewIssueStep::class.java.name

    private lateinit var titleField: JBTextField
    private lateinit var descriptionField: JBTextArea

    override fun getComponent(): JComponent = panel {
        row {
            titleField = textField()
                .label(TodosaurusCoreBundle.getMessage("wizard.steps.createNewIssue.title"), LabelPosition.TOP)
                .align(AlignX.FILL)
                .text(model.toDoItem.title)
                .onChanged {
                    model.toDoItem.title = it.text
                }
                .component
        }

        row {
            descriptionField = textArea()
                .label(TodosaurusCoreBundle.getMessage("wizard.steps.createNewIssue.description"), LabelPosition.TOP)
                .align(Align.FILL)
                .text(model.toDoItem.description)
                .onChanged {
                    model.toDoItem.description = it.text
                }
                .component
                .also {
                    it.lineWrap = true
                }
        }
        .resizableRow()
    }

    override fun getPreferredFocusedComponent(): JComponent
        = titleField

    override fun isComplete(): Boolean
        = true

    override fun forgetUserChoice() {
        UserChoiceStore.getInstance(project).forgetChoice()
        ToDoService.getInstance(project).createNewIssue(model.toDoItem)
    }
}
