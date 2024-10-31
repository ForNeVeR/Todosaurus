// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issueTrackers

open class TestConnectionResult {
    class Success : TestConnectionResult()

    class Failed(val reason: String? = "Unexpected error") : TestConnectionResult()
}
