package me.fornever.todosaurus.gutter

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import me.fornever.todosaurus.TodosaurusBundle
import me.fornever.todosaurus.services.ToDoService

class GutterIconProvider : LineMarkerProviderDescriptor() {

    override fun getName() = null

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element.children.isNotEmpty()) return null

        val service = ToDoService.getInstance(element.project)
        if (!service.hasNewToDoItem(element.text)) return null

        return LineMarkerInfo(
            element,
            element.textRange,
            AllIcons.General.TodoDefault,
            null, // TODO: Tooltip?
            null,
            GutterIconRenderer.Alignment.LEFT,
            TodosaurusBundle.messagePointer("gutter-icon.accessible-name")
        )
    }
}
