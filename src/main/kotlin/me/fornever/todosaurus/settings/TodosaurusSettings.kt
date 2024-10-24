package me.fornever.todosaurus.settings

import com.intellij.openapi.components.*
import com.intellij.util.xml.Convert
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.OptionTag
import me.fornever.todosaurus.issueTrackers.IssueTracker
import me.fornever.todosaurus.issueTrackers.IssueTrackerCredentials
import me.fornever.todosaurus.issues.IssuePlacementDetails
import me.fornever.todosaurus.vcs.git.GitRemote

@Service(Service.Level.APP)
@State(
    name = "me.fornever.todosaurus.settings.TodosaurusSettings",
    storages = [Storage("TodosaurusSettings.xml")]
)
class TodosaurusSettings : SimplePersistentStateComponent<TodosaurusSettings.State>(State.defaultState) {
    companion object {
        const val ISSUE_NUMBER_REPLACEMENT = "{ISSUE_NUMBER}"
        const val URL_REPLACEMENT = "{URL_REPLACEMENT}"

        fun getInstance(): TodosaurusSettings = service()
    }

    class State : BaseState() {
        companion object {
            private const val DEFAULT_NUMBER_PATTERN = "[#$ISSUE_NUMBER_REPLACEMENT]:"
            private const val DEFAULT_DESCRIPTION_TEMPLATE = """
                See the code near this line: $URL_REPLACEMENT

                Also, look for the number of this issue in the project code base.
            """

            val defaultState: State = State()
        }

        var numberPattern: String = DEFAULT_NUMBER_PATTERN
        var descriptionTemplate: String = DEFAULT_DESCRIPTION_TEMPLATE

        @get:Convert(IssueTrackerConverter::class)
        @set:Convert(IssueTrackerConverter::class)
        var issueTracker: IssueTracker? = null

        @get:Convert(IssueTrackerCredentialsConverter::class)
        @set:Convert(IssueTrackerCredentialsConverter::class)
        var credentials: IssueTrackerCredentials? = null

        @get:Convert(GitRemoteConverter::class)
        @set:Convert(GitRemoteConverter::class)
        var gitRemote: GitRemote? = null

        fun hasCredentials()
            = issueTracker != null && credentials != null && gitRemote != null
    }
}
