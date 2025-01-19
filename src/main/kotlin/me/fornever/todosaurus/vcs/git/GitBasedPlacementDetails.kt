// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.vcs.git

import me.fornever.todosaurus.issues.IssuePlacementDetails
import me.fornever.todosaurus.issues.IssuePlacementDetailsType
import me.fornever.todosaurus.ui.wizard.memoization.UserChoiceVisitor

class GitBasedPlacementDetails : IssuePlacementDetails {
    var remote: GitHostingRemote? = null

    override val type: IssuePlacementDetailsType
        get() = IssuePlacementDetailsType.GitBased

    override fun accept(visitor: UserChoiceVisitor)
        = visitor.visit(this)
}
