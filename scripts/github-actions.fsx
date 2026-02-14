let licenseHeader = """
# SPDX-FileCopyrightText: 2000-2021 JetBrains s.r.o.
# SPDX-FileCopyrightText: 2024-2026 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
#
# SPDX-License-Identifier: MIT AND Apache-2.0

# This file is auto-generated.""".Trim()

#r "nuget: Generaptor, 1.11.0"

open Generaptor
open Generaptor.GitHubActions
open type Generaptor.GitHubActions.Commands

let workflows = [

    let workflow name steps =
        workflow name [
            header licenseHeader
            yield! steps
        ]

    let dotNetJob id steps =
        job id [
            setEnv "DOTNET_CLI_TELEMETRY_OPTOUT" "1"
            setEnv "DOTNET_NOLOGO" "1"
            setEnv "NUGET_PACKAGES" "${{ github.workspace }}/.github/nuget-packages"

            step(
                name = "Check out the sources",
                usesSpec = Auto "actions/checkout"
            )
            step(
                name = "Set up .NET SDK",
                usesSpec = Auto "actions/setup-dotnet"
            )
            step(
                name = "Cache NuGet packages",
                usesSpec = Auto "actions/cache",
                options = Map.ofList [
                    "key", "${{ runner.os }}.nuget.${{ hashFiles('**/*.*proj', '**/*.props') }}"
                    "path", "${{ env.NUGET_PACKAGES }}"
                ]
            )

            yield! steps
        ]

    let mainTriggers = [
        onPushTo "main"
        onPushTo "renovate/**"
        onPullRequestTo "main"
        onSchedule "0 0 * * 6"
        onWorkflowDispatch
    ]

    workflow "infra" [
        name "Infrastructure"
        yield! mainTriggers

        dotNetJob "verify-workflows" [
            runsOn "ubuntu-24.04"
            step(run = "dotnet fsi ./scripts/github-actions.fsx verify")
        ]

        job "licenses" [
            runsOn "ubuntu-24.04"
            step(
                name = "Check out the sources",
                usesSpec = Auto "actions/checkout"
            )
            step(
                name = "REUSE license check",
                usesSpec = Auto "fsfe/reuse-action"
            )
        ]

        job "encoding" [
            runsOn "ubuntu-24.04"
            step(
                name = "Check out the sources",
                usesSpec = Auto "actions/checkout"
            )
            step(
                name = "Verify encoding",
                shell = "pwsh",
                run = "Install-Module VerifyEncoding -Repository PSGallery -RequiredVersion 2.2.1 -Force && Test-Encoding"
            )
        ]
    ]

    workflow "dependencies" [
        name "Dependency Updater"
        yield! mainTriggers

        job "main" [
            jobPermission(PermissionKind.Contents, AccessKind.Write)
            jobPermission(PermissionKind.PullRequests, AccessKind.Write)
            runsOn "ubuntu-24.04"
            jobTimeout 15
            step(
                name = "Check out the sources",
                usesSpec = Auto "actions/checkout",
                options = Map.ofList [
                    "fetch-depth", "0"
                ]
            )
            step(
                id = "update",
                usesSpec = Auto "ForNeVeR/intellij-updater",
                name = "Update the dependency versions"
            )
            step(
                condition = "steps.update.outputs.has-changes == 'true' && (github.event_name == 'schedule' || github.event_name == 'workflow_dispatch')",
                name = "Create a PR",
                shell = "pwsh",
                run = "./scripts/New-PR.ps1 -BranchName $env:BRANCH_NAME -CommitMessage $env:COMMIT_MESSAGE -PrTitle $env:PR_TITLE -PrBodyPath $env:PR_BODY_PATH",
                env = Map.ofList [
                    "GITHUB_TOKEN", "${{ secrets.GITHUB_TOKEN }}"
                    "BRANCH_NAME", "${{ steps.update.outputs.branch-name }}"
                    "COMMIT_MESSAGE", "${{ steps.update.outputs.commit-message }}"
                    "PR_TITLE", "${{ steps.update.outputs.pr-title }}"
                    "PR_BODY_PATH", "${{ steps.update.outputs.pr-body-path }}"
                ]
            )
        ]
    ]

    workflow "cli" [
        name "Main"
        yield! mainTriggers

        dotNetJob "check" [
            strategy(failFast = false, matrix = [
                "image", [
                    "macos-15"
                    "ubuntu-24.04"
                    "ubuntu-24.04-arm"
                    "windows-11-arm"
                    "windows-2025"
                ]
            ])
            runsOn "${{ matrix.image }}"

            step(
                name = "Build",
                run = "dotnet build"
            )
            step(
                name = "Test",
                run = "dotnet test",
                timeoutMin = 10
            )
        ]
    ]

    workflow "intellij" [
        name "Build"
        onPushTo "main"
        onPushTo "renovate/**"
        onPullRequest
        workflowConcurrency(
            group = "${{ github.workflow }}-${{ github.event.pull_request.number || github.ref }}",
            cancelInProgress = true
        )
        job "build" [
            jobName "Build"
            runsOn "ubuntu-24.04"
            step(
                name = "Fetch Sources",
                usesSpec = Auto "actions/checkout"
            )
            step(
                name = "Cache downloaded JDK",
                usesSpec = Auto "actions/cache",
                options = Map.ofList [
                    "path", "~/.local/share/gradle-jvm\n~/AppData/Local/gradle-jvm\n"
                    "key", "${{ runner.os }}-${{ hashFiles('gradlew*') }}"
                ]
            )
            step(
                name = "Maximize Build Space",
                usesSpec = Auto "jlumbroso/free-disk-space",
                options = Map.ofList [
                    "tool-cache", "false"
                    "large-packages", "false"
                ]
            )
            step(
                name = "Setup Gradle",
                usesSpec = Auto "gradle/actions/setup-gradle"
            )
            step(
                name = "Build plugin",
                run = "./gradlew buildPlugin"
            )
            step(
                name = "Prepare Plugin Artifact",
                id = "artifact",
                shell = "bash",
                run = "cd ${{ github.workspace }}/build/distributions\nFILENAME=`ls *.zip`\nunzip \"$FILENAME\" -d content\n\necho \"filename=${FILENAME:0:-4}\" >> $GITHUB_OUTPUT\n"
            )
            step(
                name = "Upload artifact",
                usesSpec = Auto "actions/upload-artifact",
                options = Map.ofList [
                    "name", "${{ steps.artifact.outputs.filename }}"
                    "path", "./build/distributions/content/*/*"
                ]
            )
        ]
        job "test" [
            jobName "Test"
            needs "build"
            runsOn "ubuntu-24.04"
            step(
                name = "Fetch Sources",
                usesSpec = Auto "actions/checkout"
            )
            step(
                name = "Maximize Build Space",
                usesSpec = Auto "jlumbroso/free-disk-space",
                options = Map.ofList [
                    "tool-cache", "false"
                    "large-packages", "false"
                ]
            )
            step(
                name = "Setup Gradle",
                usesSpec = Auto "gradle/actions/setup-gradle"
            )
            step(
                name = "Run Tests",
                run = "./gradlew check"
            )
            step(
                name = "Upload Test Results",
                usesSpec = Auto "actions/upload-artifact",
                options = Map.ofList [
                    "name", "${{ runner.os }}.test-results"
                    "path", "*/build/reports/tests"
                ],
                condition = "${{ always() }}"
            )
            step(
                name = "Upload Test Logs",
                usesSpec = Auto "actions/upload-artifact",
                options = Map.ofList [
                    "name", "${{ runner.os }}.test-logs"
                    "path", "*/build/idea-sandbox/*/log-test"
                ],
                condition = "${{ always() }}"
            )
        ]
        job "inspectCode" [
            jobName "Inspect code"
            needs "build"
            runsOn "ubuntu-24.04"
            jobPermission(PermissionKind.Contents, AccessKind.Write)
            jobPermission(PermissionKind.Checks, AccessKind.Write)
            jobPermission(PermissionKind.PullRequests, AccessKind.Write)
            step(
                name = "Maximize Build Space",
                usesSpec = Auto "jlumbroso/free-disk-space",
                options = Map.ofList [
                    "tool-cache", "false"
                    "large-packages", "false"
                ]
            )
            step(
                name = "Fetch Sources",
                usesSpec = Auto "actions/checkout",
                options = Map.ofList [
                    "fetch-depth", "0"
                ]
            )
            step(
                name = "Setup Java",
                usesSpec = Auto "actions/setup-java",
                options = Map.ofList [
                    "distribution", "oracle"
                    "java-version", "21"
                ]
            )
            step(
                name = "Qodana - Code Inspection",
                usesSpec = Auto "JetBrains/qodana-action",
                options = Map.ofList [
                    "cache-default-branch-only", "true"
                ]
            )
        ]
        job "verify" [
            jobName "Verify plugin"
            needs "build"
            runsOn "ubuntu-24.04"
            step(
                name = "Maximize Build Space",
                usesSpec = Auto "jlumbroso/free-disk-space",
                options = Map.ofList [
                    "tool-cache", "false"
                    "large-packages", "false"
                ]
            )
            step(
                name = "Fetch Sources",
                usesSpec = Auto "actions/checkout"
            )
            step(
                name = "Setup Gradle",
                usesSpec = Auto "gradle/actions/setup-gradle"
            )
            step(
                name = "Run Plugin Verification tasks",
                run = "./gradlew :verifyPlugin"
            )
            step(
                name = "Collect Plugin Verifier Result",
                condition = "${{ always() }}",
                usesSpec = Auto "actions/upload-artifact",
                options = Map.ofList [
                    "name", "pluginVerifier-result"
                    "path", "${{ github.workspace }}/build/reports/pluginVerifier"
                ]
            )
        ]
        job "licenses" [
            runsOn "ubuntu-24.04"
            step(
                name = "Check out the sources",
                usesSpec = Auto "actions/checkout"
            )
            step(
                name = "REUSE license check",
                usesSpec = Auto "fsfe/reuse-action"
            )
        ]
    ]

    workflow "release" [
        name "Release"
        yield! mainTriggers
        onPushTags "v*"
        dotNetJob "nuget" [
            jobName "NuGet Package"
            jobPermission(PermissionKind.Contents, AccessKind.Write)
            runsOn "ubuntu-24.04"
            step(
                id = "version",
                name = "Get version",
                shell = "pwsh",
                run = "echo \"version=$(scripts/Get-Version.ps1 -RefName $env:GITHUB_REF)\" >> $env:GITHUB_OUTPUT"
            )
            step(
                run = "dotnet pack ./cli/Todosaurus.slnx --configuration Release -p:Version=${{ steps.version.outputs.version }}"
            )
            step(
                name = "Upload artifacts",
                usesSpec = Auto "actions/upload-artifact",
                options = Map.ofList [
                    "name", "nuget"
                    "path", [
                        "./cli/Cli/bin/Release/FVNever.Todosaurus.Cli.${{ steps.version.outputs.version }}.nupkg"
                        "./cli/Cli/bin/Release/FVNever.Todosaurus.Cli.${{ steps.version.outputs.version }}.snupkg"
                    ] |> String.concat "\n"
                ]
            )
            step(
                condition = "startsWith(github.ref, 'refs/tags/v')",
                name = "Push artifact to NuGet",
                run = "dotnet nuget push ./Cli/bin/Release/FVNever.Todosaurus.Cli.${{ steps.version.outputs.version }}.nupkg --source https://api.nuget.org/v3/index.json --api-key ${{ secrets.NUGET_TOKEN }}"
            )
        ]
        job "intellij" [
            jobName "IntelliJ Plugin"
            runsOn "ubuntu-24.04"
            jobPermission(PermissionKind.Contents, AccessKind.Write)
            step(
                name = "Check out the sources",
                usesSpec = Auto "actions/checkout"
            )
            step(
                name = "Get version",
                id = "version",
                shell = "pwsh",
                run = "echo \"version=$(scripts/Get-Version.ps1 -RefName $env:GITHUB_REF)\" >> $env:GITHUB_OUTPUT"
            )
            step(
                name = "Cache downloaded JDK",
                usesSpec = Auto "actions/cache",
                options = Map.ofList [
                    "path", "~/.local/share/gradle-jvm\n~/AppData/Local/gradle-jvm\n"
                    "key", "${{ runner.os }}-${{ hashFiles('gradlew*') }}"
                ]
            )
            step(
                name = "Maximize Build Space",
                usesSpec = Auto "jlumbroso/free-disk-space",
                options = Map.ofList [
                    "tool-cache", "false"
                    "large-packages", "false"
                ]
            )
            step(
                name = "Setup Gradle",
                usesSpec = Auto "gradle/actions/setup-gradle"
            )
            step(
                name = "Build the plugin",
                shell = "pwsh",
                workingDirectory = "intellij",
                run = "./gradlew buildPlugin"
            )
            step(
                name = "Upload the artifact",
                usesSpec = Auto "actions/upload-artifact",
                options = Map.ofList [
                    "name", "Todosaurus-${{ steps.version.outputs.version }}.zip"
                    "path", "intellij/build/distributions/Todosaurus-${{ steps.version.outputs.version }}.zip"
                ]
            )
            step(
                condition = "startsWith(github.ref, 'refs/tags/v')",
                name = "Publish the plugin",
                env = Map.ofList [
                    "PUBLISH_TOKEN", "${{ secrets.PUBLISH_TOKEN }}"
                ],
                shell = "pwsh",
                workingDirectory = "intellij",
                run = "./gradlew publishPlugin"
            )
        ]
        job "github" [
            jobName "Create a Release"
            jobPermission(PermissionKind.Contents, AccessKind.Write)
            runsOn "ubuntu-24.04"

            needs "nuget"
            needs "intellij"

            step(
                name = "Check out the sources",
                usesSpec = Auto "actions/checkout"
            )
            step(
                name = "Get version",
                id = "version",
                shell = "pwsh",
                run = "echo \"version=$(scripts/Get-Version.ps1 -RefName $env:GITHUB_REF)\" >> $env:GITHUB_OUTPUT"
            )
            step(
                name = "Read the changelog",
                usesSpec = Auto "ForNeVeR/ChangelogAutomation.action",
                options = Map.ofList [
                    "input", "./CHANGELOG.md"
                    "output", "./changelog-section.md"
                ]
            )

            step(
                name = "Download NuGet package",
                usesSpec = Auto "actions/download-artifact",
                options = Map.ofSeq [
                    "name", "nuget"
                    "path", "nuget"
                ]
            )

            step(
                name = "Download IntelliJ plugin",
                usesSpec = Auto "actions/download-artifact",
                options = Map.ofSeq [
                    "name", "Todosaurus-${{ steps.version.outputs.version }}.zip"
                    "path", "intellij/"
                ]
            )

            step(
                name = "Upload the NuGet artifact",
                usesSpec = Auto "actions/upload-artifact",
                options = Map.ofList [
                    "name", "nuget"
                    "path", "nuget/"
                ]
            )
            step(
                name = "Upload the IntelliJ plugin",
                usesSpec = Auto "actions/upload-artifact",
                options = Map.ofList [
                    "name", "intellij"
                    "path", "intellij/Todosaurus-${{ steps.version.outputs.version }}.zip"
                ]
            )
            step(
                name = "Upload the changelog",
                usesSpec = Auto "actions/upload-artifact",
                options = Map.ofList [
                    "name", "changelog-section.md"
                    "path", "./changelog-section.md"
                ]
            )

            step(
                condition = "startsWith(github.ref, 'refs/tags/v')",
                name = "Create a release",
                usesSpec = Auto "softprops/action-gh-release",
                options = Map.ofList [
                    "body_path", "./release-notes.md"
                    "files", [
                        "nuget/FVNever.Todosaurus.Cli.${{ steps.version.outputs.version }}.nupkg"
                        "nuget/FVNever.Todosaurus.Cli.${{ steps.version.outputs.version }}.snupkg"
                        "intellij/Todosaurus-${{ steps.version.outputs.version }}.zip"
                    ] |> String.concat "\n"
                    "name", "Todosaurus v${{ steps.version.outputs.version }}"
                ]
            )
        ]
    ]
]
exit <| EntryPoint.Process fsi.CommandLineArgs workflows
