package me.fornever.todosaurus.settings

import com.intellij.util.xml.ConvertContext
import com.intellij.util.xml.Converter
import kotlinx.coroutines.runBlocking
import me.fornever.todosaurus.issueTrackers.IssueTrackerCredentials
import me.fornever.todosaurus.issueTrackers.IssueTrackerCredentialsProviderFactory

class IssueTrackerCredentialsConverter : Converter<IssueTrackerCredentials>() {
    private val todosaurusSettings: TodosaurusSettings = TodosaurusSettings.getInstance()

    override fun toString(credentials: IssueTrackerCredentials?, context: ConvertContext): String? {
        if (credentials == null)
            return null

        return credentials.id
    }

    override fun fromString(credentials: String?, context: ConvertContext): IssueTrackerCredentials? {
        if (credentials == null)
            return null

        val issueTracker = todosaurusSettings.state.issueTracker ?: return null

        return runBlocking {
            IssueTrackerCredentialsProviderFactory
                .getInstance(context.project)
                .create(issueTracker)
                .provide(credentials)
        }
    }
}
