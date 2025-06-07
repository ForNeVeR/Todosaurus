// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issues.ui.controls

import com.intellij.ui.ColorUtil
import kotlinx.coroutines.CoroutineScope
import me.fornever.todosaurus.core.TodosaurusCoreBundle
import me.fornever.todosaurus.core.issues.IssueLabel
import me.fornever.todosaurus.core.ui.controls.tagList.TagList
import me.fornever.todosaurus.core.ui.controls.tagList.TagRenderer

class LabelList(scope: CoroutineScope)
    : TagList<IssueLabel>(
        scope,
        { LabelPresentation(it, it.id, it.name, it.description, ColorUtil.fromHex(it.color)) },
        { TagRenderer(it) }) {
        init {
            searchTooltipText = TodosaurusCoreBundle.message("wizard.steps.createNewIssue.labels.popup.search.tooltip")
            deselectTooltipText = TodosaurusCoreBundle.message("wizard.steps.createNewIssue.labels.deselect.tooltip")
            noTagsText = TodosaurusCoreBundle.message("wizard.steps.createNewIssue.labels.empty.text")
        }
    }
