# GOVERNANCE.md

This document explains how the Happy HTTP project is run: who makes product decisions, who maintains code, and how work is coordinated.

This project has a product owner who defines direction and priorities. Code maintenance is handled by technical contributors who choose to take responsibility for review and merging.

## Roles

### Project owner

The project owner is responsible for product management, not code review.

Responsibilities
- Define roadmap, priorities, and scope
- Approve documentation structure and contributor workflow
- Decide which features and ports matter most
- Triage issues at a product level
- Define release goals and what “supported” means publicly

Current project owner
- Tor (teenne)

### Code maintainers

Code maintainers are responsible for the technical integrity of the repository.

Responsibilities
- Review and merge pull requests
- Keep the build green
- Enforce repo layout rules (`common` vs version modules)
- Own loader/version porting quality
- Coordinate technical decisions and document them
- Prepare releases, or approve release PRs prepared by others

Current status
- No dedicated code maintainer is assigned yet

This is normal for early-stage open source. The first stable maintainers usually emerge from repeated contributions.

### Contributors

Contributors can open issues and submit pull requests.

Responsibilities
- Keep PR scope focused
- Include testing notes and evidence
- Follow the porting guide and repo layout rules
- Help with testing across loaders and Minecraft versions

## Where decisions are recorded

- Discord is for quick discussion and coordination [Join our Discord server](https://discord.gg/DVuQSV27pa)
- GitHub is the source of truth for decisions and work items

Rules
- If it affects the codebase, it must be captured in GitHub (issue, PR, or docs)
- Discord decisions should be summarised in the relevant GitHub issue or PR
- Documentation in `docs/` is treated as canonical when it describes workflows and structure

## How work is tracked

- Bugs, compatibility problems, and features are tracked as GitHub issues
- Pull requests are the only way code changes land
- Supported targets are tracked in `docs/versions.md`

Compatibility policy
- We do not open one issue per patch version unless the patch introduces a real break
- Compatibility work should be coordinated through one tracker issue per loader

## Decision-making

### Product decisions

Product decisions are made by the project owner.

Examples
- Roadmap and priority order
- Which versions to target next
- Which features to accept or defer
- Release goals

### Technical decisions

Technical decisions are made by the code maintainers for the affected area.

Examples
- How the multi-module Gradle build is implemented
- How shared logic is split between `common` and platform glue
- How to handle a breaking API change in a specific version line

If no maintainer exists for an area yet, technical decisions are made by the contributor doing the work, but must be documented clearly in the PR and reviewed by whoever merges.

## Review and merge policy

A PR can be merged when:
- It is focused and understandable
- It follows repo layout rules
- It includes testing notes
- It does not break supported targets (or clearly documents what is broke
