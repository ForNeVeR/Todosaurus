// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issues.labels.ui.controls

import me.fornever.todosaurus.core.issues.labels.IssueLabel
import me.fornever.todosaurus.core.ui.controls.tagList.TagPresentation

class LabelPresentation(
    override val value: IssueLabel,
    override val id: Long,
    override val text: String,
    override val description: String?,
    colorHex: String
) : TagPresentation<IssueLabel>(colorHex)
