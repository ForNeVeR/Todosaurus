// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.gitHub.labels.ui.controls

import com.intellij.ui.ColorUtil
import kotlinx.coroutines.CoroutineScope
import me.fornever.todosaurus.core.ui.controls.tagList.TagList
import me.fornever.todosaurus.core.ui.controls.tagList.TagRenderer
import me.fornever.todosaurus.gitHub.TodosaurusGitHubBundle
import me.fornever.todosaurus.gitHub.labels.api.GitHubLabel

class LabelList(scope: CoroutineScope)
    : TagList<GitHubLabel>(
        scope,
        { LabelPresentation(it, it.id, it.name, it.description, ColorUtil.fromHex(it.color)) },
        { TagRenderer(it) }) {
        init {
            searchTooltipText = TodosaurusGitHubBundle.message("wizard.steps.createNewIssue.labels.popup.search.tooltip")
            deselectTooltipText = TodosaurusGitHubBundle.message("wizard.steps.createNewIssue.labels.deselect.tooltip")
            noTagsText = TodosaurusGitHubBundle.message("wizard.steps.createNewIssue.labels.empty.text")
        }
    }
