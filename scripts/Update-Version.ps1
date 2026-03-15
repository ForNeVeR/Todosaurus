# SPDX-FileCopyrightText: 2025-2026 Friedrich von Never <friedrich@fornever.me>
#
# SPDX-License-Identifier: MIT

param (
    $NewVersion = '1.10.1',
    $RepoRoot = "$PSScriptRoot/.."
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

function Update-PowerShellFile($relativePath) {
    $file = Resolve-Path "$RepoRoot/$relativePath"
    $oldContent = [IO.File]::ReadAllText($file)
    $newContent = $oldContent -replace "\`$NewVersion = '[\d.]*?'", "`$NewVersion = '$NewVersion'"
    [IO.File]::WriteAllText($file, $newContent)
    Write-Output "Updated file `"$file`"."
}

function Update-PropertiesFile($relativePath, $propName) {
    $file = Resolve-Path "$RepoRoot/$relativePath"
    $oldContent = [IO.File]::ReadAllText($file)
    $regex = [Regex]::Escape($propName)
    $newContent = $oldContent -replace "$regex=.*?(\n|$)", "$propname=$NewVersion`n"
    [IO.File]::WriteAllText($file, $newContent)
    Write-Output "Updated file `"$file`"."
}

function Update-PropsFile($relativePath, $propName) {
    $file = Resolve-Path "$RepoRoot/$relativePath"

    # NOTE: I really tried to play nice and load the file via [xml], but didn't find a way to preserve the
    #       formatting. PreserveWhitespace = $true helps a lot, but still doesn't preserve the line breaks between
    #       attributes on the same node.
    $oldContent = [IO.File]::ReadAllText($file)
    $regex = [Regex]::Escape($propName)
    $newContent = $oldContent -replace "<($regex)( ?.*?)>.*?</$regex>", "<`$1`$2>$NewVersion</`$1>"
    [IO.File]::WriteAllText($file, $newContent)
    Write-Output "Updated file `"$file`"."
}

function Update-ActionYaml($relativePath) {
    $file = Resolve-Path "$RepoRoot/$relativePath"
    $oldContent = [IO.File]::ReadAllText($file)
    $newContent = $oldContent -replace '(default:\s*")\d+\.\d+\.\d+(")', "`$1$NewVersion`$2"
    [IO.File]::WriteAllText($file, $newContent)
    Write-Output "Updated file `"$file`"."
}

function Update-MarkdownFile($relativePath, $actionName) {
    $file = Resolve-Path "$RepoRoot/$relativePath"
    $oldContent = [IO.File]::ReadAllText($file)
    $regex = [Regex]::Escape($actionName)
    $majorVersion = $NewVersion.Substring(0, $NewVersion.IndexOf('.'))
    $newContent = $oldContent -replace "$regex@v([^\s]+)", "$regex@v$majorVersion"
    $newContent = $newContent -replace '`\d+\.\d+\.\d+`', "``$NewVersion``"
    [IO.File]::WriteAllText($file, $newContent)
    Write-Output "Updated file `"$file`"."
}

Update-PowerShellFile 'scripts/Update-Version.ps1'
Update-PropsFile 'cli/Directory.Build.props' 'Version'
Update-PropertiesFile 'intellij/gradle.properties' 'pluginVersion'
Update-MarkdownFile 'action/README.md' 'ForNeVeR/Todosaurus/action'
Update-ActionYaml 'action/action.yml'
