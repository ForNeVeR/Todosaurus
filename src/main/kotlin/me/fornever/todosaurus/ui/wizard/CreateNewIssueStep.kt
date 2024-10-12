package me.fornever.todosaurus.ui.wizard

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import javax.swing.JComponent

class CreateNewIssueStep(private val project: Project, private val model: TodosaurusContext) : TodosaurusStep() {
    companion object {
        val id: Any = CreateNewIssueStep::class.java
    }

    override val id: Any = Companion.id

    private lateinit var titleField: JBTextField
    private lateinit var descriptionField: JBTextArea

    override fun getComponent(): JComponent = panel {
        row {
            titleField = textField()
                .label("Issue title:", LabelPosition.TOP)
                .align(AlignX.FILL)
                .text(model.toDoItem.title)
                .component
        }

        row {
            descriptionField = textArea()
                .label("Description:", LabelPosition.TOP)
                .align(Align.FILL)
                .text(model.toDoItem.description)
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