# docs/contributor-workflow.md

This document explains how we communicate and coordinate work across GitHub and Discord, especially with a small team and incoming contributors.

It complements `CONTRIBUTING.md` by focusing on workflow and project coordination rather than git commands.

## Tools and what they are for

### GitHub Issues

Use for
- Bugs, compatibility failures, and feature requests
- Tracking work that should result in code changes
- Recording decisions that affect the repository
- Assigning ownership and documenting progress

Rules
- If it affects the codebase, it must exist as a GitHub issue or PR
- Link related issues instead of duplicating them
- Prefer one tracking issue per large effort (for example a compatibility port)

### GitHub Pull requests

Use for
- All code and documentation changes
- Review discussion
- Final decision record for implementation details

Rules
- Keep PRs small and focused
- Include testing notes
- Link the issue being addressed

### Discord

Use for
- Quick questions and unblockers
- Informal discussion
- Coordinating who is working on what right now

Rules
- Do not rely on Discord as the only place decisions are recorded
- If Discord discussion changes direction, summarise it in the relevant GitHub issue
- Use Discord to guide people to the right GitHub issue or doc

## How a piece of work should flow

1) Start with an issue  
- Bug, feature request, or compatibility failure

2) Agree on scope  
- Clarify what “done” means
- Identify which module(s) are affected

3) Implement in a PR  
- Keep the PR limited to one change area
- Include test results

4) Merge and update docs  
- Update `docs/versions.md` if support status changed
- Update any workflow docs if structure changed

## How we coordinate compatibility work

Compatibility work can explode into many duplicate issues.

Policy
- Do not open one issue per patch version unless you can show a real break
- Use `docs/versions.md` as the support matrix
- Use one compatibility tracker issue per loader to coordinate ports

When a new version fails
- Open a compatibility issue only if you have logs and a concrete failure
- Link it from the tracker issue
- Update the matrix status to Broken with a short note and link

## Ownership and assignment

- The project owner sets priorities and roadmap
- Code maintainers (when present) review and merge code

If you want to take responsibility for a loader or version line, propose it in an issue and add yourself as an owner in `MAINTAINERS.md`.

## Triage rules (keep issues manageable)

When creating issues, aim for:
- one issue per distinct problem
- clear reproduction steps
- logs attached
- correct labels (loader + Minecraft version)

When reviewing issues, aim for:
- closing duplicates quickly
- converting vague issues into concrete tasks or questions
- linking to docs instead of re-explaining basics

## What to do if you are new

Start here
- Read `README.md`
- Read `CONTRIBUTING.md`
- Read `docs/repo-layout.md`
- Check `docs/versions.md`

Good first contributions
- Improve docs clarity
- Validate one target in the versions matrix
- Fix a small bug in one module
- Help triage and reduce duplicate issues

## Communication style

- Prefer short, factual updates
- Use GitHub for persistent information
- Use Discord for speed, then write the conclusion into GitHub

This workflow is designed to keep the project scalable as contributors join.
