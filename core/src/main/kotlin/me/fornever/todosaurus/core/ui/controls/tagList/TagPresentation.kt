// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.ui.controls.tagList

import java.awt.Color

abstract class TagPresentation<T> {
    abstract val value: T

    abstract val id: Long

    abstract val text: String

    abstract val description: String?

    abstract val foreground: Color

    override fun equals(other: Any?): Boolean
        = other is TagPresentation<*> && other.id == id

    override fun hashCode(): Int
        = id.hashCode()
}
