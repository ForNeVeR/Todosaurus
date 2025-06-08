// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issues.labels

import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import me.fornever.todosaurus.core.TodosaurusCoreBundle
import me.fornever.todosaurus.core.git.GitBasedPlacementDetails
import me.fornever.todosaurus.core.git.GitHostingRemote
import me.fornever.todosaurus.core.issues.ToDoItem
import me.fornever.todosaurus.core.issues.labels.ui.controls.LabelList
import me.fornever.todosaurus.core.issues.ui.wizard.IssueOptions
import me.fornever.todosaurus.core.ui.wizard.TodosaurusWizardContext
import javax.swing.JCheckBox
import javax.swing.JComponent

abstract class LabelsOptions(
	scope: CoroutineScope,
    private val project: Project,
	private val model: TodosaurusWizardContext<ToDoItem.New>
): IssueOptions {
    private var hostingRemote: GitHostingRemote? = null
    private var rememberCheckBox: JCheckBox = JCheckBox(TodosaurusCoreBundle.message("wizard.steps.createNewIssue.labels.remember.text"), true)
    private val labelList: LabelList = LabelList(scope, rememberCheckBox)

    fun getSelectedLabels()
        = labelList.getSelectedTags()

    override fun save() {
        val labelsStore = LabelsStore.getInstance(project)
        labelsStore.state.labels = if (rememberCheckBox.isSelected) getSelectedLabels().map { it.name } else emptyList()
    }

    override fun refresh() {
        val placementDetails = model.placementDetails as? GitBasedPlacementDetails
            ?: return

        if (hostingRemote != placementDetails.remote)
            labelList.fetchTagsUsing(::provideLabels) {
                val savedLabels = LabelsStore
                    .getInstance(project)
                    .state
                    .labels
                    .mapNotNull { labelList.extractTag(it) }

                if (savedLabels.isNotEmpty())
                    labelList.selectTags(savedLabels)
            }

        hostingRemote = placementDetails.remote
    }

    override fun createOptionsPanel(): JComponent {
        val panel = panel {
			group(TodosaurusCoreBundle.message("wizard.steps.createNewIssue.labels.title"), false) {
				row {
					cell(labelList)
						.align(Align.FILL)
				}
			}
		}

        refresh()

        return panel
    }

    abstract suspend fun provideLabels(): Iterable<IssueLabel>
}
