// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.ui.controls

import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.border.LineBorder

class RoundedBorder(
    borderColor: Color,
    private val backgroundColor: Color,
    private val cornerRadius: Int = JBUI.scale(10),
    thickness: Int = JBUI.scale(1),
) : LineBorder(borderColor, thickness) {
    /*
        TODO: Add license information
        Copied from https://github.com/JetBrains/intellij-community/blob/master/platform/platform-impl/src/com/intellij/ui/dsl/builder/components/RoundedLineBorderWithBackground.kt
        I don't understand why they close the API for such extremely useful things
     */
    override fun paintBorder(component: Component, graphics: Graphics, x: Int, y: Int, width: Int, height: Int) {
        val renderer = graphics as Graphics2D

        val oldAntialiasing = renderer.getRenderingHint(RenderingHints.KEY_ANTIALIASING)
        renderer.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        val oldColor = renderer.color
        renderer.color = backgroundColor
        renderer.fillRoundRect(x + thickness - 1, y + thickness - 1,
            width - thickness - thickness + 1,
            height - thickness - thickness + 1,
            cornerRadius, cornerRadius)

        renderer.color = lineColor

        for (offset in 0 until thickness)
            renderer.drawRoundRect(x + offset, y + offset, width - offset - offset - 1, height - offset - offset - 1, cornerRadius, cornerRadius)

        renderer.color = oldColor
        renderer.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntialiasing)
    }

    override fun getBorderInsets(c: Component, insets: Insets) = JBUI.emptyInsets()
}
