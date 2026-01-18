# Technical Debt Analysis: Eira Relay

> **Rebranding:** The mod is being renamed from "Happy HTTP" / "HttpAutomator" to "Eira Relay" (www.eira.no)

## Overview

This document identifies technical debt in the codebase and provides a migration path to the latest NeoForge version.

---

## Critical Debt

### 1. ~~Unmerged `dev` Branch~~ ✅ RESOLVED

**Status:** Resolved via PR #1 (merge/dev-to-main)

The dev branch has been merged. However, note that the dev code targets MC 1.20.2,
while the main branch now targets MC 1.21.1 via NeoForge.

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

### 4. ~~NeoForge Module Partially Complete~~ ✅ RESOLVED

**Status:** NeoForge module now has both blocks implemented

**Current state:**
- ✅ HttpReceiverBlock for 1.21.1
- ✅ HttpReceiverBlockEntity for 1.21.1
- ✅ HttpReceiverSettingsScreen for 1.21.1
- ✅ HttpSenderBlock for 1.21.1 (NEW)
- ✅ HttpSenderBlockEntity for 1.21.1 (NEW)
- ✅ HttpSenderSettingsScreen for 1.21.1 (NEW)
- ✅ HTTP Server implementation
- ✅ HTTP Client implementation (NEW)
- ✅ Registry system (BlockRegistry, ItemRegistry, BlockEntityRegistry)
- ✅ All network packets
- ✅ Config system (HttpServerConfig)
- ✅ Server lifecycle event handlers
- ✅ Mixin config (fixed via PR #4)

**Resolution:** Complete the missing pieces incrementally

---

### 3. Forge-Specific APIs in Common Code

**Severity:** High
**Impact:** Blocks multi-platform abstraction

**Problematic imports in forge module:**
```java
// FMLJavaModLoadingContext - Forge-specific
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// SimpleChannel/PacketDistributor - Forge-specific networking
import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.network.PacketDistributor;
```

**Files affected:**
- `forge/.../platform/registry/BlockRegistry.java`
- `forge/.../platform/registry/ItemRegistry.java`
- `forge/.../platform/registry/BlockEntityRegistry.java`
- `forge/.../platform/network/PacketHandler.java`
- `forge/.../HttpAutomator.java`

**Resolution:** Already abstracted via platform interfaces, NeoForge needs its own implementations

---

## High Debt

### 4. ~~NeoForge Mixin Config Wrong Package~~ ✅ RESOLVED

**Status:** Resolved via PR #4 (fix/neoforge-mixin-config)

**File renamed:** `httpautomator.neoforge.mixins.json`
**Package fixed to:** `com.clapter.httpautomator.mixin`
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

NeoForge event handlers are fully implemented in `HttpAutomator.java`:
- `NeoForge.EVENT_BUS` for server lifecycle events
- Proper startup/shutdown handling

---

## Medium Debt

### 7. Unused Imports

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

### 8. Deprecated Block Methods

**Severity:** Medium
**Impact:** Future compatibility
**Status:** Only affects disabled modules (common/ for MC 1.20.2)

**Note:** NeoForge 1.21.1 implementation uses current API methods

---

### 9. ~~Default LAN Binding (Security)~~ ✅ RESOLVED

**Status:** Resolved via PR #7 (fix/default-localhost-binding)

**Implementation:**
- Changed `DEFAULT_BIND_ADDRESS` to `127.0.0.1` in `HttpServerImpl.java`
- Server now binds to localhost by default for security

---

### 10. No HTTP Method Enforcement

**Severity:** Medium
**Impact:** API inconsistency

**Issue:** Receivers documented as POST-only but method not strictly checked

---

## Low Debt

### 11. Commented-Out Code

**Severity:** Low
**Impact:** Code cleanliness

Various files contain commented neighbor update code that should be removed or restored.

---

### 12. Missing Unit Tests

**Severity:** Low
**Impact:** Regression risk

No automated tests for:
- HTTP server functionality
- Handler registration
- Request parsing
- Block entity persistence

---

### 13. Inconsistent Package Naming

**Severity:** Low
**Impact:** Confusion

- Mod ID: `httpautomator` (code) vs `happyhttp` (assets)
- Some files reference old package names

---

## NeoForge Migration Path

### Step 1: Fix Mixin Config

```json
// neoforge/src/main/resources/neoforge.neoforge.mixins.json
{
  "package": "com.clapter.httpautomator.mixin"
}
```

### Step 2: Implement Event Handlers

```java
// neoforge/.../ExampleMod.java
@Mod(Constants.MOD_ID)
public class ExampleMod {
    public ExampleMod(IEventBus modEventBus) {
        CommonClass.init();

        // Register mod event bus listeners
        modEventBus.addListener(this::commonSetup);

        // Register game event bus listeners
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        ModNetworkPackets.register();
    }

    private void onServerStarting(ServerStartingEvent event) {
        CommonClass.onServerStarting(event.getServer());
    }

    private void onServerStarted(ServerStartedEvent event) {
        CommonClass.onServerStarted();
    }

    private void onServerStopping(ServerStoppingEvent event) {
        CommonClass.onServerStopping();
    }
}
```

### Step 3: Implement NeoForge Networking

Create `neoforge/.../platform/network/PacketHandler.java`:
- Use NeoForge's networking API
- Implement `IPayloadHandler` for packet handling
- Register packets with NeoForge channel

### Step 4: Implement NeoForge Registry

Create registry implementations using NeoForge's `DeferredRegister`:
```java
public static final DeferredRegister<Block> BLOCKS =
    DeferredRegister.create(Registries.BLOCK, Constants.MOD_ID);
```

### Step 5: Implement NeoForge Config

Create `neoforge/.../platform/config/HttpServerConfig.java`:
- Use NeoForge's config system
- Implement `IHttpServerConfig` interface

---

## Version Compatibility Matrix

| Component | Current | Latest NeoForge | Migration Effort |
|-----------|---------|-----------------|------------------|
| Event System | N/A | NeoForge.EVENT_BUS | Medium |
| Networking | N/A | IPayloadHandler | High |
| Registry | N/A | DeferredRegister | Low |
| Config | N/A | ModConfigSpec | Low |
| Mixin | 0.8.5 | Compatible | None |

---

## Debt Reduction Priority (PR-Based)

| Priority | Debt Item | PR# | Branch | Status |
|----------|-----------|-----|--------|--------|
| 1 | Merge dev branch | #1 | merge/dev-to-main | ✅ Complete |
| 2 | Merge feenixnet | #2 | merge/feenixnet-to-main | ✅ Complete |
| 3 | Merge docs | #3 | docs/add-claude-md-and-analysis-structure | ✅ Complete |
| 4 | Fix NeoForge mixin | #4 | fix/neoforge-mixin-config | ✅ Complete |
| 5 | Port HTTP Sender | #5 | feature/neoforge-http-sender | ✅ Complete |
| 6 | Fix memory leak | #6 | fix/handler-cleanup-on-remove | ✅ Complete |
| 7 | Default to localhost | #7 | fix/default-localhost-binding | ✅ Complete |
| 8 | Rename to Eira Relay | #8 | refactor/rename-eira-relay | Pending |

**Note:** Original PRs #8-11 (NeoForge events, networking, registry, config) were already
implemented by the feenixnet branch. HTTP Sender was ported directly to NeoForge 1.21.1
instead of merging incompatible MC 1.20.2 code.

See `doc/new/tasks.md` and `doc/new/backlog.md` for full PR details.
