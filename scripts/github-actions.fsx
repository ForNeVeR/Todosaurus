let licenseHeader = """
# SPDX-FileCopyrightText: 2024-2025 Friedrich von Never <friedrich@fornever.me>
#
# SPDX-License-Identifier: MIT

# This file is auto-generated.""".Trim()

#r "nuget: Generaptor.Library, 1.8.0"

open Generaptor
open Generaptor.GitHubActions
open type Generaptor.GitHubActions.Commands

let workflows = [

    let setUpDotnet =
        step(
            name = "Set up .NET SDK",
            uses = "actions/setup-dotnet@v4"
        )

    workflow "main" [
        header licenseHeader
        name "Main"
        onPushTo "main"
        onPullRequestTo "main"
        onSchedule "0 0 * * 6"
        onWorkflowDispatch
        job "check" [
            strategy(failFast = false, matrix = [
                "image", [
                    "macos-14"
                    "ubuntu-24.04"
                    "windows-2022"
                ]
            ])
            runsOn "${{ matrix.image }}"
            setEnv "DOTNET_CLI_TELEMETRY_OPTOUT" "1"
            setEnv "DOTNET_NOLOGO" "1"
            setEnv "NUGET_PACKAGES" "${{ github.workspace }}/.github/nuget-packages"
            step(
                uses = "actions/checkout@v4"
            )
            setUpDotnet
            step(
                name = "NuGet cache",
                uses = "actions/cache@v4",
                options = Map.ofList [
                    "key", "${{ runner.os }}.nuget.${{ hashFiles('**/*.csproj') }}"
                    "path", "${{ env.NUGET_PACKAGES }}"
                ]
            )
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
        job "licenses" [
            runsOn "ubuntu-24.04"
            step(
                uses = "actions/checkout@v4"
            )
            step(
                name = "REUSE license check",
                uses = "fsfe/reuse-action@v5"
            )
        ]
        job "encoding" [
            runsOn "ubuntu-24.04"
            step(
                uses = "actions/checkout@v4"
            )
            step(
                name = "Verify encoding",
                shell = "pwsh",
                run = "scripts/Test-Encoding.ps1"
            )
        ]
    ]

    workflow "release" [
        header licenseHeader
        name "Release"
        onPushTo "main"
        onPushTags "v*"
        onPullRequestTo "main"
        onSchedule "0 0 * * 6"
        onWorkflowDispatch
        job "nuget" [
            jobPermission(PermissionKind.Contents, AccessKind.Write)
            runsOn "ubuntu-24.04"
            step(
                uses = "actions/checkout@v4"
            )
            step(
                id = "version",
                name = "Get version",
                shell = "pwsh",
                run = "echo \"version=$(scripts/Get-Version.ps1 -RefName $env:GITHUB_REF)\" >> $env:GITHUB_OUTPUT"
            )
            setUpDotnet
            step(
                run = "dotnet pack --configuration Release -p:Version=${{ steps.version.outputs.version }}"
            )
            step(
                name = "Read changelog",
                uses = "ForNeVeR/ChangelogAutomation.action@v2",
                options = Map.ofList [
                    "output", "./release-notes.md"
                ]
            )
            step(
                name = "Upload artifacts",
                uses = "actions/upload-artifact@v4",
                options = Map.ofList [
                    "path", "./release-notes.md\n./Reuse/bin/Release/Reuse.${{ steps.version.outputs.version }}.nupkg\n./Reuse/bin/Release/Reuse.${{ steps.version.outputs.version }}.snupkg"
                ]
            )
            step(
                condition = "startsWith(github.ref, 'refs/tags/v')",
                name = "Create a release",
                uses = "softprops/action-gh-release@v2",
                options = Map.ofList [
                    "body_path", "./release-notes.md"
                    "files", "./Reuse/bin/Release/Reuse.${{ steps.version.outputs.version }}.nupkg\n./Reuse/bin/Release/Reuse.${{ steps.version.outputs.version }}.snupkg"
                    "name", "dotnet-reuse v${{ steps.version.outputs.version }}"
                ]
            )
            step(
                condition = "startsWith(github.ref, 'refs/tags/v')",
                name = "Push artifact to NuGet",
                run = "dotnet nuget push ./Reuse/bin/Release/Reuse.${{ steps.version.outputs.version }}.nupkg --source https://api.nuget.org/v3/index.json --api-key ${{ secrets.NUGET_TOKEN }}"
            )
        ]
    ]

    workflow "docs" [
        header licenseHeader
        name "Docs"
        onPushTo "main"
        onWorkflowDispatch
        workflowPermission(PermissionKind.Actions, AccessKind.Read)
        workflowPermission(PermissionKind.Pages, AccessKind.Write)
        workflowPermission(PermissionKind.IdToken, AccessKind.Write)
        workflowConcurrency(
            group = "pages",
            cancelInProgress = false
        )
        job "publish-docs" [
            environment(name = "github-pages", url = "${{ steps.deployment.outputs.page_url }}")
            runsOn "ubuntu-22.04"
            step(
                name = "Checkout",
                uses = "actions/checkout@v4"
            )
            setUpDotnet
            step(
                run = "dotnet tool restore"
            )
            step(
                run = "dotnet docfx docs/docfx.json"
            )
            step(
                name = "Upload artifact",
                uses = "actions/upload-pages-artifact@v3",
                options = Map.ofList [
                    "path", "docs/_site"
                ]
            )
            step(
                name = "Deploy to GitHub Pages",
                id = "deployment",
                uses = "actions/deploy-pages@v4"
            )
        ]
    ]
]
EntryPoint.Process fsi.CommandLineArgs workflows
