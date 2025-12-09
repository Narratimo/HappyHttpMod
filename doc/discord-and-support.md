# docs/discord-and-support.md

This document explains how we use Discord alongside GitHub, and how we handle support so the project does not become unmanageable.

We have a Discord server with a channel for the mod. Discord is useful for speed, but GitHub is the permanent record.
[Join our Discord server](https://discord.gg/DVuQSV27pa)

## What Discord is for

Use Discord for:
- Quick questions and unblockers
- Early feedback on ideas
- Coordination between people currently working
- Helping users who are stuck with installation or configuration

Discord is especially good for:
- “What am I doing wrong?”
- “Can someone sanity-check this approach?”
- “Is anyone already working on this?”

## What Discord is not for

Do not rely on Discord for:
- Tracking work
- Recording decisions that affect the codebase
- Reporting bugs without logs
- Compatibility claims (“works on 1.21.10”) unless it is written into `docs/versions.md`

If it affects code or support status, it must be captured in GitHub.

## Support flow (how users should be handled)

### Step 1: basic support in Discord

If the user has a simple question:
- Answer in Discord
- Point them to the relevant doc (README or wiki/docs)
- If the doc is missing, open a docs issue and link it

### Step 2: bug or crash

If it looks like a bug:
- Ask for `latest.log` and any crash report
- If they can reproduce it, ask them to open a GitHub issue using the Bug Report template
- Provide the issue link back in Discord so others can follow progress

### Step 3: compatibility claim

If a user asks “does it work on version X?”
- Point them to `docs/versions.md`
- If it is marked Untested, ask them to test and report results
- Do not open one issue per patch version unless there is a real break with logs

## How to move conclusions from Discord to GitHub

When Discord discussion leads to a decision:
- Post a short summary comment in the relevant GitHub issue or PR

Minimum summary format
- What decision was made
- Why
- What happens next
- Link to any relevant logs or screenshots

This keeps the project scalable and friendly for contributors who are not on Discord.

## Suggested Discord channel usage

- `#happy-http` (general)
  - support questions
  - release announcements
  - quick coordination

Optional (recommended as contributors grow)
- `#happy-http-dev`
  - development discussion and PR coordination

- `#happy-http-testing`
  - testing results, especially for new Minecraft versions

If you add these channels later, update this doc.

## What to pin in Discord

In the mod channel, pin:
- Link to GitHub repo
- Link to Releases
- Link to `docs/versions.md`
- Link to “How to report a bug” (Bug Report template)
- Link to the active compatibility tracker issue (for example Forge 1.21 line)

This reduces repeated questions.

## Code of conduct

If a Code of Conduct exists, link it in Discord channel topics and pins.
If it does not exist yet, add `CODE_OF_CONDUCT.md` and link it here.

## Quick links

- Repository: ht
