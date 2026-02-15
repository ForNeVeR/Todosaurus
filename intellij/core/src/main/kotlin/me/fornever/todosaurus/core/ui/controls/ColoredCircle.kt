// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.ui.controls

import com.intellij.util.ui.JBUI
import java.awt.Color
import java.awt.Dimension
import javax.swing.JComponent

internal class ColoredCircle(diameter: Int, color: Color) : JComponent() {
    init {
        border = JBUI.Borders.compound(
            RoundedBorder(color, color, 999, 1)
        )

        preferredSize = Dimension(diameter, diameter)
        minimumSize = Dimension(diameter, diameter)
        maximumSize = Dimension(diameter, diameter)

        isOpaque = false
    }
}
