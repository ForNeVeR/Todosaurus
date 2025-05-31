// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.gitHub.labels.ui.controls

import me.fornever.todosaurus.core.ui.controls.tagList.TagPresentation
import org.jetbrains.plugins.github.api.data.GithubIssueLabel
import java.awt.Color

data class LabelPresentation(
    override val value: GithubIssueLabel,
    override val id: Long,
    override val text: String,
    override val description: String?,
    override val foreground: Color
) : TagPresentation<GithubIssueLabel>()
