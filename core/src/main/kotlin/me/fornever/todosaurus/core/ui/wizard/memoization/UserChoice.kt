// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.ui.wizard.memoization

import me.fornever.todosaurus.core.issues.IssuePlacementDetails

data class UserChoice(
    var issueTrackerId: String? = null,
    var credentialsId: String? = null,
    var placementDetails: IssuePlacementDetails? = null
) {
    fun accept(visitor: UserChoiceVisitor)
        = visitor.visit(this)
}
