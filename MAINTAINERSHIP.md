<!--
SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>

SPDX-License-Identifier: MIT
-->

Maintainer Guide
================

Publish a Release
-----------------
To publish a new release, follow these steps:

1. Update the copyright year in the `LICENSE.md`, if required.
2. Make sure there's a properly formed version entry in the `CHANGELOG.md`.
3. Choose the new version according to [Semantic Versioning][semver]. It should consist of three numbers (i.e. `1.0.0`).
4. Update the version in the `gradle.properties` file.
5. Merge these changes via a PR.
6. Push a tag named `v<VERSION>` to GitHub.

The new release will be published automatically.

Rotate the Publishing Key
-------------------------
To rotate the key used for publication to the Marketplace, follow these steps:
1. Go to [the Tokens page][marketplace.tokens].
2. Create a new token named `github.todosaurus` (replacing the previous one if required).
3. Go to [the Action Secrets page][github.secrets].
4. Update the `PUBLISH_TOKEN` secret with the new token value.

[github.secrets]: https://github.com/ForNeVeR/Todosaurus/settings/secrets/actions
[marketplace.tokens]: https://plugins.jetbrains.com/author/me/tokens
[marketplace]: https://plugins.jetbrains.com/plugin/23838
[semver]: https://semver.org/spec/v2.0.0.html
