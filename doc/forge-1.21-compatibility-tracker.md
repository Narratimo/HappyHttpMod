# docs/forge-1.21-compatibility-tracker.md

This is the canonical tracker for Forge compatibility work on the Minecraft 1.21 line.

It exists to prevent one issue per patch version with no diagnostic content. Patch-specific issues are only kept when they contain evidence of a unique break.

Status is reflected in `docs/versions.md`. This tracker is where we coordinate the work.

## Scope

Loader
- Forge

Minecraft line
- 1.21 (includes 1.21.0 through 1.21.x patches)

In scope
- Build, launch, and functional compatibility for the Happy HTTP blocks and config
- Module structure and Gradle wiring needed to support the 1.21 line cleanly

Out of scope (for this tracker)
- NeoForge and Fabric ports
- New features that are not required for compatibility

## Definition of done

A Forge 1.21 target is considered working when:

- Target module builds successfully
- Dev client launches
- World loads
- HTTP Receiver block: can be placed, configured, and triggers redstone on request
- HTTP Sender block: can be placed, configured, and sends request on redstone input
- Config file generates and settings apply correctly
- `docs/versions.md` updated (Beta or Stable)

## Current baseline

- Known working baseline: Forge 1.20.2
- Target: Forge 1.21 line

## How we will track patch versions

Default policy
- Track at minor line level: `version-1.21`

Patch issues
- Keep patch issues open only if they contain logs and a unique break
- Close patch issues that only state “compatibility with X” with no failure details, and link back here

## Work plan

### Step 1: confirm target strategy

- [ ] Decide the first pinned target patch (example: 1.21.1 or 1.21.10)
- [ ] Update `docs/versions.md` to reflect the chosen initial target

### Step 2: build structure for Forge 1.21

- [ ] Create / update the Forge 1.21 version module (one module, one jar)
- [ ] Ensure jar name includes `forge` and `mc1.21.x`
- [ ] Ensure the module depends on `common` correctly
- [ ] Ensure baseline Forge 1.20.2 still builds

### Step 3: fix compile and mapping breaks

- [ ] Resolve compilation errors for the Forge 1.21 module only
- [ ] Move truly shared logic into `common` (no Minecraft API imports)

### Step 4: run smoke test

- [ ] Launch dev client
- [ ] Load world
- [ ] Test Receiver end-to-end
- [ ] Test Sender end-to-end
- [ ] Verify config generation and binding behaviour

### Step 5: stabilise and mark status

- [ ] Update `docs/versions.md` (Beta or Stable)
- [ ] Prepare release notes and jar(s) if releasing

## Links to existing issues

These issues currently exist and should be handled under this tracker.

Recent Forge 1.21 patch issues (opened recently)
- #40 Forge 1.21
- #41 Forge 1.21.1
- #42 Forge 1.21.2
- #43 Forge 1.21.3
- #44 Forge 1.21.4
- #45 Forge 1.21.5
- #46 Forge 1.21.6
- #47 Forge 1.21.7
- #48 Forge 1.21.8
- #49 Forge 1.21.9
- #50 Forge 1.21.10

Policy for the above
- If the issue has logs and a specific failure, keep it and link it under “Active breakpoints”
- If it has no diagnostics, close as duplicate and link back to this tracker

Forge 1.20.5 / 1.20.6 issues (also opened recently)
- #38 Forge 1.20.5
- #39 Forge 1.20.6

Older compatibility issues (opened earlier)
- #5 Forge 1.20.0
- #6 Forge 1.20.1
- #7 Forge 1.20.1
- #8 Forge 1.20.3
- #9 Forge 1.20.4
- #10 Forge 1.20.5
- #13 Forge 1.20.6
- #14 Forge 1.19.4
- #15 Forge 1.19.3
- #16 Forge 1.19.2
- #17 Forge 1.19.1
- #18 Forge 1.19.0
- #19 Forge 1.18.1
- #20 Forge 1.18.0
- #21 Global parameters (server, mc-java, version-1.20.2) or similar cross-cutting item

Note
- Many of the older issues appear to be “all versions compatible with Forge” style requests. We should consolidate those into the support matrix and the relevant tracker(s), and keep only issues that contain a real break and logs.

## Active breakpoints (fill this in as you learn)

Use this section to list concrete breaks that block the port.

Example format
- Minecraft 1.21.x: build fails due to <error> (link issue)
- Minecraft 1.21.x: crash on launch at <class> (link issue)
- Minecraft 1.21.x: Receiver does not trigger redstone (link issue)

## Who is doing what

Use this section to avoid duplicate effort.

- Port lead: unassigned
- Build wiring: unassigned
- Receiver validation: unassigned
- Sender validation: unassigned
- Config binding behaviour: unassigned

Contributors should comment in this tracker with:
- what they will take
- which module/version they will work on
- expected PR scope

## How to contribute to this tracker

- Read `docs/porting-guide.md`
- Keep PRs to one target module at a time
- Include logs and test notes in PR descriptions
- Update `docs/versions.md` when status changes

Discord is welcome for coordination, but conclusions must be written back to GitHub.
