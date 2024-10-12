package me.fornever.todosaurus.vcs.git

import java.net.URI
import java.nio.file.Path

data class GitRemote(val url: URI, val rootPath: Path) {
    val ownerAndName: String
        get() = url.path.removePrefix("/")

    val owner: String
        get() = ownerAndName.substringBefore('/')

    val name: String
        get() = ownerAndName.substringAfter('/')
}
