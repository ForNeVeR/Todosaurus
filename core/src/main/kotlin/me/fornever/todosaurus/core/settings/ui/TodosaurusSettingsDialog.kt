// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.settings.ui

import com.intellij.ide.impl.ProjectUtil
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import me.fornever.todosaurus.TodosaurusBundle
import me.fornever.todosaurus.ui.wizard.memoization.UserChoiceStore
import javax.swing.JButton
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
        group(TodosaurusBundle.message("settings.common.title")) {
            row {
                panel {
                    row {
                        label(TodosaurusBundle.message("settings.common.userChoice.title"))
                            .comment(TodosaurusBundle.message("settings.common.userChoice.description"))
                    }
                }

                forgetButton()
                    .align(AlignX.RIGHT)
            }
        }

        group(TodosaurusBundle.message("settings.patterns.title")) {
            row {
                textField()
                    .also { it.enabled(false) }
                    .label(TodosaurusBundle.message("settings.patterns.issueNumber.title"), LabelPosition.TOP)
                    .comment(TodosaurusBundle.message("settings.patterns.issueNumber.description"))
                    .align(AlignX.FILL) // TODO[#134]: Allow to customize template for issue number. This is difficult task because the "newItemPattern" is now linked to a regular [.*?] pattern
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

    private fun Row.forgetButton(): Cell<JButton> {
        val forgetTitle = TodosaurusBundle.message("settings.common.userChoice.forget.title")
        val activeProject = ProjectUtil.getActiveProject()

        if (activeProject == null || activeProject.isDefault)
            return button(forgetTitle) { }
                .enabled(false)
                .apply {
                    component.toolTipText = TodosaurusBundle.message("settings.common.userChoice.forget.tooltip")
                }

        val choiceStore = UserChoiceStore.getInstance(activeProject)

        return button(forgetTitle) {
            val button = it.source as? JButton
                ?: return@button

            choiceStore.forgetChoice()
            button.isEnabled = false
        }
        .enabled(choiceStore.getChoiceOrNull() != null)
    }
}
