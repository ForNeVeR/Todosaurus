// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.wizardTests

import me.fornever.todosaurus.core.ui.wizard.TodosaurusWizardStep
import javax.swing.JComponent

class FakeWizardStep(override val id: String) : TodosaurusWizardStep() {
    override fun createComponent(): JComponent = error("Not implemented.")

    override fun getPreferredFocusedComponent(): JComponent = error("Not implemented.")

    override fun isComplete(): Boolean = error("Not implemented.")
}
