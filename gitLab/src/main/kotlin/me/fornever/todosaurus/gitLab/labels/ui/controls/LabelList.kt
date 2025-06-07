// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT
package me.fornever.todosaurus.gitLab.labels.ui.controls

import com.intellij.ui.ColorUtil
import kotlinx.coroutines.CoroutineScope
import me.fornever.todosaurus.core.ui.controls.tagList.TagList
import me.fornever.todosaurus.core.ui.controls.tagList.TagRenderer
import me.fornever.todosaurus.gitLab.TodosaurusGitLabBundle
import me.fornever.todosaurus.gitLab.api.GitLabLabel

class LabelList(scope: CoroutineScope)
    : TagList<GitLabLabel>(
        scope,
        { LabelPresentation(it, it.id, it.name, it.description, ColorUtil.fromHex(it.color)) },
        { TagRenderer(it) }) {
        init {
            searchTooltipText = TodosaurusGitLabBundle.message("wizard.steps.createNewIssue.labels.popup.search.tooltip")
            deselectTooltipText = TodosaurusGitLabBundle.message("wizard.steps.createNewIssue.labels.deselect.tooltip")
            noTagsText = TodosaurusGitLabBundle.message("wizard.steps.createNewIssue.labels.empty.text")
        }
    }
