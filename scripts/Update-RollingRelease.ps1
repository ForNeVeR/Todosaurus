# SPDX-FileCopyrightText: 2025 Friedrich von Never <friedrich@fornever.me>
#
# SPDX-License-Identifier: MIT

param (
    [string] $Version
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$dotIndex = $Version.IndexOf('.')
if ($dotIndex -le 0) {
    throw "Cannot determine version base from version: `"$Version`"."
}
$versionBase = $Version.Substring(0, $dotIndex)
$tag = "v$versionBase"
Write-Output "Determined the rolling version tag `"$tag`"."

Write-Output "Force-updating a local tag `"$tag`"…"
git tag --force $tag
if (!$?) {
    throw "Exit code from git tag: $LASTEXITCODE."
}

Write-Output "Force-pushing the tag `"$tag`"…"
git push --force origin $tag
if (!$?) {
    throw "Exit code from git push: $LASTEXITCODE."
}
