<!--
SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>

SPDX-License-Identifier: MIT
-->

# Todosaurus skill

This directory contains a reusable skill: [`SKILL.md`](SKILL.md), for connecting unresolved `TODO` markers with GitHub issues.

## Install in GitHub Copilot

Copy `todosaurus-skill/SKILL.md` from this repository to one of the following locations:

User-level (available to all repositories for this user, if your Copilot client supports user skills):

```text
<your-home>/.agents/skills/todosaurus-skill/SKILL.md
```

Repository-level (available only in one repository):

```text
<your-repository>/.agents/skills/todosaurus-skill/SKILL.md
```

## Install in Claude Code

Copy `todosaurus-skill/SKILL.md` from this repository to one of the following locations:

User-level (available to all repositories for this user):

```text
<your-home>/.claude/skills/todosaurus-skill/SKILL.md
```

Repository-level (available only in one repository):

```text
<your-repository>/.claude/skills/todosaurus-skill/SKILL.md
```

After installation, invoke the skill by name from your coding assistant workflow.
