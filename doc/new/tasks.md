# Immediate Tasks: Happy HTTP Mod

## Critical Priority

### 1. Merge `dev` Branch to `main`

**Status:** Blocking all other feature work

**What's in dev branch:**
- HTTP Sender Block (complete implementation)
- Global parameters support
- HTTP client for sending requests
- Enhanced GUIs with parameter editing
- Power modes (Toggle/Timer)
- JSON utilities
- Many bug fixes and improvements

**Files to merge:**
```
common/src/main/java/.../block/HttpSenderBlock.java
common/src/main/java/.../blockentity/HttpSenderBlockEntity.java
common/src/main/java/.../client/gui/HttpSenderSettingsScreen.java
common/src/main/java/.../client/gui/BaseBlockScreen.java
common/src/main/java/.../client/gui/widgets/EditBoxPair.java
common/src/main/java/.../client/gui/widgets/ScrollableWidget.java
common/src/main/java/.../http/HttpClientImpl.java
common/src/main/java/.../http/api/IHttpClient.java
common/src/main/java/.../enums/EnumHttpMethod.java
common/src/main/java/.../enums/EnumPoweredType.java
common/src/main/java/.../enums/EnumTimerUnit.java
common/src/main/java/.../platform/config/GlobalParam.java
common/src/main/java/.../utils/JsonUtils.java
common/src/main/java/.../utils/NBTConverter.java
common/src/main/java/.../utils/ParameterReader.java
common/src/main/java/.../utils/QueryBuilder.java
+ network packets, registry updates, assets
```

**Steps:**
1. Create PR from `dev` to `main`
2. Resolve any merge conflicts
3. Test both blocks work correctly
4. Merge and tag release

---

### 2. Fix NeoForge Mixin Config

**File:** `neoforge/src/main/resources/neoforge.neoforge.mixins.json`

**Issue:** Package name is wrong
```json
"package": "com.example.examplemod.mixin"  // WRONG
```

**Fix:**
```json
"package": "com.clapter.httpautomator.mixin"  // CORRECT
```

---

### 3. Complete NeoForge Implementation

**Missing components:**
- Server lifecycle event handlers (ServerStartingEvent, etc.)
- Network packet registration and handling
- HTTP server integration
- Configuration loading

**Reference:** Use Forge implementation as template

---

## High Priority

### 4. Clean Up Unused Imports

**Files:**
- `common/.../block/HttpReceiverBlock.java` line 9 - unused `WorldLoadEvent`
- `forge/.../HttpAutomator.java` line 6 - unused `WorldLoadEvent`

---

### 5. Add Handler Cleanup on Block Removal

**Issue:** HTTP handlers are not removed when blocks are destroyed

**Location:** `HttpReceiverBlock.onRemove()` and `HttpSenderBlock.onRemove()`

**Fix:** Call `httpServer.unregisterHandler(url)` when block is removed

---

### 6. Default to Localhost Binding

**Security Issue:** Default bind to `192.168.0.1` exposes server to LAN

**Recommendation:** Default to `127.0.0.1` for security, require explicit config for LAN

---

## Medium Priority

### 7. Add HTTP Method Enforcement

**Issue:** Receivers should only accept POST, but method not strictly enforced

**Location:** `HttpReceiverBlockHandler.java`

---

### 8. Add Unit Tests for HTTP Layer

**Missing:** No automated tests for HTTP server functionality

**Priority areas:**
- Handler registration
- Request routing
- Parameter parsing
- Response generation

---

### 9. Document Actual Config File Locations

**Issue:** Documentation mentions `happyhttp-common.toml` but code uses different paths

**Action:** Verify and document correct config file paths for each platform

---

### 10. Complete Fabric Implementation

**Missing:** Most core functionality

**Lower priority** than NeoForge due to user base

---

## Low Priority

### 11. Remove Commented-Out Code

**Location:** Various files have commented neighbor update code

---

### 12. Add Observability/Logging

**Recommendation:** Add structured logging for:
- HTTP server startup/shutdown
- Request handling
- Handler registration

---

## Task Dependencies

```
Merge dev → Fix imports → Add handler cleanup → NeoForge completion
                                              ↘
                                                Fabric completion
```

## Estimated Effort

| Task | Complexity | Files |
|------|------------|-------|
| Merge dev | Medium | ~60 files |
| Fix mixin config | Trivial | 1 file |
| NeoForge completion | High | ~10 files |
| Unused imports | Trivial | 2 files |
| Handler cleanup | Low | 2 files |
| Localhost default | Low | 1 file |
| Method enforcement | Low | 1 file |
| Unit tests | High | New files |
| Config docs | Low | Documentation |
| Fabric completion | High | ~10 files |
