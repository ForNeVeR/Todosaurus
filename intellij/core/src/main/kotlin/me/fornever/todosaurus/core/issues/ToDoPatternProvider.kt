// SPDX-FileCopyrightText: 2024-2026 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issues

// IgnoreTODO-Start
import com.intellij.ide.todo.TodoConfiguration
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.RangeMarker
// IgnoreTODO-End

@Suppress("unused") // TODO[#134]: This is supposed to become used.
@Service(Service.Level.APP)
class ToDoPatternProvider {
    companion object {
        fun getInstance(): ToDoPatternProvider = service()
    }

    fun provideSuitablePattern(toDoRange: RangeMarker): Regex
        = TodoConfiguration
            .getInstance()
            .todoPatterns
            .mapNotNull { it.pattern?.toRegex() }
            // IgnoreTODO-Start
            .firstOrNull { it.containsMatchIn(toDoRange.document.getText(toDoRange.textRange)) }
                ?: error("The specified TODO does not match any of the patterns")
            // IgnoreTODO-End
}
