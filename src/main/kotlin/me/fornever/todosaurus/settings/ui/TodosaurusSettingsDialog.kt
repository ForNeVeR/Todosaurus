// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.settings.ui

import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.LabelPosition
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class TodosaurusSettingsDialog {
    private lateinit var numberPatternField: JBTextField
    private lateinit var descriptionTemplateField: JBTextArea

    var numberPattern: String
        get() = numberPatternField.text
        set(value) {
            numberPatternField.text = value
        }

    var descriptionTemplate: String
        get() = descriptionTemplateField.text
        set(value) {
            descriptionTemplateField.text = value
        }

    fun createPanel(): JComponent = panel {
        // TODO: Move all text to TodosaurusBundle.properties
        // TODO: Add validation
        group("Patterns") {
            row {
                textField()
                    .label("Issue number pattern:", LabelPosition.TOP)
                    .comment("This pattern is used when inserting a number into the TODO after creating a task in the issue tracker")
                    .align(AlignX.FILL)
                    .let { numberPatternField = it.component }
            }

            row {
                textArea()
                    .label("Issue description template:", LabelPosition.TOP)
                    .comment("This template is used as the initial text when creating a new issue")
                    .align(AlignX.FILL)
                    .let { descriptionTemplateField = it.component }
            }
            .resizableRow()
        }
    }
}
