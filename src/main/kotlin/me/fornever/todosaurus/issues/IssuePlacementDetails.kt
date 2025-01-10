// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issues

import me.fornever.todosaurus.ui.wizard.memoization.UserChoiceVisitor

interface IssuePlacementDetails {
    val type: IssuePlacementDetailsType

    fun accept(visitor: UserChoiceVisitor)
}
