// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.ui.wizard.memoization

import me.fornever.todosaurus.issueTrackers.IssueTrackerType
import me.fornever.todosaurus.issues.IssuePlacementDetails

data class UserChoice(
    var issueTrackerType: IssueTrackerType? = null,
    var credentialsId: String? = null,
    var placementDetails: IssuePlacementDetails? = null
) {
    fun accept(visitor: UserChoiceVisitor)
        = visitor.visit(this)
}
