// SPDX-FileCopyrightText: 2024-2025 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

#r "nuget: Generaptor.Library, 1.6.1"

open System
open Generaptor
open Generaptor.GitHubActions
open type Generaptor.GitHubActions.Commands
open type Generaptor.Library.Actions
open type Generaptor.Library.Patterns

let mainBranch = "main"
let ubuntu = "ubuntu-24.04"
let images = [
    "macos-14"
    ubuntu
    "windows-2022"
]

let workflows = [
    let mainTriggers = [
        onPushTo mainBranch
        onPullRequestTo mainBranch
        onSchedule(day = DayOfWeek.Saturday)
        onWorkflowDispatch
    ]

    workflow "main" [
        name "Main"
        yield! mainTriggers

        job "check" [
            checkout
            yield! dotNetBuildAndTest(projectFileExtensions = [".csproj"])
        ] |> addMatrix images

        job "licenses" [
            runsOn ubuntu
            checkout

            step(name = "REUSE license check", uses = "fsfe/reuse-action@v5")
        ]


        job "encoding" [
            runsOn ubuntu
            checkout

            step(name = "Verify encoding", shell = "pwsh", run = "scripts/Test-Encoding.ps1")
        ]
    ]

    workflow "release" [
        name "Release"
        yield! mainTriggers
        onPushTags "v*"
        job "nuget" [
            runsOn ubuntu
            checkout
            writeContentPermissions

            let configuration = "Release"

            let versionStepId = "version"
            let versionField = "${{ steps." + versionStepId + ".outputs.version }}"
            getVersionWithScript(stepId = versionStepId, scriptPath = "scripts/Get-Version.ps1")
            dotNetPack(version = versionField)

            let releaseNotes = "./release-notes.md"
            step(
                name = "Read changelog",
                uses = "ForNeVeR/ChangelogAutomation.action@v2",
                options = Map.ofList [
                    "output", releaseNotes
                ]
            )

            let artifacts projectName includeSNuPkg = [
                let packageId = projectName
                $"./{projectName}/bin/{configuration}/{packageId}.{versionField}.nupkg"
                if includeSNuPkg then $"./{projectName}/bin/{configuration}/{packageId}.{versionField}.snupkg"
            ]
            let allArtifacts = [
                yield! artifacts "Reuse" true
            ]
            uploadArtifacts [
                releaseNotes
                yield! allArtifacts
            ]
            yield! ifCalledOnTagPush [
                createRelease(
                    name = $"dotnet-reuse v{versionField}",
                    releaseNotesPath = releaseNotes,
                    files = allArtifacts
                )
                yield! pushToNuGetOrg "NUGET_TOKEN" (
                    artifacts "Reuse" false
                )
            ]
        ]
    ]
]

exit <| EntryPoint.Process fsi.CommandLineArgs workflows
