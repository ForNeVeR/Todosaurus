// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.gitHub.labels.ui.controls

import com.intellij.ui.ColorUtil
import kotlinx.coroutines.CoroutineScope
import me.fornever.todosaurus.core.ui.controls.tagList.TagList
import me.fornever.todosaurus.core.ui.controls.tagList.TagRenderer
import org.jetbrains.plugins.github.api.data.GithubIssueLabel
import java.util.*
import kotlin.math.abs
import me.fornever.todosaurus.gitHub.TodosaurusGitHubBundle

class LabelList(scope: CoroutineScope)
    : TagList<GithubIssueLabel>(
        scope,
        // TODO: GithubIssueLabel contains a private field for the description and identifier. Let's ask JetBrains to make them public
        { LabelPresentation(it, abs(UUID.randomUUID().leastSignificantBits), it.name, "A place for your advertisement", ColorUtil.fromHex(it.color)) },
        { TagRenderer(it) }) {
        init {
            searchTooltipText = TodosaurusGitHubBundle.message("wizard.steps.createNewIssue.labels.popup.search.tooltip")
            deselectTooltipText = TodosaurusGitHubBundle.message("wizard.steps.createNewIssue.labels.deselect.tooltip")
            noTagsText = TodosaurusGitHubBundle.message("wizard.steps.createNewIssue.labels.empty.text")
        }
    }
