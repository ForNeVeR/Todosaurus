// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issues.labels.ui.controls

import kotlinx.coroutines.CoroutineScope
import me.fornever.todosaurus.core.TodosaurusCoreBundle
import me.fornever.todosaurus.core.issues.labels.IssueLabel
import me.fornever.todosaurus.core.ui.controls.tagList.TagList
import me.fornever.todosaurus.core.ui.controls.tagList.TagRenderer

class LabelList(scope: CoroutineScope)
    : TagList<String, IssueLabel>(
        scope,
        presentationFactory = { LabelPresentation(it, it.id, it.name, it.description, it.color) },
        keySelector = { it.name },
        rendererFactory = { TagRenderer(it) }) {
        init {
            searchTooltipText = TodosaurusCoreBundle.message("wizard.steps.createNewIssue.labels.popup.search.tooltip")
            deselectTooltipText = TodosaurusCoreBundle.message("wizard.steps.createNewIssue.labels.deselect.tooltip")
            noTagsText = TodosaurusCoreBundle.message("wizard.steps.createNewIssue.labels.empty.text")
        }
    }
