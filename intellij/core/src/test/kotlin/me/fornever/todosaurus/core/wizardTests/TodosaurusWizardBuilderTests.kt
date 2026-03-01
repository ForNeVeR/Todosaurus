// SPDX-FileCopyrightText: 2024-2026 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.wizardTests

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import me.fornever.todosaurus.core.issues.ToDoItem
import me.fornever.todosaurus.core.settings.TodosaurusSettings
import me.fornever.todosaurus.core.testFramework.FakeProject
import me.fornever.todosaurus.core.testFramework.FakeRangeMarker
import me.fornever.todosaurus.core.ui.wizard.TodosaurusWizardBuilder
import me.fornever.todosaurus.core.ui.wizard.TodosaurusWizardContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TodosaurusWizardBuilderTests {
    @Test
    fun `Should link steps properly`() {
        // Arrange
        // IgnoreTODO-Start
        val model = TodosaurusWizardContext(ToDoItem.fromRange(FakeRangeMarker("TODO"), TodosaurusSettings.State.defaultState))
        // IgnoreTODO-End
        val sut = TodosaurusWizardBuilder(FakeProject(), model, CoroutineScope(Dispatchers.IO))
        val firstStep = FakeWizardStep("1")
        val secondStep = FakeWizardStep("2")
        val lastStep = FakeWizardStep("3")

        // Act
        sut.addStep(firstStep)
            .addStep(secondStep)
            .addStep(lastStep)

        // Assert
        assertEquals(secondStep.id, firstStep.nextId)
        assertEquals(null, firstStep.previousId)
        assertEquals(lastStep.id, secondStep.nextId)
        assertEquals(firstStep.id, secondStep.previousId)
        assertEquals(null, lastStep.nextId)
        assertEquals(secondStep.id, lastStep.previousId)
    }
}
