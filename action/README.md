<!--
SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>

SPDX-License-Identifier: MIT
-->

Todosaurus GitHub Action
========================

Usage
-----
```yaml
jobs:
  main:
    - name: Check TODOs
      uses: ForNeVeR/Todosaurus/action@v1
      with:
        config: todosaurus.toml # optional
        strict: 'true' # optional
        github-token: ${{ github.token }} # optional, recommended
```
