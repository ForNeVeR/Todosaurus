// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.settings.ui

import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.LabelPosition
import com.intellij.ui.dsl.builder.panel
import me.fornever.todosaurus.TodosaurusBundle
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
        // TODO[#136]: Add validation
        // TODO[#138]: Add the option to reset data saved with ‘Remember my choice’
        group(TodosaurusBundle.message("settings.patterns.title")) {
            row {
                textField()
                    .label(TodosaurusBundle.message("settings.patterns.issueNumber.title"), LabelPosition.TOP)
                    .comment(TodosaurusBundle.message("settings.patterns.issueNumber.description"))
                    .align(AlignX.FILL)
                    .enabled(false) // TODO[#134]: Allow to customize template for issue number. This is difficult task because the "newItemPattern" is now linked to a regular [.*?] pattern
                    .let { numberPatternField = it.component }
            }

            row {
                textArea()
                    .label(TodosaurusBundle.message("settings.patterns.descriptionTemplate.title"), LabelPosition.TOP)
                    .comment(TodosaurusBundle.message("settings.patterns.descriptionTemplate.description"))
                    .align(AlignX.FILL)
                    .let { descriptionTemplateField = it.component }
            }
            .resizableRow()
        }
    }
}
