// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.ui.actions

import com.intellij.ide.todo.TodoPanel
import com.intellij.ide.todo.nodes.TodoItemNode
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.editor.RangeMarker
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.tree.TreeUtil

fun AnActionEvent.getToDoTextRange(): RangeMarker? {
    val tree = getData(PlatformDataKeys.CONTEXT_COMPONENT) as? Tree
    val descriptor = tree?.selectionPath?.let(TreeUtil::getLastUserObject) as? TodoItemNode
    return descriptor?.value?.rangeMarker
}

fun AnActionEvent.tryActivateOnToDoPanel(): Boolean {
    val toDoPanel = getData(TodoPanel.TODO_PANEL_DATA_KEY)

    if (toDoPanel == null || project == null) {
        presentation.isEnabledAndVisible = false

        return false
    }

    presentation.isVisible = true

    return true
}
