// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issues.ui.wizard

import com.intellij.openapi.application.ApplicationManager
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
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.Splitter
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import me.fornever.todosaurus.core.TodosaurusCoreBundle
import me.fornever.todosaurus.core.issues.ToDoItem
import me.fornever.todosaurus.core.issues.ToDoService
import me.fornever.todosaurus.core.issues.UrlReplacement
import me.fornever.todosaurus.core.ui.wizard.TodosaurusWizardContext
import me.fornever.todosaurus.core.ui.wizard.TodosaurusWizardStep
import me.fornever.todosaurus.core.ui.wizard.memoization.ForgettableStep
import me.fornever.todosaurus.core.ui.wizard.memoization.UserChoiceStore
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class CreateNewIssueStep(
    private val project: Project,
    private val model: TodosaurusWizardContext<ToDoItem.New>) : TodosaurusWizardStep(), ForgettableStep {

    override val id: String = CreateNewIssueStep::class.java.name

    private lateinit var titleField: JBTextField
    private lateinit var descriptionField: JBTextArea
    private var filePathLabel: JLabel = JLabel().apply {
        foreground = UIUtil.getLabelInfoForeground()
    }

    private lateinit var todoPreviewer: Editor

    private val file: VirtualFile?
        = FileDocumentManager.getInstance().getFile(model.toDoItem.toDoRange.document)

    private val selectionAttributes: TextAttributes
        = EditorColorsManager
            .getInstance()
            .globalScheme
            .getAttributes(EditorColors.SEARCH_RESULT_ATTRIBUTES)

    private var issueTrackerId: String? = null
    @Suppress("UnstableApiUsage")
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

        if (file == null)
            return null

        val editor = EditorFactory.getInstance().createViewer(document, project, EditorKind.PREVIEW)
            .also { todoPreviewer = it }

        editor.component.preferredSize = JBUI.emptySize()

        if (editor is EditorEx) {
            editor.settings.apply {
                isLineNumbersShown = true
                isAnimatedScrolling = true
                isFoldingOutlineShown = false
                isAdditionalPageAtBottom = false
                isCaretRowShown = true
                isVirtualSpace = false
                isUseSoftWraps = false
            }

            editor.highlighter = EditorHighlighterFactory
                .getInstance()
                .createEditorHighlighter(project, file)
        }

        return panel {
            row {
                cell(filePathLabel)
            }

            row {
                cell(editor.component)
                    .resizableColumn()
                    .align(Align.FILL)
            }
            .resizableRow()
        }
    }

    private fun highlightUrlReplacementInsideTodoPreviewer(urlReplacement: UrlReplacement) {
        todoPreviewer.markupModel.removeAllHighlighters()

        todoPreviewer.caretModel.moveToOffset(urlReplacement.startLineOffset)
        todoPreviewer.scrollingModel.scrollToCaret(ScrollType.CENTER)

        todoPreviewer.markupModel.addRangeHighlighter(
            urlReplacement.startLineOffset,
            urlReplacement.endLineOffset,
            HighlighterLayer.SELECTION - 1,
            selectionAttributes,
            HighlighterTargetArea.LINES_IN_RANGE)
    }

    private val mainPanel: DialogPanel = panel {
        row {
            @Suppress("UnstableApiUsage")
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
                .onChanged { textArea ->
                    model.toDoItem.description = textArea.text
                }
                .component
                .also { textArea ->
                    textArea.lineWrap = true
                    textArea.wrapStyleWord = true
                    textArea.addCaretListener { caret ->
                        val currentPosition = caret?.dot
                            ?: return@addCaretListener

                        val replacement = model.toDoItem.urlReplacements.getSelectedReplacement(currentPosition)
                            ?: model.toDoItem.urlReplacements.getLastReplacement()

                        updateFilePath(replacement)

                        if (replacement != null) {
                            highlightUrlReplacementInsideTodoPreviewer(replacement)
                        }
                        else {
                            todoPreviewer.markupModel.removeAllHighlighters()
                        }
                    }
                }
        }
        .resizableRow()
    }

    private fun updateFilePath(replacement: UrlReplacement?) {
        if (file == null)
            return

        filePathLabel.text = when {
            replacement == null -> TodosaurusCoreBundle.getMessage("wizard.steps.createNewIssue.labels.filePath.text", file.path)
            replacement.startLineNumber == replacement.endLineNumber -> TodosaurusCoreBundle.getMessage("wizard.steps.createNewIssue.labels.filePath.text.designator.singleLine", file.path, replacement.startLineNumber)
            else -> TodosaurusCoreBundle.getMessage("wizard.steps.createNewIssue.labels.filePath.text.designator.multipleLines", file.path, replacement.startLineNumber, replacement.endLineNumber)
        }
    }

    private fun recreateIssueOptions() {
        val currentHolder = optionsHolder ?: return

        val issueOptions = IssueOptionsProvider.provideAll(project, model)

        @Suppress("UnstableApiUsage")
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

        ApplicationManager.getApplication().invokeLater {
            val lastReplacement = model.toDoItem
                .urlReplacements
                .getLastReplacement()

            if (lastReplacement != null) {
                updateFilePath(lastReplacement)
                highlightUrlReplacementInsideTodoPreviewer(lastReplacement)
            }
        }

        return rootPanel
    }

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
