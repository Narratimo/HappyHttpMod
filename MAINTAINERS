# MAINTAINERS.md

This document defines who owns product direction and who maintains the codebase, so work does not bottleneck and contributors know where decisions happen.

## Roles

### Project owner

The project owner is responsible for product direction and coordination, not code maintenance.

Responsibilities
- Define roadmap, priorities, and scope
- Maintain and approve documentation and contributor workflow
- Triage issues at a product level (what matters, what is next)
- Coordinate contributors and agree on milestones
- Decide what “supported” means for the public roadmap

Project owner
- Tor (teenne)

### Code maintainers

Code maintainers are responsible for technical review and merging changes.

Responsibilities
- Review and merge pull requests
- Keep the build green
- Own module boundaries and architecture enforcement
- Ensure compatibility ports follow the porting guide
- Prepare and cut releases (or delegate release work)
- Reject or request changes to PRs that break supported targets

Current status
- No dedicated code maintainer is assigned yet

If you want to become a maintainer, see “Becoming a code maintainer” below.

## Ownership areas (code)

When code maintainers exist, ownership should be assigned across areas:

- Build and repository structure (Gradle, CI, release wiring)
- Common core (`common/`)
- Forge targets (`forge/`)
- NeoForge targets (`neoforge/`)
- Fabric targets (`fabric/`)

Each area should list one primary owner and optional secondary owners.

## Review and merge policy (until maintainers are assigned)

Until a code maintainer is assigned:
- PRs may be accepted only for documentation and non-code changes, unless explicitly coordinated
- For code contributions, we will prefer small PRs that are easy to verify with local builds and logs
- Contributors should include clear testing notes and evidence (logs, screenshots, reproducible steps)

If a contributor wants to lead technical maintenance, they can do so by taking ownership of an area.

## Becoming a code maintainer

To become a code maintainer:
- Contribute consistently (especially ports and fixes)
- Demonstrate that you can keep builds working
- Be responsive in PR reviews and issue discussions
- Propose ownership in a GitHub issue (describe which area you will own)

Maintainers are added through a PR updating this file.

## Where decisions are recorded

- Discord is for quick coordination and discussion
- GitHub is the source of truth for work items and decisions that affect the codebase

Product direction decisions are approved by the project owner.
Technical implementation decisions are owned by code maintainers.
