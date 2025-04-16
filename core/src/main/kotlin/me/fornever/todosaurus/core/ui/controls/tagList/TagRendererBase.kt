// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT
package me.fornever.todosaurus.core.ui.controls.tagList

import java.awt.Component

interface TagRendererBase<T> {
    fun renderComponent(tagPresentation: TagPresentation<T>, isSelected: Boolean, hasFocus: Boolean): Component
}
