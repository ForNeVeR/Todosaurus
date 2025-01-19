// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.wizardTests

import me.fornever.todosaurus.ui.wizard.TodosaurusWizardStep
import javax.swing.JComponent

class FakeWizardStep(override val id: String) : TodosaurusWizardStep() {
    override fun getComponent(): JComponent = error("Not implemented.")

    override fun getPreferredFocusedComponent(): JComponent = error("Not implemented.")

    override fun isComplete(): Boolean = error("Not implemented.")
}
