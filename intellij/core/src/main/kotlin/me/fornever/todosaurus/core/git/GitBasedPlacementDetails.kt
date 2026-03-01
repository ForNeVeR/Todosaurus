// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.git

import me.fornever.todosaurus.core.issues.IssuePlacementDetails
import me.fornever.todosaurus.core.issues.IssuePlacementDetailsType
import me.fornever.todosaurus.core.ui.wizard.memoization.UserChoiceVisitor

class GitBasedPlacementDetails : IssuePlacementDetails {
    var remote: GitHostingRemote? = null

    override val type: IssuePlacementDetailsType
        get() = IssuePlacementDetailsType.GitBased

    override fun accept(visitor: UserChoiceVisitor)
        = visitor.visit(this)
}
