// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.git

import java.net.URI
import java.nio.file.Path

data class GitHostingRemote(val url: URI, val rootPath: Path) {
    val ownerAndName: String
        get() = url.path.removePrefix("/")

    val owner: String
        get() = ownerAndName.substringBefore('/')

    val name: String
        get() = ownerAndName.substringAfter('/')
}
