// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issues.ui.wizard

import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import kotlinx.coroutines.CoroutineScope
import me.fornever.todosaurus.core.TodosaurusCoreBundle
import me.fornever.todosaurus.core.git.GitBasedPlacementDetails
import me.fornever.todosaurus.core.git.GitHostingRemote
import me.fornever.todosaurus.core.issues.IssueLabel
import me.fornever.todosaurus.core.issues.ToDoItem
import me.fornever.todosaurus.core.issues.ui.controls.LabelList
import me.fornever.todosaurus.core.ui.wizard.TodosaurusWizardContext
import javax.swing.JComponent

abstract class LabelsOptions(
	scope: CoroutineScope,
	private val model: TodosaurusWizardContext<ToDoItem.New>
): IssueOptions {
    private var hostingRemote: GitHostingRemote? = null
    private val labelList: LabelList = LabelList(scope)

    fun getSelectedLabels()
        = labelList.getSelectedTags()

    override fun refresh() {
        val placementDetails = model.placementDetails as? GitBasedPlacementDetails
            ?: return

        if (hostingRemote != placementDetails.remote)
            labelList.fetchTagsUsing(::provideLabels)

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
