# .github/pull_request_template.md

## Summary

Describe what this PR changes and why.

## Related issues

Link issues, for example: `Closes #123` or `Relates to #123`.

## What was tested

- [ ] I built the module(s) I changed
- [ ] If I changed `common/`, I ran `./gradlew buildAll`
- [ ] I launched the dev client for the affected target (when applicable)
- [ ] I ran the minimum smoke test for the affected target (when applicable)

Notes (what you tested, and how)

## Target(s) affected

Tick all that apply:

- [ ] common
- [ ] Forge
- [ ] NeoForge
- [ ] Fabric

Minecraft version(s):
- [ ] 1.20.x
- [ ] 1.21.x
- [ ] Other: ________

## Checklist

- [ ] This PR is focused (no unrelated refactors or formatting-only changes)
- [ ] I kept version modules thin and put shared logic in `common/` where possible
- [ ] Jar naming includes loader and Minecraft version (no overwrites)
- [ ] I updated docs when behaviour, structure, or support status changed
- [ ] I added logs / screenshots to the PR description if needed to explain a failure or fix

## Notes for reviewers

Anything that might help reviewers: trade-offs, follow-ups, risks, or known limitations.
