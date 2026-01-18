# Technical Debt Analysis: Eira Relay

## Overview

This document identifies technical debt in the codebase. Most critical items have been resolved.

---

## Critical Debt (All Resolved)

### 1. ~~Unmerged `dev` Branch~~ ✅ RESOLVED

**Status:** Resolved via PR #1 (merge/dev-to-main)

The dev branch has been merged. HTTP Sender was ported separately to NeoForge 1.21.1.

---

### 2. ~~Version Incompatibility~~ ✅ RESOLVED

**Status:** Resolved via PR #5 (feature/neoforge-http-sender)

HTTP Sender has been ported from common/ (MC 1.20.2) to neoforge/ (MC 1.21.1).
The common/forge modules remain disabled for now as they target MC 1.20.2.

---

### 3. ~~HTTP Sender Missing on NeoForge 1.21.1~~ ✅ RESOLVED

**Status:** Resolved via PR #5 (feature/neoforge-http-sender)

Ported HTTP Sender to NeoForge 1.21.1:
- ✅ HttpSenderBlock.java
- ✅ HttpSenderBlockEntity.java
- ✅ HttpSenderSettingsScreen.java
- ✅ HttpClientImpl.java / IHttpClient.java
- ✅ EnumHttpMethod.java
- ✅ Network packets (CHttpSenderOpenGuiPacket, SUpdateHttpSenderValuesPacket)
- ✅ Utility classes (JsonUtils, NBTConverter, QueryBuilder)
- ✅ Assets (blockstates, models, textures, lang)

---

### 4. ~~NeoForge Mixin Config Wrong Package~~ ✅ RESOLVED

**Status:** Resolved via PR #4 (fix/neoforge-mixin-config)

**File renamed:** `eirarelay.neoforge.mixins.json`
**Package fixed to:** `no.eira.relay.mixin`
**Compatibility level:** Updated to JAVA_21

---

### 5. ~~Memory Leak - Handler Not Cleaned Up~~ ✅ RESOLVED

**Status:** Resolved via PR #6 (fix/handler-cleanup-on-remove)

**Implementation:**
- Added `unregisterHandler(String url)` method to `IHttpServer` interface
- Implemented in `HttpServerImpl` to remove handlers from both map and server
- Added `onRemove()` method to both `HttpReceiverBlock` and `HttpSenderBlock`
- Handlers are now properly cleaned up when blocks are destroyed

---

### 6. ~~Event System Migration Needed~~ ✅ RESOLVED

**Status:** Already implemented in feenixnet branch (merged via PR #2)

NeoForge event handlers are fully implemented in `EiraRelay.java`:
- `NeoForge.EVENT_BUS` for server lifecycle events
- Proper startup/shutdown handling

---

### 7. ~~Default LAN Binding (Security)~~ ✅ RESOLVED

**Status:** Resolved via PR #7 (fix/default-localhost-binding)

**Implementation:**
- Changed `DEFAULT_BIND_ADDRESS` to `127.0.0.1` in `HttpServerImpl.java`
- Server now binds to localhost by default for security

---

### 8. ~~Inconsistent Package Naming~~ ✅ RESOLVED

**Status:** Resolved via PR #8 (refactor/rename-eira-relay)

**Implementation:**
- Package renamed from `com.clapter.httpautomator` to `no.eira.relay`
- Mod ID changed from `httpautomator` to `eirarelay`
- All assets renamed from `assets/httpautomator/` to `assets/eirarelay/`
- All references updated throughout codebase

---

## Medium Debt

### 9. Unused Imports

**Severity:** Medium
**Impact:** Code cleanliness
**Status:** Only affects disabled modules (common/forge for MC 1.20.2)

**Files:**
| File | Import | Line | Module Status |
|------|--------|------|---------------|
| `common/.../block/HttpReceiverBlock.java` | `WorldLoadEvent` | 9 | Disabled |
| `forge/.../HttpAutomator.java` | `WorldLoadEvent` | 6 | Disabled |

**Note:** Not blocking since these modules are disabled in settings.gradle

---

### 10. Deprecated Block Methods

**Severity:** Medium
**Impact:** Future compatibility
**Status:** Only affects disabled modules (common/ for MC 1.20.2)

**Note:** NeoForge 1.21.1 implementation uses current API methods

---

## Low Debt

### 11. Missing Unit Tests

**Severity:** Low
**Impact:** Regression risk

No automated tests for:
- HTTP server functionality
- Handler registration
- Request parsing
- Block entity persistence

---

### 12. Commented-Out Code

**Severity:** Low
**Impact:** Code cleanliness

Various files contain commented neighbor update code that should be removed or restored.

---

## Debt Reduction Summary

| Priority | Debt Item | PR# | Branch | Status |
|----------|-----------|-----|--------|--------|
| 1 | Merge dev branch | #1 | merge/dev-to-main | ✅ Complete |
| 2 | Merge feenixnet | #2 | merge/feenixnet-to-main | ✅ Complete |
| 3 | Merge docs | #3 | docs/add-claude-md-and-analysis-structure | ✅ Complete |
| 4 | Fix NeoForge mixin | #4 | fix/neoforge-mixin-config | ✅ Complete |
| 5 | Port HTTP Sender | #5 | feature/neoforge-http-sender | ✅ Complete |
| 6 | Fix memory leak | #6 | fix/handler-cleanup-on-remove | ✅ Complete |
| 7 | Default to localhost | #7 | fix/default-localhost-binding | ✅ Complete |
| 8 | Rename to Eira Relay | #8 | refactor/rename-eira-relay | ✅ Complete |

**All critical technical debt has been resolved.**
