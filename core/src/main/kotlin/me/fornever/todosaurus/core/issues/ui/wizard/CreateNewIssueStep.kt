// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issues.ui.wizard

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.ScrollPaneFactory
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
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

@Suppress("UnstableApiUsage")
class CreateNewIssueStep(
    private val project: Project,
    private val model: TodosaurusWizardContext<ToDoItem.New>) : TodosaurusWizardStep(), ForgettableStep {

    override val id: String = CreateNewIssueStep::class.java.name

    private lateinit var titleField: JBTextField
    private lateinit var descriptionField: JBTextArea

    private var issueTrackerId: String? = null
    private var optionsHolder: Placeholder? = null

    override fun _init() {
        super._init()

        val issueTracker = model.connectionDetails.issueTracker
            ?: return

        if (issueTrackerId == issueTracker.id) {
            model.issueOptions.map { it.refresh() }
        }
        else {
            recreateIssueOptions()
        }

        issueTrackerId = issueTracker.id
    }

    private val component: DialogPanel = panel {
        row {
            optionsHolder = placeholder()
                .align(Align.FILL)
        }

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
                    it.wrapStyleWord = true
                }
        }
        .resizableRow()
    }

    private fun recreateIssueOptions() {
        val currentHolder = optionsHolder ?: return

        val issueOptions = IssueOptionsProvider.provideAll(project, model)

        currentHolder.component = panel {
            if (issueOptions.isNotEmpty())
                issueOptions.map { component -> createOptionsRow(component.createOptionsPanel()) }
        }

        model.issueOptions = issueOptions
    }

    override fun createComponent(): JComponent
        = ScrollPaneFactory.createScrollPane(component, true)

    // Copied from https://github.com/JetBrains/intellij-community/blob/dc6fdfc676b0dcc191611482c2907be241f181ae/platform/vcs-impl/src/com/intellij/vcs/commit/CommitOptionsPanel.kt#L110
    // SPDX-SnippetBegin
    // SPDX-SnippetCopyrightText: 2000-2019 JetBrains s.r.o.
    // SPDX-License-Identifier: Apache-2.0
    private fun Panel.createOptionsRow(component: JComponent): Row {
        val meaningfulComponent = extractMeaningfulComponent(component)

        return row {
            cell(meaningfulComponent ?: component)
                .align(Align.FILL)
        }
        .bottomGap(BottomGap.MEDIUM)
    }

    // Copied from https://github.com/JetBrains/intellij-community/blob/dc6fdfc676b0dcc191611482c2907be241f181ae/platform/vcs-impl/src/com/intellij/vcs/commit/CommitOptionsPanel.kt#L118
    private fun extractMeaningfulComponent(component: JComponent): JComponent? {
        if (component is DialogPanel)
            return null

        if (component is JPanel) {
            val border = component.border

            if (component.layout is BorderLayout && component.components.size == 1 && (border == null || border is EmptyBorder))
                return component.components[0] as? JComponent
        }

        return null
    }
    // SPDX-SnippetEnd

    override fun getPreferredFocusedComponent(): JComponent
        = titleField

    override fun isComplete(): Boolean
        = true

    override fun forgetUserChoice() {
        UserChoiceStore.getInstance(project).forgetChoice()
        ToDoService.getInstance(project).createNewIssue(model.toDoItem)
    }
}
