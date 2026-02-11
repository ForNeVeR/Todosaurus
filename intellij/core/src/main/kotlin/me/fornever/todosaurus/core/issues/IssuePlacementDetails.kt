// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issues

import me.fornever.todosaurus.core.ui.wizard.memoization.UserChoiceVisitor

interface IssuePlacementDetails {
    val type: IssuePlacementDetailsType

    fun accept(visitor: UserChoiceVisitor)
}
