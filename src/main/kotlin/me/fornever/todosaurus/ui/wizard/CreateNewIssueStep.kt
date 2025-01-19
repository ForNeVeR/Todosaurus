// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.ui.wizard

import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import me.fornever.todosaurus.TodosaurusBundle
import javax.swing.JComponent

class CreateNewIssueStep(private val model: TodosaurusWizardContext) : TodosaurusWizardStep() {

    override val id: String = CreateNewIssueStep::class.java.name

    private lateinit var titleField: JBTextField
    private lateinit var descriptionField: JBTextArea

    override fun getComponent(): JComponent = panel {
        row {
            titleField = textField()
                .label(TodosaurusBundle.getMessage("wizard.steps.createNewIssue.title"), LabelPosition.TOP)
                .align(AlignX.FILL)
                .text(model.toDoItem.title)
                .onChanged { // For some reason bindText({ model.toDoItem.title }, { model.toDoItem.title = it }) function is not working :(
                    model.toDoItem.title = it.text
                }
                .component
        }

        row {
            descriptionField = textArea()
                .label(TodosaurusBundle.getMessage("wizard.steps.createNewIssue.description"), LabelPosition.TOP)
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
}
