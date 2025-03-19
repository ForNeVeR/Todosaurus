// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.ui.controls.tagList.chooserPopup

import com.intellij.collaboration.ui.codereview.details.SelectableWrapper
import me.fornever.todosaurus.core.ui.controls.tagList.TagPresentation
import me.fornever.todosaurus.core.ui.controls.tagList.TagRendererBase
import java.awt.Color
import java.awt.Component
import javax.swing.JList
import javax.swing.ListCellRenderer

@Suppress("UnstableApiUsage")
class SelectableWrapperRendererAdapter<T>(private val presentationFactory: (T) -> TagPresentation<T>, private val adaptee: TagRendererBase<SelectableWrapper<T>>) :
	ListCellRenderer<SelectableWrapper<T>> {
    override fun getListCellRendererComponent(
		list: JList<out SelectableWrapper<T>>,
		selectedItem: SelectableWrapper<T>,
		index: Int,
		isSelected: Boolean,
		hasFocus: Boolean
    ): Component
        = adaptee.renderComponent(SelectableWrapperAdapter(selectedItem, presentationFactory(selectedItem.value)), isSelected, hasFocus)

    private class SelectableWrapperAdapter<T>(override val value: SelectableWrapper<T>, private val presentation: TagPresentation<T>) :
        TagPresentation<SelectableWrapper<T>>() {
        override val id: Long
            get() = presentation.id

        override val text: String
            get() = presentation.text

        override val description: String?
            get() = presentation.description

        override val foreground: Color
            get() = presentation.foreground
    }
}
