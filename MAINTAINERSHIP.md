Maintainer Guide
================

Publish a Release
-----------------
To publish a new release, follow these steps:
1. Update the `LICENSE.md` file, if required.
2. Prepare an `[Unreleased]` section in the `CHANGELOG.md` file.
3. Update the version in the `gradle.properties` file.
4. Merge the changes via a GitHub PR.
5. Go to the Releases **section** on GitHub, press **Publish a Release** for the latest release draft.

   The GitHub actions will manage the rest and upload the plugin to the Marketplace.

Rotate the Publishing Key
-------------------------
To rotate the key used for publication to the Marketplace, follow these steps:
1. Go to [the Tokens page][marketplace.tokens].
2. Create a new token named `github.todosaurus` (replacing the previous one if required).
3. Go to [the Action Secrets page][github.secrets].
4. Update the `PUBLISH_TOKEN` secret with the new token value.

Rotate the PR Token
-------------------
To make PRs in the `.github/workflows/dependencies.yml`, the CI uses a special token.

To refresh it, follow the steps:
1. Go to the [Fine-grained tokens][github.tokens] settings page on GitHub.
2. Generate a new token named **github.todosaurus**, scoped to the **Todosaurus** repository, with the following **Repository permissions**:
    - **Contents**: **Read and write**,
    - **Pull requests**: **Read and write**.
3. Paste the token into [the Action Secrets page][github.secrets] as `PR_TOKEN`.

[github.secrets]: https://github.com/ForNeVeR/Todosaurus/settings/secrets/actions
[github.tokens]: https://github.com/settings/tokens?type=beta
[marketplace.tokens]: https://plugins.jetbrains.com/author/me/tokens
