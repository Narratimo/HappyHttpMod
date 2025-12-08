# docs/issues-and-labels.md

This document defines how we use GitHub issues and labels for Happy HTTP.

It is written to fit the current repository setup: issue templates already exist, and labels already exist for loaders, client/server, and many specific Minecraft versions (including patch versions).

## Goals

- Keep the issue list actionable and low-noise
- Make compatibility work trackable without one issue per patch version
- Make it obvious what is being worked on and what is supported
- Ensure GitHub (not Discord) is the permanent record for decisions and progress

## Where to put things

- Discord: quick questions, coordination, informal discussion
- GitHub issues: work items, decisions that affect the codebase, compatibility failures, feature requests
- `docs/versions.md`: public status matrix for what is supported

If something affects a support claim, it must be reflected in `docs/versions.md` and backed by an issue or PR.

## Issue types (templates)

Use the existing templates:

- Bug Report
- Compatibility Issue / Compatibility report
- Documentation Request
- Feature Request
- Performance Issue
- Support Request

Guideline
- Support Request is for help using the mod. If it reveals missing documentation, convert it into a Documentation Request and link it.

## Triage rules for compatibility issues

We do not want 10+ parallel issues that just say “Compatibility with Minecraft 1.21.x” with no failure details.

### When a compatibility issue is valid

Open a compatibility issue only if you have at least one of these:
- Build failure (compile or Gradle)
- Crash on launch or world load
- Functional break (Receiver, Sender, config, networking) with steps and logs

### When a compatibility issue should not be opened

Do not open an issue if it is only:
- “Does it work on version X?”

Instead:
- Mark the target as **Untested** in `docs/versions.md`
- Comment in the compatibility tracker issue for that loader and minor line

### Patch version policy

You currently have labels for patch versions, for example:
- `version-1.21.1`, `version-1.21.2`, … `version-1.21.10`

Policy
- Treat patch versions as separate issues only when the patch introduces a unique break.
- Otherwise, track compatibility at the minor line level (1.21.x) through a tracker issue and the support matrix.

## Labels (based on what exists today)

Your current label set already includes:

Type and workflow
- `bug`
- `documentation`
- `enhancement`
- `duplicate`
- `invalid`
- `question`
- `help wanted`
- `good first issue`
- `wontfix`
- `codex` (internal/automation-related)

Loader and platform
- `loader-forge`
- `loader-fabric`
- `loader-neoforge`
- `mc-java`
- `mc-bedrock` (only use if it becomes relevant)
- `server-spigot-plugin` (only use if you actually target Spigot as a plugin deliverable)

Environment
- `Client`
- `Server`

Minecraft versions
- Many specific labels: `version-1.17` through `version-1.21.10` (and more)

### How we will use labels

#### Required labels for compatibility issues

Every compatibility issue must have:

1) Loader
- `loader-forge` or `loader-neoforge` or `loader-fabric`

2) Platform
- `mc-java`

3) Environment
- `Client` and/or `Server`

4) Version label
- Prefer the *minor line* label where possible:
  - Use `version-1.21` for anything in the 1.21 line unless the patch is uniquely broken
  - Use `version-1.20.6` only if the problem is unique to that patch

Note
- You currently do not have explicit “1.21.x” labels, you have `version-1.21` plus patch labels.
- We will use `version-1.21` as the default for the entire 1.21 line.

#### Required labels for bug reports

Bug issues should have:

- One loader label (if loader-specific)
- `mc-java`
- `Client` and/or `Server`
- Version label if the bug is version-specific, otherwise omit version labels

#### Feature requests

Feature issues should have:
- `enhancement`
- Optional: loader labels if the feature is loader-specific
- Do not add a version label unless it is tied to a specific version line

#### Documentation requests

Docs issues should have:
- `documentation`

Optional
- `help wanted` if you want community contributions

#### Support requests

Support issues should have:
- `question`

Closing policy
- Close once answered and link the relevant documentation
- If the question reveals missing docs, open or convert to a Documentation Request

## Tracker issues (how we manage many related items)

We will use tracker issues to coordinate large efforts and reduce duplicates.

Examples
- Forge compatibility tracker (1.21 line)
- Multi-module Gradle refactor tracker
- Multi-loader architecture tracker

Tracker label guidance
- Use `documentation` if it is mostly planning and coordination
- Use `enhancement` if it unlocks capability
- Always include the loader label if it is loader-specific

Pin active trackers.

## How we will clean up the current issue set

Based on the current list, there are many Forge compatibility issues for:
- 1.20.5, 1.20.6
- 1.21, 1.21.1 … 1.21.10
…and older sets for 1.18.x and 1.19.x.

Cleanup policy

1) Create one pinned tracker issue for Forge 1.21 line
- Label it: `loader-forge`, `mc-java`, `Client`, `Server`, `version-1.21`
- Link to `docs/versions.md`
- Link to `docs/porting-guide.md`

2) For patch issues that contain no logs and no specific failure
- Close as duplicate
- Comment: “Tracked in Forge 1.21 compatibility tracker” and link it

3) Keep patch issues open only when they describe a specific break
- Ensure they have logs and steps
- Link them from the tracker issue

4) Update the versions matrix
- Mark Forge 1.21 line as Untested or Broken based on current reality
- Mark specific patches only when you have evidence that they differ

This leaves GitHub readable for contributors and watchers.

## Label updates recommended (optional)

You can keep the current labels and still follow the policy above.

If you want to improve label ergonomics later, add these:

- `version-1.20.x`
- `version-1.21.x`

Then:
- Use the `.x` labels for most compatibility tracking
- Use patch labels only for unique breaks

This is optional, not required.

## Quick examples

Good compatibility issue
- Labels: `loader-forge`, `mc-java`, `Client`, `version-1.21`
- Title: `[Compatibility] Forge 1.21: crash on launch during mod init`
- Includes: crash report + latest.log + steps

Bad compatibility issue
- Labels: `loader-forge`, `mc-java`, `version-1.21.9`
- Title: `[Forge] Compatibility with Minecraft 1.21.9`
- No logs, no failure described

Good feature issue
- Labels: `enhancement`
- Title: `[Feature] Add per-block cooldown to prevent accidental spam`
