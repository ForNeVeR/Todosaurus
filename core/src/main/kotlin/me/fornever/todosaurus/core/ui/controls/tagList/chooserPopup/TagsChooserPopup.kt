// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.ui.controls.tagList.chooserPopup

import com.intellij.collaboration.ui.codereview.details.SelectableWrapper
import com.intellij.collaboration.ui.codereview.list.search.PopupConfig
import com.intellij.collaboration.ui.util.popup.showAndAwaitSubmissions
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.openapi.ui.popup.util.PopupUtil
import com.intellij.ui.CollectionListModel
import com.intellij.ui.SearchTextField
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.JBList
import com.intellij.ui.components.TextComponentEmptyText
import com.intellij.ui.popup.AbstractPopup
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import me.fornever.todosaurus.core.ui.controls.tagList.TagPresentation
import me.fornever.todosaurus.core.ui.controls.tagList.TagRendererBase
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JList
import javax.swing.ListCellRenderer
import javax.swing.ListSelectionModel

/*
    This file contains functions to create multiple chooser popup.
    Copied from:
    1. https://github.com/JetBrains/intellij-community/blob/b8e56e1013054034fcd7d5d8aab96bba355b429a/platform/collaboration-tools/src/com/intellij/collaboration/ui/codereview/list/search/ChooserPopupUtil.kt#L35
    2. https://github.com/JetBrains/intellij-community/blob/ce3350428547b29fc91584a082389769540623fd/platform/collaboration-tools/src/com/intellij/collaboration/ui/util/popup/CollaborationToolsPopupUtil.kt#L23
    TODO: Add license information
 */

@Suppress("UnstableApiUsage")
object TagsChooserPopup {
    suspend fun <T> showTagsChooserPopup(
        tags: Map<T, TagPresentation<T>>,
        popupRenderer: TagRendererBase<SelectableWrapper<T>>,
        selectionChooser: (T) -> Boolean,
        position: RelativePoint,
        popupOptions: PopupConfig): List<T> {

        val popupTags = tags.values.map { SelectableWrapper(it.value, selectionChooser(it.value)) }.toList()

        val listModel = CollectionListModel(popupTags)
        val rendererAdapter = SelectableWrapperRendererAdapter({ tags[it] ?: error("Tag should be added to list") }, popupRenderer)
        val selectableList = listModel.toSelectableList(rendererAdapter)

        val popup = PopupChooserBuilder(selectableList)
            .setFilteringEnabled { selectableItem ->
                val wrappeeItem = (selectableItem as? SelectableWrapper<*>)
                    ?: return@setFilteringEnabled ""

                val presentation = tags[wrappeeItem.value]
                    ?: return@setFilteringEnabled ""

                return@setFilteringEnabled presentation.text
            }
            .setCloseOnEnter(false)
            .configure(popupOptions)
            .createPopup()

        popup.configureSearchField(popupOptions)
        PopupUtil.setPopupToggleComponent(popup, position.component)

        return popup.showAndAwaitSubmissions(selectableList, position, popupOptions.showDirection)
    }

    private fun <T> CollectionListModel<SelectableWrapper<T>>.toSelectableList(renderer: ListCellRenderer<SelectableWrapper<T>>): JBList<SelectableWrapper<T>>
        = JBList(this).apply {
            visibleRowCount = 7
            selectionMode = ListSelectionModel.SINGLE_SELECTION
            cellRenderer = renderer
            background = JBUI.CurrentTheme.Popup.BACKGROUND

            addMouseListener(object : MouseAdapter() {
                override fun mouseReleased(mouseEvent: MouseEvent) {
                    if (UIUtil.isActionClick(mouseEvent, MouseEvent.MOUSE_RELEASED) && !UIUtil.isSelectionButtonDown(mouseEvent) && !mouseEvent.isConsumed)
                        toggleSelection()
                }
            })

            addKeyListener(object : KeyAdapter() {
                override fun keyPressed(keyEvent: KeyEvent?) {
                    if (keyEvent != null && keyEvent.keyCode == KeyEvent.VK_ENTER)
                        toggleSelection()
                }
            })
        }

    private fun <T> JList<SelectableWrapper<T>>.toggleSelection() {
        selectedValuesList.forEach {
            it.isSelected = !it.isSelected
        }

        repaint()
    }

    private fun JBPopup.configureSearchField(popupOptions: PopupConfig) {
        val searchTextField = UIUtil.findComponentOfType(content, SearchTextField::class.java) ?: return
        AbstractPopup.customizeSearchFieldLook(searchTextField, true)
        searchTextField.textEditor.emptyText.text = popupOptions.searchTextPlaceHolder ?: return
        TextComponentEmptyText.setupPlaceholderVisibility(searchTextField.textEditor)
    }

    private fun <T> PopupChooserBuilder<T>.configure(popupOptions: PopupConfig): PopupChooserBuilder<T> {
        val title = popupOptions.title

        if (title != null)
            setTitle(title)

        isFilterAlwaysVisible = popupOptions.alwaysShowSearchField
        setMovable(popupOptions.isMovable)
        setResizable(popupOptions.isResizable)

        return this
    }

}

