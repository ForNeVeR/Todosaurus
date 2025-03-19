// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.ui.controls.tagList.chooserPopup

import com.intellij.collaboration.ui.codereview.details.SelectableWrapper
import com.intellij.ide.plugins.newui.ListPluginComponent
import com.intellij.openapi.ui.popup.util.PopupUtil
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.EmptySpacingConfiguration
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.ui.popup.list.SelectablePanel
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.NamedColorUtil
import com.intellij.util.ui.UIUtil
import me.fornever.todosaurus.core.ui.controls.ColoredCircle
import me.fornever.todosaurus.core.ui.controls.tagList.TagPresentation
import me.fornever.todosaurus.core.ui.controls.tagList.TagRendererBase
import java.awt.Component

@Suppress("UnstableApiUsage")
internal class PopupTagRenderer<T> : TagRendererBase<SelectableWrapper<T>> {
    override fun renderComponent(tagPresentation: TagPresentation<SelectableWrapper<T>>, isSelected: Boolean, hasFocus: Boolean): Component {
        val content = panel {
			customizeSpacingConfiguration(EmptySpacingConfiguration()) {
                val checkBox = JBCheckBox()
				val coloredCircle = ColoredCircle(diameter = 12, tagPresentation.foreground)

				row {
					cell(checkBox)
						.customize(UnscaledGaps(right = 8))
                        .applyToComponent {
                            this.isSelected = tagPresentation.value.isSelected
                            isFocusPainted = hasFocus
                            isFocusable = hasFocus
                            isOpaque = false
                        }

					cell(coloredCircle)
						.customize(UnscaledGaps(right = 8))

					label(tagPresentation.text)
						.applyToComponent {
							foreground = if (isSelected) NamedColorUtil.getListSelectionForeground(true) else UIUtil.getListForeground()
						}
				}

				tagPresentation.description?.let {
					row {
						label(it)
							.customize(UnscaledGaps(top = 4, left = 44)) // TODO: Left gap is a dirty hack. We have to find another way to align the description strictly under the title
							.applyToComponent {
								font = JBFont.medium()
								foreground = UIUtil.getLabelInfoForeground()
							}
					}
				}
			}
		}
        .apply {
            border = JBUI.Borders.empty(8, 0)
            isOpaque = false
        }

        return SelectablePanel
            .wrap(content, JBUI.CurrentTheme.Popup.BACKGROUND)
            .apply {
                PopupUtil.configListRendererFlexibleHeight(this@apply)

                if (isSelected)
                    selectionColor = ListPluginComponent.SELECTION_COLOR
            }
    }
}
