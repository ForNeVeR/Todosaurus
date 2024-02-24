Maintainer Guide
================

Rotate the Publishing Key
-------------------------
To rotate the key used for publication to the Marketplace, follow these steps:
1. Go to [the Tokens page][marketplace.tokens].
2. Create a new token named `github.todosaurus` (replacing the previous one if required).
3. Go to [the Action Secrets page][github.secrets].
4. Update the `PUBLISH_TOKEN` secret with the new token value.

[marketplace.tokens]: https://plugins.jetbrains.com/author/me/tokens
[github.secrets]: https://github.com/ForNeVeR/Todosaurus/settings/secrets/actions
