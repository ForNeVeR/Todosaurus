// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issues.ui.gutter

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiElement
import me.fornever.todosaurus.core.issues.ToDoItem
import me.fornever.todosaurus.core.issues.ui.gutter.actions.CreateNewIssueForAction
import me.fornever.todosaurus.core.issues.ui.gutter.actions.OpenIssueThatReportedAsAction
import me.fornever.todosaurus.core.settings.TodosaurusSettings
import javax.swing.Icon

class ToDoGutterIconRenderer(private val lineMarker: ToDoLineMarker) : LineMarkerInfo.LineMarkerGutterIconRenderer<PsiElement>(lineMarker) {
    override fun equals(other: Any?)
        = other is ToDoGutterIconRenderer

    override fun hashCode() = 0

    override fun getIcon(): Icon
        = AllIcons.General.TodoDefault

    override fun getPopupMenuActions(): ActionGroup? {
        val psiElement = lineMarker.element
            ?: return ActionGroup.EMPTY_GROUP

        val todosaurusSettings = TodosaurusSettings.getInstance()
        val toDoItems = ToDoItem.extractFrom(psiElement, todosaurusSettings.state)

        if (toDoItems.isEmpty())
            return ActionGroup.EMPTY_GROUP

        return object : ActionGroup() {
            override fun getChildren(actionEvent: AnActionEvent?): Array<AnAction>
                = toDoItems.map { when(it) {
                    is ToDoItem.New -> CreateNewIssueForAction(it)
                    is ToDoItem.Reported -> OpenIssueThatReportedAsAction(it)
                } }
                .toTypedArray()
        }
    }

    override fun isDumbAware() = true
}
