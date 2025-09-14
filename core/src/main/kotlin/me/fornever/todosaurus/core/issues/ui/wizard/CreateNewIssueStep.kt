// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issues.ui.wizard

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.EditorKind
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.Splitter
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
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
    private lateinit var todoPreviewer: Editor

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

    private fun createTodoPreviewer(): JComponent? {
        val document = model.toDoItem.toDoRange.document
        val file = FileDocumentManager.getInstance().getFile(document) ?: return null

        val selectionStartOffset = model.toDoItem.toDoRange.textRange.startOffset
        val selectionEndOffset = model.toDoItem.toDoRange.textRange.endOffset

        val editor = EditorFactory.getInstance().createViewer(document, project, EditorKind.PREVIEW)

        editor.component.preferredSize = JBUI.size(150, 50)

        if (editor is EditorEx) {
            editor.settings.apply {
                isLineNumbersShown = true
                isFoldingOutlineShown = false
                isAdditionalPageAtBottom = false
                isCaretRowShown = false
                isVirtualSpace = false
                isUseSoftWraps = false
            }

            editor.highlighter = EditorHighlighterFactory
                .getInstance()
                .createEditorHighlighter(project, file)
        }

        editor.caretModel.moveToOffset(selectionStartOffset)
        editor.scrollingModel.scrollToCaret(ScrollType.CENTER)

        val selectionAttributes = EditorColorsManager
            .getInstance()
            .globalScheme
            .getAttributes(EditorColors.SEARCH_RESULT_ATTRIBUTES)

        editor.markupModel.addRangeHighlighter(
            selectionStartOffset,
            selectionEndOffset,
            HighlighterLayer.SELECTION - 1,
            selectionAttributes,
            HighlighterTargetArea.EXACT_RANGE)

        return editor.also { todoPreviewer = it }.component
    }

    private val mainPanel: DialogPanel = panel {
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

    override fun createComponent(): JComponent {
        val rootPanel = Splitter(true, 0.50f)
        val todoPreviewer = createTodoPreviewer()

        rootPanel.firstComponent = mainPanel

        if (todoPreviewer != null)
            rootPanel.secondComponent = todoPreviewer

        return ScrollPaneFactory.createScrollPane(rootPanel, true)
    }

    // TODO: Add license information. Copied from https://github.com/JetBrains/intellij-community/blob/dc6fdfc676b0dcc191611482c2907be241f181ae/platform/vcs-impl/src/com/intellij/vcs/commit/CommitOptionsPanel.kt#L110
    private fun Panel.createOptionsRow(component: JComponent): Row {
        val meaningfulComponent = extractMeaningfulComponent(component)

        return row {
            cell(meaningfulComponent ?: component)
                .align(Align.FILL)
        }
        .bottomGap(BottomGap.MEDIUM)
    }

    // TODO: Add license information. Copied from https://github.com/JetBrains/intellij-community/blob/dc6fdfc676b0dcc191611482c2907be241f181ae/platform/vcs-impl/src/com/intellij/vcs/commit/CommitOptionsPanel.kt#L118
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

    override fun getPreferredFocusedComponent(): JComponent
        = titleField

    override fun isComplete(): Boolean
        = true

    override fun forgetUserChoice() {
        UserChoiceStore.getInstance(project).forgetChoice()
        ToDoService.getInstance(project).createNewIssue(model.toDoItem)
    }

    override fun dispose() {
        super.dispose()

        UIUtil.invokeLaterIfNeeded {
            try {
                EditorFactory.getInstance().releaseEditor(todoPreviewer)
            }
            catch (_: Throwable) {
                // Ignore
            }
        }
    }
}
