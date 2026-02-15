// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.ui.wizard.memoization

import me.fornever.todosaurus.core.git.GitBasedPlacementDetails

interface UserChoiceVisitor {
    fun visit(userChoice: UserChoice)

    fun visit(placementDetails: GitBasedPlacementDetails)
}
