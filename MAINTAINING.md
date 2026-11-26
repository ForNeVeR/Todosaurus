<!--
SPDX-FileCopyrightText: 2024-2026 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>

SPDX-License-Identifier: MIT
-->

Maintainer Guide
================

Publish a New Version
---------------------
1. Update the copyright statement in the `LICENSE.txt`, if required.
2. Update the `<Copyright>` statement and `<PackageLicenseExpression>` field in the `cli/Directory.Build.props`, if required.
3. Update the project's status in the `README.md` file, if required.
4. Make sure there's a properly formed version entry in the `CHANGELOG.md`.
5. Choose the new version according to [Semantic Versioning][semver]. It should consist of three numbers (i.e. `1.0.0`).
6. Update the version of all the packages via `scripts/Update-Version.ps1 -NewVersion <the new version>`.
7. Merge the aforementioned changes via a pull request.
8. Push a tag named `v<VERSION>` to GitHub.

The new release will be published automatically.

Rotate the JetBrains Marketplace Publishing Key
-----------------------------------------------
To rotate the key used for publication to the Marketplace, follow these steps:
1. Go to [the Tokens page][marketplace.tokens].
2. Create a new token named `github.todosaurus` (replacing the previous one if required).
3. Go to [the Action Secrets page][github.secrets].
4. Update the `PUBLISH_TOKEN` secret with the new token value.

NuGet Publishing Policy
-----------------------
This repository relies on [NuGet Trusted Publishing][docs.nuget-trusted-publishing] policy. In case you need to create it again, follow these steps:

1. Sign in to nuget.org.
2. Go to the [Trusted Publishing][nuget.trusted-publishing] section.
3. Create a new policy:
   - **Policy Name:** `github.todosaurus`.
   - **Repository Owner:** `ForNeVeR`.
   - **Repository**: `Todosaurus`.
   - **Workflow File**: `release.yml`.
   - **Environment:** _leave empty_.
   - **Scope:** **push only new packages versions**.
   - **Glob Patterns and Packages**: `FVNever.Todosaurus.Cli`.
4. Put your nuget.org username into the `NUGET_USER` secret on the [action secrets][github.secrets] section of GitHub settings.

When introducing a new package, you can adjust the policy temporarily to allow it to **push new packages and package versions**.

Rotate the Dependency Update Token
----------------------------------
This project uses a special GitHub application to manage the IntelliJ-based dependencies, as documented in [intellij-updater][].

To update the token:
1. Go to the [application settings][github.apps.intellij-updater].
2. Generate a new private key.
3. Copy the **App ID** to the `IJ_UPDATER_APP_ID` variable in [GitHub Actions secrets][github.secrets].
4. Copy the new private key to the `IJ_UPDATER_PRIVATE_KEY` variable in [GitHub Actions secrets][github.secrets].

[docs.nuget-trusted-publishing]: https://learn.microsoft.com/en-us/nuget/nuget-org/trusted-publishing
[github.apps.intellij-updater]: https://github.com/settings/apps/intellij-updater
[github.secrets]: https://github.com/ForNeVeR/Todosaurus/settings/secrets/actions
[intellij-updater]: https://github.com/ForNeVeR/intellij-updater
[marketplace.tokens]: https://plugins.jetbrains.com/author/me/tokens
[nuget.trusted-publishing]: https://www.nuget.org/account/trustedpublishing
[semver]: https://semver.org/spec/v2.0.0.html
