// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.ui.controls.tagList.chooserPopup

import com.intellij.collaboration.ui.codereview.details.SelectableWrapper
import me.fornever.todosaurus.core.ui.controls.tagList.TagPresentation
import me.fornever.todosaurus.core.ui.controls.tagList.TagRendererBase
import java.awt.Component
import javax.swing.JList
import javax.swing.ListCellRenderer

@Suppress("UnstableApiUsage")
class SelectableWrapperRendererAdapter<Tag>(private val presentationFactory: (Tag) -> TagPresentation<Tag>, private val adaptee: TagRendererBase<SelectableWrapper<Tag>>)
    : ListCellRenderer<SelectableWrapper<Tag>> {
    override fun getListCellRendererComponent(
        list: JList<out SelectableWrapper<Tag>>,
        selectedItem: SelectableWrapper<Tag>,
        index: Int,
        isSelected: Boolean,
        hasFocus: Boolean
    ): Component
        = adaptee.renderComponent(SelectableWrapperAdapter(selectedItem, presentationFactory(selectedItem.value)), isSelected, hasFocus)

    private class SelectableWrapperAdapter<Tag>(override val value: SelectableWrapper<Tag>, private val presentation: TagPresentation<Tag>)
        : TagPresentation<SelectableWrapper<Tag>>(presentation.colorHex) {
        override val id: Long
            get() = presentation.id

        override val text: String
            get() = presentation.text

        override val description: String?
            get() = presentation.description
    }
}
