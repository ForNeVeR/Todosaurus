// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.wizardTests.memoizationTests

import me.fornever.todosaurus.issues.IssuePlacementDetails
import me.fornever.todosaurus.issues.IssuePlacementDetailsType
import me.fornever.todosaurus.ui.wizard.memoization.UserChoiceVisitor

class FakeIssuePlacementDetails : IssuePlacementDetails {
    override val type: IssuePlacementDetailsType
        get() = IssuePlacementDetailsType.valueOf("Fake")

    override fun accept(visitor: UserChoiceVisitor)
    { }
}
