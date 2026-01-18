# Project Analysis: Eira Relay

> **Rebranding:** The mod is being renamed from "Happy HTTP" / "HttpAutomator" to "Eira Relay" (www.eira.no)

## Executive Summary

**Eira Relay** (formerly Happy HTTP Mod) is a Minecraft mod designed to bridge the in-game world with external systems using HTTP webhooks. The mod is maintained by Eira, a non-commercial organization focused on teaching kids and teenagers to code.

**Primary Goal:** Enable adventure map creators to build interactive experiences that mix physical/digital world events with Minecraft, allowing bidirectional triggering between the two.

## Current Implementation Status

### Implemented Features

| Feature | Status | Location |
|---------|--------|----------|
| HTTP Receiver Block | Complete | `common/.../block/HttpReceiverBlock.java` |
| HTTP Server | Complete | `common/.../http/HttpServerImpl.java` |
| Configuration GUI | Complete | `common/.../client/gui/HttpReceiverSettingsScreen.java` |
| Custom Endpoint URLs | Complete | Block entity stores URL config |
| Port Configuration | Complete | `happyhttp-common.toml` (default: 8080) |
| Forge Support | Complete | `forge/` module |

### Gap Analysis: Main Branch vs Dev Branch

| Feature | Main Branch | Dev Branch |
|---------|-------------|------------|
| HTTP Receiver Block | Implemented | Enhanced |
| HTTP Sender Block | Missing | **IMPLEMENTED** |
| Parameter Matching | Missing | **IMPLEMENTED** |
| Power Modes (Toggle/Timer) | Missing | **IMPLEMENTED** (EnumPoweredType, EnumTimerUnit) |
| Global Variables | Missing | **IMPLEMENTED** (GlobalParam, config) |
| HTTP Client | Missing | **IMPLEMENTED** (HttpClientImpl) |
| JSON Utilities | Missing | **IMPLEMENTED** (JsonUtils) |
| Base Block Screen | Missing | **IMPLEMENTED** (BaseBlockScreen) |
| NeoForge Support | Skeleton | Skeleton |
| Fabric Support | Incomplete | Incomplete |

### Branches Status

| Branch | Status | Key Content |
|--------|--------|-------------|
| `main` | Stable but incomplete | HTTP Receiver only |
| `dev` | Feature-complete but not merged | HTTP Sender, global params, enhancements |
| `feenixnet` | Documentation updates | Working version docs, compatibility |
| `origin/codex/*` | Bug fixes | Startup error fixes |

**Key Finding:** The HTTP Sender Block is fully implemented in the `dev` branch but has NOT been merged to `main`. The `dev` branch also contains global parameters, HTTP client, and many other features.

## Platform Support Status

| Platform | Status | Details |
|----------|--------|---------|
| Forge 1.20.2 | Functional | Full implementation, working HTTP server |
| NeoForge | Skeleton | Constructor only, no event handlers or networking |
| Fabric | Incomplete | Basic structure, missing core functionality |

## Documentation Quality

The project has comprehensive documentation:
- Feature specifications with prioritization tiers
- Security review with actionable recommendations
- Use case documentation
- Configuration reference
- Porting guide for new versions

**Documentation Gaps:**
- No API documentation for HTTP endpoints
- Missing developer onboarding guide
- No automated testing documentation

## Risk Areas

1. **Single Platform Dependency:** Only Forge is fully functional
2. **Missing Core Feature:** HTTP Sender is documented but not implemented
3. **Security Concerns:** Default LAN binding, no TLS, no authentication
4. **Memory Leak:** HTTP handlers not cleaned up on block removal
5. **Version Lock:** Tied to Minecraft 1.20.2, no multi-version support yet

## Recommendations

### Immediate Actions
1. **Merge `dev` branch to `main`** - Contains HTTP Sender and many features
2. Complete NeoForge implementation
3. Add handler cleanup on block removal
4. Fix NeoForge mixin config package name

### Short-term Improvements
1. Add parameter validation to HTTP Receiver
2. Implement power modes (Toggle/Timer)
3. Add default localhost binding for security
4. Add HTTP method enforcement

### Long-term Goals
1. Multi-version support (1.21.x)
2. Fabric feature parity
3. Authentication helpers
4. TLS support option
