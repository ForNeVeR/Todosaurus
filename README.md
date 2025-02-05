<!--
SPDX-FileCopyrightText: 2000–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>

SPDX-License-Identifier: MIT AND Apache-2.0
-->

Todosaurus [![Status Enfer][status-enfer]][andivionian-status-classifier] [![Download][badge.plugin]][marketplace.plugin]
==========

<!-- Plugin description -->
**Todosaurus** is an IntelliJ plugin that helps you to manage the TODO comments. It allows quickly creating a new GitHub issue from a TODO comment, and update the comment with the issue number.

For example, if you have a comment `// TODO: Fix this code` in your sources, then the plugin will allow to create an issue linking the line of code including this comment, and update it to `// TODO[#123]: Fix this code`.
<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Todosaurus"</kbd> >
  <kbd>Install</kbd>

- Manually:

  Download the [latest release](https://github.com/ForNeVeR/Todosaurus/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

How to Use
----------
Currently, plugin only supports one TODO pattern, namely `TODO:` (case-insensitive) that will be converted to `TODO[#<issue-number>]` by the plugin action.

### Editor
![A screenshot of the editor window with a TODO comment in the file text. Context action list is open, and the entry "Create New Issue" is highlighted.][screenshot.editor]

When the plugin sees one of the recognized TODO patterns in the editor, it will show a gutter icon and will add a corresponding context action (triggered by `Alt+Enter` in common IntelliJ keymaps).

### TODO Tool Window
![A screenshot of the "TODO" tool window. A plugin-added item context menu item, "Create New Issue", is highlighted.][screenshot.todo]

For this feature to work, make sure you have enabled the `\btodo\b.*` pattern in the **Settings | Editor | TODO** settings page. _(It is enabled by default, so if you didn't touch these settings, then it's enabled for you.)_

Then, open the **TODO** tool window.

Open context menu for any TODO item that isn't yet linked to GitHub (i.e., an item like `TODO: something`, with no issue number).

### Configuration
To report issues, the plugin will ask you to authenticate your account (it connects to the task tracker via the API).

Currently, we automatically support GitHub accounts linked via the **Settings | Tools | Tasks | Servers** page. If no account has been added, the plugin will request you to add one.

You can save the selected steps to avoid going through them again via the checkbox **Remember my choice**. If you want to readjust the saved steps in the future, use either the corresponding link in the dialog or the **Forget** button on the plugin's settings page.

If you have some advanced usage scenarios, do not hesitate to leave your feedback at [the issue tracker][issues].

Documentation
-------------
- [Changelog][docs.changelog]
- [Contributor Guide][docs.contributing]
- [Maintainer Guide][docs.maintainer-guide]
- [Code of Conduct (adapted from the Contributor Covenant)][docs.code-of-conduct]

License
-------
The project is distributed under the terms of [the MIT license][docs.license]
(unless a particular file states otherwise).

The license indication in the project's sources is compliant with the [REUSE specification v3.3][reuse.spec].

### Additional Copyright Holders
In addition to the people listed in the version control history, Todosaurus reuses portions of code from [IntelliJ IDEA Community Edition][intellij-community] and [IntelliJ Platform Plugin Template][intellij-platform-plugin-template], copyrighted by JetBrains s.r.o. and contributors.

Major sections copied from the external projects are marked with corresponding references in the file headers.

[andivionian-status-classifier]: https://andivionian.fornever.me/v1/#status-enfer-
[badge.plugin]: https://img.shields.io/jetbrains/plugin/v/23838.svg
[docs.changelog]: CHANGELOG.md
[docs.code-of-conduct]: CODE_OF_CONDUCT.md
[docs.contributing]: CONTRIBUTING.md
[docs.license]: LICENSE.md
[docs.maintainer-guide]: MAINTAINERSHIP.md
[intellij-community]: https://github.com/JetBrains/intellij-community
[intellij-platform-plugin-template]: https://github.com/JetBrains/intellij-platform-plugin-template
[issues]: https://github.com/ForNeVeR/Todosaurus/issues
[marketplace.plugin]: https://plugins.jetbrains.com/plugin/23838
[reuse.spec]: https://reuse.software/spec-3.3/
[screenshot.editor]: docs/screenshot.editor.png
[screenshot.todo]: docs/screenshot.todo.png
[status-enfer]: https://img.shields.io/badge/status-enfer-orange.svg
