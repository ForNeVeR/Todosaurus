package me.fornever.todosaurus.models

import java.net.URI

data class RepositoryModel(val url: URI) {
    val ownerAndName: String
        get() = url.path.removePrefix("/")
    val owner: String
        get() = ownerAndName.substringBefore('/')
    val name: String
        get() = ownerAndName.substringAfter('/')
}
