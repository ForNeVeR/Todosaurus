// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT
package me.fornever.todosaurus.core.ui.controls.tagList

import java.awt.Component

interface TagRendererBase<Tag> {
    fun renderComponent(tagPresentation: TagPresentation<Tag>, isSelected: Boolean, hasFocus: Boolean): Component
}
