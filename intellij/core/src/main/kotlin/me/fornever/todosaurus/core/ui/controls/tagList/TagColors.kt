// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.ui.controls.tagList

import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.util.ui.UIUtil
import java.awt.Color

data class TagColors(
    val baseColor: Color,
    val backgroundColor: Color,
    val borderColor: Color,
    val foregroundColor: Color) {
    companion object {
        /*
            TODO[#232]: It seems that the color of UIUtil.getPanelBackground() is not updated after changing the theme in the IDE.
            TODO[#232]: We need to calculate colors for light and dark themes exactly once.
            TODO[#232]: Expand the cache so that it stores colors for dark and light themes separately (currently, the cache only stores colors for one theme).
         */
        private val themeColor: Color = UIUtil.getPanelBackground()
        private val cache: MutableMap<Color, TagColors> = mutableMapOf()

        fun adjustColors(baseColor: Color): TagColors {
            val cachedColors = cache[baseColor]

            if (cachedColors != null)
                return cachedColors

            val calculatedColors = if (JBColor.isBright()) calculateForBrightTheme(baseColor) else calculateForDarkTheme(baseColor)
            cache[baseColor] = calculatedColors

            return calculatedColors
        }

        private fun calculateForBrightTheme(baseColor: Color): TagColors {
            val themeContrast = ColorUtil.getContrast(themeColor, baseColor)
            val blackContrast = ColorUtil.getContrast(Color.BLACK, baseColor)
            val foregroundColor = if (themeContrast >= blackContrast) themeColor else Color.BLACK
            val borderColor = if (ColorUtil.getContrast(baseColor, themeColor) < 4.5) ColorUtil.darker(baseColor, 2) else baseColor
            return TagColors(baseColor, baseColor, borderColor, foregroundColor)
        }

        private fun calculateForDarkTheme(baseColor: Color): TagColors {
            val backgroundColor = ColorUtil.mix(themeColor, baseColor, 0.2)
            val borderColor = ColorUtil.brighter(backgroundColor, 8)
            val foregroundColor = ColorUtil.brighter(backgroundColor, 16)
            return TagColors(baseColor, backgroundColor, borderColor, foregroundColor)
        }
    }
}
