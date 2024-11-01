// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.wizardTests

import me.fornever.todosaurus.ui.wizard.TodosaurusStep
import javax.swing.JComponent

class FakeStep(override val id: Any) : TodosaurusStep() {
    override fun getComponent(): JComponent = error("Not implemented.")

    override fun getPreferredFocusedComponent(): JComponent = error("Not implemented.")

    override fun isComplete(): Boolean = error("Not implemented.")
}
