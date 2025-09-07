// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.ui.controls.tagList

import com.intellij.CommonBundle
import com.intellij.collaboration.ui.codereview.list.search.PopupConfig
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.EDT
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.JBColor
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.ActionLink
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.WrapLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.fornever.todosaurus.core.TodosaurusCoreBundle
import me.fornever.todosaurus.core.ui.controls.tagList.chooserPopup.PopupTagRenderer
import me.fornever.todosaurus.core.ui.controls.tagList.chooserPopup.TagsChooserPopup
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.util.concurrent.CancellationException
import javax.swing.JLabel
import javax.swing.JPanel

open class TagList<Key, Tag>(
    private val scope: CoroutineScope,
    private val presentationFactory: (Tag) -> TagPresentation<Tag>,
    private val keySelector: (Tag) -> Key,
    private val rendererFactory: (TagList<Key, Tag>) -> TagRenderer<Key, Tag>): JPanel() {

    private var userInterface: TagList<Key, Tag>.UserInterface = Regular()
    private var tagProvider: suspend () -> Iterable<Tag> = { emptyList() }
    private var afterFetch: () -> Unit = { }
    private val keyMap: MutableMap<Key, Tag> = mutableMapOf()
    private val tagsPool: MutableMap<Tag, TagPresentation<Tag>> = mutableMapOf()
    private val selectedTags: HashMap<Tag, Component> = hashMapOf()
    private val noTagsLabel: JLabel
    private val selectLink: ActionLink
    private val deselectAllLink: ActionLink

    var searchTooltipText: String = TodosaurusCoreBundle.message("tagList.popup.search.tooltip")
    var deselectTooltipText: String = TodosaurusCoreBundle.message("tagList.deselect.tooltip")

    var noTagsText: String
        get() = noTagsLabel.text
        set(value) {
            noTagsLabel.text = value
        }

    var selectText: String
        get() = selectLink.text
        set(value) {
            selectLink.text = value
        }

    var clearAllText: String
        get() = deselectAllLink.text
        set(value) {
            deselectAllLink.text = value
        }

    private val tagRenderer: TagRendererBase<Tag> by lazy {
        rendererFactory(this)
    }

    init {
        layout = WrapLayout(FlowLayout.LEADING, JBUI.scale(5), JBUI.scale(5))

        noTagsLabel = JLabel(TodosaurusCoreBundle.message("tagList.noTags.text")).apply {
            foreground = UIUtil.getLabelInfoForeground()
        }

        selectLink = ActionLink(TodosaurusCoreBundle.message("tagList.select.text")) { linkEvent ->
            val link = linkEvent.source as? ActionLink
                ?: return@ActionLink

            val popupPosition = RelativePoint.getSouthOf(link)

            scope.launch(Dispatchers.EDT) {
                userInterface.showPopupIn(popupPosition)
            }
        }
        .apply {
            setDropDownLinkIcon()
            border = JBUI.Borders.empty(0, 20, 0, 0)
            autoHideOnDisable = false
        }

        deselectAllLink = ActionLink(TodosaurusCoreBundle.message("tagList.deselectAll.text")) {
            deselectAllTags()
        }
        .apply {
            autoHideOnDisable = false
        }

        add(noTagsLabel)
        add(selectLink)
        add(deselectAllLink)

        setUI(Empty())
    }

    fun getSelectedTags(): List<Tag>
        = selectedTags.keys.toList()

    final override fun add(component: Component?): Component {
        return super.add(component)
    }

    override fun getPreferredSize(): Dimension
        = super.getPreferredSize().also { it.width = 0 }

    override fun getMinimumSize(): Dimension
        = Dimension(0, super.getMinimumSize().height)

    private fun setUI(userInterface: TagList<Key, Tag>.UserInterface) {
        this.userInterface = userInterface
        this.userInterface.update()
    }

    fun fetchTagsUsing(tagProvider: suspend () -> Iterable<Tag>, afterFetch: () -> Unit) {
        this.tagProvider = tagProvider
        this.afterFetch = afterFetch
        setUI(Loading())
    }

    fun extractTag(key: Key): Tag?
        = keyMap[key]

    fun selectTag(tag: Tag) {
        addSelectionFor(tag)

        if (userInterface !is Loading)
            userInterface.update()
    }

    /*
        Remarks: Updates the user interface once after all tags have been created
     */
    fun selectTags(tags: Iterable<Tag>) {
        tags.forEach {
            addSelectionFor(it)
        }

        if (userInterface !is Loading)
            userInterface.update()
    }

    private fun addSelectionFor(tag: Tag) {
        if (selectedTags.contains(tag))
            return

        val tagPresentation = tagsPool[tag]
            ?: error("Tag should be added to list")

        val tagComponent = tagRenderer.renderComponent(tagPresentation, isSelected = false, hasFocus = false)
        selectedTags[tag] = add(tagComponent, selectedTags.size)
    }

    fun deselectTag(tag: Tag) {
        removeSelectionFor(tag)

        if (userInterface !is Loading)
            userInterface.update()
    }

    /*
        Remarks: Updates the user interface once after all tags have been removed
     */
    fun deselectTags(tags: Iterable<Tag>) {
        tags.forEach {
            removeSelectionFor(it)
        }

        if (userInterface !is Loading)
            userInterface.update()
    }

    private fun removeSelectionFor(tag: Tag) {
        if (!selectedTags.contains(tag))
            return

        remove(selectedTags.remove(tag))
    }

    /*
        Remarks: Removes all tags in a more optimal way
     */
    fun deselectAllTags() {
        repeat(selectedTags.size) {
            remove(0)
        }

        selectedTags.clear()

        if (userInterface !is Loading)
            userInterface.update()
    }

    private abstract inner class UserInterface {
        abstract fun update()
        abstract suspend fun showPopupIn(position: RelativePoint)
    }

    private inner class Empty : UserInterface() {
        override fun update() {
            if (tagsPool.isNotEmpty())
                return setUI(Regular())

            noTagsLabel.isVisible = true
            selectLink.isEnabled = true
            selectLink.setDropDownLinkIcon()
            deselectAllLink.isEnabled = false
        }

        override suspend fun showPopupIn(position: RelativePoint) {
            lateinit var popup: JBPopup

            val panel = panel {
                row {
                    label(CommonBundle.message("empty.menu.filler"))
                        .align(Align.CENTER)
                        .applyToComponent {
                            foreground = UIUtil.getLabelInfoForeground()
                        }
                }

                row {
                    link(TodosaurusCoreBundle.message("tagList.popup.refresh.tooltip")) {
                        popup.cancel()
                        setUI(Loading())
                        selectLink.doClick()
                    }
                    .align(Align.CENTER)
                }
            }
            .apply {
                border = JBUI.Borders.empty(15)
            }

            JBPopupFactory
                .getInstance()
                .createComponentPopupBuilder(panel, null)
                .createPopup()
                .also { popup = it }
                .show(position)
        }
    }

    private inner class Regular : UserInterface() {
        override fun update() {
            if (tagsPool.isEmpty())
                return setUI(Empty())

            val selectionBlank = selectedTags.isEmpty()
            noTagsLabel.isVisible = selectionBlank
            selectLink.isEnabled = true
            selectLink.setDropDownLinkIcon()
            deselectAllLink.isEnabled = !selectionBlank

            revalidate()
            repaint()
        }

        @Suppress("UnstableApiUsage")
        override suspend fun showPopupIn(position: RelativePoint) {
            val popupOptions = PopupConfig(
                alwaysShowSearchField = tagsPool.isNotEmpty(),
                searchTextPlaceHolder = searchTooltipText,
                isResizable = true)

            val popupTags = TagsChooserPopup.showTagsChooserPopup(
                tagsPool,
                PopupTagRenderer(),
                { selectedTags.contains(it) },
                position,
                popupOptions)

            deselectTags(selectedTags.keys.filter { !popupTags.contains(it) })
            selectTags(popupTags)
        }
    }

    private inner class Loading : UserInterface() {
        private var popup: JBPopup? = null

        override fun update() {
            val selectedBeforeLoading = selectedTags.keys

            deselectAllTags()

            keyMap.clear()
            tagsPool.clear()

            noTagsLabel.isVisible = true
            deselectAllLink.isEnabled = false
            selectLink.isEnabled = true
            selectLink.setIcon(AnimatedIcon.Default(), true)

            scope.launch(Dispatchers.IO) {
                val tags = try {
                    tagProvider()
                }
                catch (exception: CancellationException) {
                    throw exception
                }
                catch (throwable: Throwable) {
                    val errorReason = throwable.localizedMessage
                        ?: throwable.message
                            ?: TodosaurusCoreBundle.message("tagList.popup.error.tooltip")

                    return@launch withContext(Dispatchers.EDT) {
                        updateUiThenReopenPopup(Error(errorReason))
                    }
                }

                withContext(Dispatchers.EDT) {
                    tags.forEach {
                        if (selectedBeforeLoading.contains(it))
                            addSelectionFor(it)

                        keyMap[keySelector(it)] = it
                        tagsPool[it] = presentationFactory(it)
                    }

                    afterFetch()

                    updateUiThenReopenPopup(Regular())
                }
            }
        }

        private fun updateUiThenReopenPopup(userInterface: UserInterface) {
            popup?.cancel()

            setUI(userInterface)

            popup?.apply {
                selectLink.doClick()
            }
        }

        override suspend fun showPopupIn(position: RelativePoint) {
            val panel = panel {
                row {
                    label(TodosaurusCoreBundle.message("tagList.popup.loading.tooltip"))
                        .align(Align.CENTER)
                        .applyToComponent {
                            icon = AnimatedIcon.Default()
                            foreground = UIUtil.getLabelInfoForeground()
                        }
                }
            }
            .apply {
                border = JBUI.Borders.empty(15)
            }

            JBPopupFactory
                .getInstance()
                .createComponentPopupBuilder(panel, null)
                .createPopup()
                .also { popup = it }
                .show(position)
        }
    }

    private inner class Error(private val reason: String) : UserInterface() {
        override fun update() {
            selectLink.isEnabled = true
            deselectAllLink.isEnabled = false
            selectLink.setIcon(AllIcons.General.Error, false)
        }

        override suspend fun showPopupIn(position: RelativePoint) {
            lateinit var popup: JBPopup

            val panel = panel {
                row {
                    label(reason) // TODO: Add support for multiline (\n) text
                        .align(Align.CENTER)
                        .applyToComponent {
                            icon = AllIcons.General.Error
                            foreground = JBColor.RED
                        }
                }

                row {
                    link(TodosaurusCoreBundle.message("tagList.popup.tryAgain.tooltip")) {
                        popup.cancel()
                        setUI(Loading())
                        selectLink.doClick()
                    }
                    .align(Align.CENTER)
                }
            }
            .apply {
                border = JBUI.Borders.empty(15)
            }

            JBPopupFactory
                .getInstance()
                .createComponentPopupBuilder(panel, null)
                .createPopup()
                .also { popup = it }
                .show(position)
        }
    }
}
