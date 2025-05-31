// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.ui.controls.tagList

import com.intellij.icons.AllIcons
import com.intellij.openapi.ui.popup.IconButton
import com.intellij.ui.InplaceButton
import com.intellij.ui.RoundedLineBorder
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import java.awt.Component
import java.awt.event.ActionListener
import javax.swing.JLabel

class TagRenderer<T>(private val tagList: TagList<T>) : TagRendererBase<T> {
    override fun renderComponent(tagPresentation: TagPresentation<T>, isSelected: Boolean, hasFocus: Boolean): Component
        = panel {
            customizeSpacingConfiguration(EmptySpacingConfiguration()) {
                row {
                    cell(JLabel(tagPresentation.text).apply {
                        font = JBFont.medium()
                        foreground = tagPresentation.foreground
                    })
                    .align(Align.CENTER)
                    .resizableColumn()

                    deselectButton(tagList.deselectTooltipText) {
                        tagList.deselectTag(tagPresentation.value)
                    }
                    .align(Align.CENTER)
                    .customize(UnscaledGaps(left = 5))
                }
            }
        }
        .apply {
            border = JBUI.Borders.compound(
                RoundedLineBorder(tagPresentation.foreground, 25, 1),
                JBUI.Borders.empty(4, 8, 3, 4)
            )
        }

    private fun Row.deselectButton(tooltip: String, listener: ActionListener): Cell<InplaceButton> {
        val iconButton = IconButton(tooltip, AllIcons.Actions.Close, AllIcons.Actions.CloseHovered)
        return cell(InplaceButton(iconButton, listener))
    }
}
