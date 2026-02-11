// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issues.ui.gutter

import com.intellij.codeInsight.daemon.MergeableLineMarkerInfo
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import me.fornever.todosaurus.core.TodosaurusCoreBundle
import me.fornever.todosaurus.core.issues.ToDoItem
import me.fornever.todosaurus.core.settings.TodosaurusSettings
import javax.swing.Icon

class ToDoLineMarker(psiElement: PsiElement)
    : MergeableLineMarkerInfo<PsiElement>(
        psiElement,
        psiElement.textRange,
        AllIcons.General.TodoDefault,
        ::tooltipProvider,
        /* navHandler = */ null,
        GutterIconRenderer.Alignment.LEFT,
        TodosaurusCoreBundle.messagePointer("gutter.accessibleName")
) {
    override fun canMergeWith(lineMarker: MergeableLineMarkerInfo<*>): Boolean
        = lineMarker is ToDoLineMarker

    override fun getCommonIcon(lineMarkers: MutableList<out MergeableLineMarkerInfo<*>>): Icon
        = lineMarkers[0].icon

    override fun createGutterRenderer() = ToDoGutterIconRenderer(this)
}

private fun tooltipProvider(psiElement: PsiElement): String? {
    val todosaurusSettings = TodosaurusSettings.getInstance()
    val toDoItems = ToDoItem.extractFrom(psiElement, todosaurusSettings.state)
    val counts = ToDoCounts.create(toDoItems)

    return when {
        counts.reported == 0 && counts.new == 0 -> null
        counts.reported == 0 && counts.new == 1 -> TodosaurusCoreBundle.message("gutter.tooltip.newItem")
        counts.reported == 0 && counts.new > 1 -> TodosaurusCoreBundle.message("gutter.tooltip.newItems", counts.new)
        counts.reported == 1 && counts.new == 0 -> {
            val toDoItem = toDoItems.filterIsInstance<ToDoItem.Reported>().single()
            TodosaurusCoreBundle.message("gutter.tooltip.reportedItem", toDoItem.issueNumber)
        }
        counts.reported > 1 && counts.new == 0 -> TodosaurusCoreBundle.message("gutter.tooltip.reportedItems", counts.reported)
        else -> TodosaurusCoreBundle.message("gutter.tooltip.mixedItems", counts.new, counts.reported)
    }
}

private data class ToDoCounts(val new: Int, val reported: Int) {
    companion object {
        fun create(items: Array<ToDoItem>): ToDoCounts {
            var new = 0
            var reported = 0

            for (item in items)
                when (item) {
                    is ToDoItem.New -> new++
                    is ToDoItem.Reported -> reported++
                }

            return ToDoCounts(new, reported)
        }
    }
}
