// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.ui.controls.tagList

import com.intellij.ui.ColorUtil

abstract class TagPresentation<T>(val colorHex: String) {
    abstract val value: T

    abstract val id: Long

    abstract val text: String

    abstract val description: String?

    val colors: TagColors
        = TagColors.adjustColors(ColorUtil.fromHex(colorHex))

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true

        if (other !is TagPresentation<*>)
            return false

        return id == other.id
    }

    override fun hashCode(): Int
        = id.hashCode()
}
