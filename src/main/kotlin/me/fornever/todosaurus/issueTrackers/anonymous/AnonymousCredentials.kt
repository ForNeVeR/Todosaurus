package me.fornever.todosaurus.issueTrackers.anonymous

import com.google.common.base.Objects
import com.intellij.collaboration.api.ServerPath
import me.fornever.todosaurus.issueTrackers.IssueTrackerCredentials

class AnonymousCredentials(override val serverPath: ServerPath) : IssueTrackerCredentials {
    override val id: String
        get() = Objects.hashCode(username).toString()

    override val username: String
        get() = "Anonymous"
}
