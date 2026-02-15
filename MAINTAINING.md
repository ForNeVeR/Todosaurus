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
6. Update the version in the `intellij/gradle.properties` file.
7. Update the `<Version>` in the `cli/Directory.Build.props` file.
8. Merge the aforementioned changes via a pull request.
9. Check if the NuGet key is still valid (see the **Rotate NuGet Publishing Key** section if it isn't).
10. Push a tag named `v<VERSION>` to GitHub.

The new release will be published automatically.

Rotate the JetBrains Marketplace Publishing Key
-----------------------------------------------
To rotate the key used for publication to the Marketplace, follow these steps:
1. Go to [the Tokens page][marketplace.tokens].
2. Create a new token named `github.todosaurus` (replacing the previous one if required).
3. Go to [the Action Secrets page][github.secrets].
4. Update the `PUBLISH_TOKEN` secret with the new token value.

Rotate NuGet Publishing Key
---------------------------
CI relies on a NuGet API key being added to the secrets. From time to time, this key requires maintenance: it will become obsolete and will have to be updated.

To update the key:

1. Sign in onto nuget.org.
2. Go to the [API keys][nuget.api-keys] section.
3. Update the existing or create a new key named `github.todosaurus` with a permission to **Push only new package versions** and only allowed to publish the package **FVNever.Todosaurus.Cli**.

   (If this is the first publication of a new package, upload a temporary short-living key with permission to add new packages, and rotate it afterward.)
4. Paste the generated key to the `NUGET_TOKEN` variable on the [action secrets][github.secrets] section of GitHub settings.

[github.secrets]: https://github.com/ForNeVeR/Todosaurus/settings/secrets/actions
[marketplace.tokens]: https://plugins.jetbrains.com/author/me/tokens
[nuget.api-keys]: https://www.nuget.org/account/apikeys
[semver]: https://semver.org/spec/v2.0.0.html
