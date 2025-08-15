// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.ui.controls

import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.JComponent
import javax.swing.border.LineBorder

internal class ColoredCircle(diameter: Int, color: Color) : JComponent() {
    private class Border(
        color: Color,
        private val bgColor: Color,
        private val arcSize: Int = JBUI.scale(10),
        thickness: Int = JBUI.scale(1),
    ) : LineBorder(color, thickness) {
        /*
            TODO: Add license information
            Copied from https://github.com/JetBrains/intellij-community/blob/master/platform/platform-impl/src/com/intellij/ui/dsl/builder/components/RoundedLineBorderWithBackground.kt
            I don't understand why they close the API for such extremely useful things
         */
        override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
            val g2 = g as Graphics2D

            val oldAntialiasing = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING)
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            val oldColor = g2.color

            g2.color = bgColor
            g2.fillRoundRect(x + thickness - 1, y + thickness - 1,
                width - thickness - thickness + 1,
                height - thickness - thickness + 1,
                arcSize, arcSize)

            g2.color = lineColor
            for (i in 0 until thickness) {
                g2.drawRoundRect(x + i, y + i, width - i - i - 1, height - i - i - 1, arcSize, arcSize)
            }

            g2.color = oldColor
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntialiasing)
        }

        override fun getBorderInsets(c: Component, insets: Insets) = JBUI.emptyInsets()
    }

    init {
        border = JBUI.Borders.compound(
            Border(color, color, 999, 1)
        )

        preferredSize = Dimension(diameter, diameter)
        minimumSize = Dimension(diameter, diameter)
        maximumSize = Dimension(diameter, diameter)

        isOpaque = false
    }
}
