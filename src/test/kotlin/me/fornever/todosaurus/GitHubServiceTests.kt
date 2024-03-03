package me.fornever.todosaurus

import com.intellij.mock.MockDocument
import com.intellij.mock.MockFileDocumentManagerImpl
import com.intellij.mock.MockProject
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.project.Project
import com.intellij.openapi.rd.createNestedDisposable
import com.jetbrains.rd.util.lifetime.Lifetime
import com.jetbrains.rd.util.lifetime.LifetimeDefinition
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import me.fornever.todosaurus.models.CreateIssueModel
import me.fornever.todosaurus.models.RepositoryModel
import me.fornever.todosaurus.services.GitHubService
import me.fornever.todosaurus.testFramework.*
import org.jetbrains.plugins.github.authentication.accounts.GithubAccount
import java.net.URI
import kotlin.io.path.Path

class GitHubServiceTests : TestCase() {

    private lateinit var ld: LifetimeDefinition
    private val lifetime: Lifetime
        get() = ld.lifetime

    private lateinit var project: Project
    private val globalLock = MockGlobalLock()
    private val tokenStorage = MockGitHubTokenStorage()
    private val gitHubApi = MockGitHubApi()
    private val fileDocumentManager = MockFileDocumentManagerImpl(null) { error("Not implemented") }
    private lateinit var service: GitHubService

    override fun setUp() {
        super.setUp()
        ld = LifetimeDefinition()
        project = MockProject(null, lifetime.createNestedDisposable("GitHubServiceTests"))
        service = GitHubService(project, globalLock, tokenStorage, gitHubApi, lazy { fileDocumentManager })
    }
    override fun tearDown() {
        ld.terminate()
        super.tearDown()
    }

    fun testRepositoryNotSelected() {
        val model = stubModel()
        runBlocking {
            assertThrows<IllegalStateException>("Repository is not selected.") {
                service.createIssue(model)
            }
        }
    }

    fun testAccountNotSelected() {
        val model = stubModel().copy(selectedRepository = RepositoryModel(URI("https://example.com"), Path("/")))
        runBlocking {
            assertThrows<IllegalStateException>("Account is not selected.") {
                service.createIssue(model)
            }
        }
    }

    fun testTokenNotFound() {
        val account = GithubAccount()
        val model = stubModel().copy(
            selectedRepository = RepositoryModel(URI("https://example.com"), Path("/")),
            selectedAccount = account,
        )
        runBlocking {
            assertThrows<IllegalStateException>("Token is not found.") {
                service.createIssue(model)
            }
        }
    }

    fun testCreateIssueNoFileForDocument() {
        val account = GithubAccount()
        val model = stubModel().copy(
            selectedRepository = RepositoryModel(URI("https://example.com"), Path("/")),
            selectedAccount = account,
            textRangeMarker = mockRangeMarker(MockDocument())
        )
        tokenStorage.putToken(account, "token")
        runBlocking {
            assertThrows<IllegalStateException>("Cannot find file for the requested document.") {
                service.createIssue(model)
            }
        }
    }

    fun testCreateIssueNoRelativePath() {
        fail()
    }

    fun testCreateIssueNoRootFile() {
        fail()
    }

    fun testCreateIssueNoRepository() {
        fail()
    }

    fun testCreateIssueNoRevision() {
        fail()
    }

    fun testCreateIssueOneLine() {
        fail()
    }

    fun testCreateIssueSeveralLines() {
        fail()
    }

    fun testCreateIssueTemplateReplacement() {
        fail()
    }

    private fun stubModel() = CreateIssueModel(
        selectedRepository = null,
        selectedAccount = null,
        title = "title",
        description = "description",
        textRangeMarker = MockRangeMarker()
    )

    private fun mockRangeMarker(document: Document): RangeMarker = MockRangeMarker(document)
}
