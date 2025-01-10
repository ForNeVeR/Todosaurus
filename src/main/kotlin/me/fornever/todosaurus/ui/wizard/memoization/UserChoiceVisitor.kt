// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.ui.wizard.memoization

import me.fornever.todosaurus.vcs.git.GitBasedPlacementDetails

interface UserChoiceVisitor {
    fun visit(userChoice: UserChoice)

    fun visit(placementDetails: GitBasedPlacementDetails)
}
