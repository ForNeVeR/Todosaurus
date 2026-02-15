# SPDX-FileCopyrightText: 2024-2026 Friedrich von Never <friedrich@fornever.me>
#
# SPDX-License-Identifier: MIT

param(
    [string] $RefName,
    [string] $RepositoryRoot = "$PSScriptRoot/.."
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

function Get-VersionFromDirectoryBuildProps {
    $propsFilePath = "$RepositoryRoot/cli/Directory.Build.props"
    [xml] $props = Get-Content $propsFilePath
    $version = $null
    foreach ($group in $props.Project.PropertyGroup) {
        if ($group.Label -eq 'Packaging') {
            $version = $group.Version
            break
        }
    }
    $version
}

function Get-VersionFromGradleProperties {
    $propertiesFile = "$RepositoryRoot/intellij/gradle.properties"
    $content = Get-Content -Raw $propertiesFile
    $version = $null
    if ($content -match 'pluginVersion\s*=\s*(.*?)\n') {
        $version = $Matches[1]
    }
    $version
}

Write-Host "Determining version from ref `"$RefName`"…"
if ($RefName -match '^refs/tags/v') {
    $version = $RefName -replace '^refs/tags/v', ''
    Write-Host "Pushed ref is a version tag, version: $version"
} else {
    $dotNetVersion = Get-VersionFromDirectoryBuildProps
    $jvmVersion = Get-VersionFromGradleProperties

    if ($null -eq $dotNetVersion) {
        throw 'Cannot read version from .NET metadata.'
    }
    if ($null -eq $jvmVersion) {
        throw 'Cannot read version from Gradle metadata.'
    }

    if ($dotNetVersion -ne $jvmVersion) {
        throw "Versions read from .NET and Gradle are not equal: $dotNetVersion != $jvmVersion."
    }

    $version = $dotNetVersion
    Write-Host "Pushed ref is a not version tag, got version from the metadata: $version"
}

Write-Output $version
