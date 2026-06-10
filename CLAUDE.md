# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository state

This repository currently contains **distributed binaries and documentation only — no source code is checked in.**

- `Secret_Hunter.exe` — compiled Windows GUI binary (PE32+ x86-64, ~12 MB).
- `Secret-Hunter-Linux` — a 2-byte placeholder/stub, not a real Linux build.
- `README.md` — project description, features, usage.

There is no build system, test suite, linter, or package manifest. Do not invent build/lint/test commands — none exist yet. If asked to "run tests" or "build," first confirm with the user whether they intend to add source code, since the application source is not present in this repo.

## What the application is

**Secret_Hunter** (a.k.a. SainiON Hacks JS Recon & Secret Scanner) is a GUI reconnaissance tool for bug bounty hunters and pentesters. Its pipeline, per the README, is:

1. Pull historical JavaScript files for a target domain from the **Wayback Machine** (CDX API).
2. Filter/dedupe URLs using **URO**.
3. Scan the JS for hardcoded secrets using **900+ regex-based patterns**.
4. Reduce false positives with **Shannon entropy** scoring.
5. Extract and auto-resolve hidden **endpoints**.
6. Export findings as an **HTML report**.

Tech stack indicated by the README badges: **Python 3.x** with a **Tkinter** GUI.

## Secret detection rule format

Secret patterns are defined in `custom_secrets.json` (not yet present in this repo) as an array of objects:

```json
[
  {
    "name": "Google API Key",
    "pattern": "AIza[0-9A-Za-z\\-_]{35}",
    "severity": "medium"
  }
]
```

When adding or editing detection rules, preserve this `name` / `pattern` / `severity` shape. `pattern` is a regex; the engine combines regex matching with entropy scoring to suppress false positives.

## Scope and intent

This is an **authorized-testing / educational** security tool (see the README disclaimer). Work on it accordingly: improving recon and secret-detection capabilities for legitimate bug bounty and pentest use is in scope. Do not add features whose primary purpose is unauthorized access, evasion, or mass/abusive targeting.

## Project links

- YouTube: https://www.youtube.com/@SainiONHacks
- Upstream repo: https://github.com/SainiONHacks/Secret_Hunter
