# GOVERNANCE.md

This document explains how the Happy HTTP project is maintained and how decisions are made.

It exists to keep collaboration smooth for a small core team and future contributors.

## Project roles

### Maintainers

Maintainers are responsible for:
- Reviewing and merging pull requests
- Keeping the build green
- Cutting releases
- Updating documentation when workflows change
- Making final calls when there is disagreement

Maintainers may also assign “ownership” of modules (by loader/version) to reduce review bottlenecks.

### Contributors

Contributors can:
- Open issues
- Submit pull requests
- Participate in discussions
- Help test builds across supported targets

Contributors do not need permission to propose changes. Maintainers decide what gets merged.

## Where decisions are recorded

- Discord is for quick coordination and discussion
- GitHub is the source of truth for decisions and work items

Rules
- If it affects the codebase, it must be captured in GitHub as an issue, PR, or doc change
- Architecture and workflow decisions must be written into:
  - the relevant GitHub issue or PR description, or
  - a document in the `docs/` folder

## How work is tracked

- Bugs, compatibility problems, and feature requests are tracked as GitHub issues
- PRs are the only way changes enter the codebase
- Release notes are tracked via GitHub Releases (and optionally a `CHANGELOG.md`)

## Decision-making

Default rule
- We aim for consensus between active maintainers

When consensus is not possible
- The maintainer responsible for the affected area makes the final call

Areas can include
- build and repository structure
- a specific loader
- a specific Minecraft version line

## Scope control

We keep the project maintainable by being strict about scope.

We prioritise:
- work that improves multi-version and multi-loader maintainability
- work that reduces support burden and configuration mistakes
- reliability and safety improvements

We de-prioritise:
- large features without a clear use case and test plan
- changes that add complexity to every target without strong value
- “one PR does everything” changes that are hard to review

## Contribution acceptance policy

We accept contributions when they:
- align with the project direction
- follow repository structure rules (`common` vs version modules)
- keep PR scope focused
- include basic testing notes
- do not break existing supported targets

We may ask contributors to revise PRs to match these rules.

## Version support policy

- The supported matrix is tracked in `docs/versions.md`
- We do not treat every patch version as a separate target unless it introduces a real break
- Compatibility work should be coordinated through tracker issues rather than dozens of parallel issues

## Releases

- Releases are produced per target jar (loader + Minecraft version)
- Releases must clearly state what they support
- A release should not claim support that is not reflected in `docs/versions.md`

## Community expectations

- Be respectful and constructive
- Assume good intent
- Keep discussions grounded in concrete information (logs, steps, evidence)
- Help newcomers get unblocked by pointing to the correct docs and issues

## Changes to governance

This document can be updated via pull request.
If you want to propose a governance change, open an issue first.
