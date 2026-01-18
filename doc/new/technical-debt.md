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

### 2. Version Incompatibility (NEW)

**Severity:** Critical
**Impact:** HTTP Sender not available on NeoForge 1.21.1

**Problem:**
- `dev` branch code (common/, forge/) targets **Minecraft 1.20.2** with Forge
- `feenixnet` branch code (neoforge/) targets **Minecraft 1.21.1** with NeoForge
- These are incompatible Minecraft versions

**Current State:**
- NeoForge module (1.21.1): HTTP Receiver only - **BUILDS AND WORKS**
- Common/Forge modules (1.20.2): HTTP Receiver + HTTP Sender - **DISABLED**
- settings.gradle only includes `neoforge` module

**Resolution:**
1. Port HTTP Sender from common/ to neoforge/ for MC 1.21.1
2. Update all common/forge code to MC 1.21.1 (or maintain separate versions)

---

### 3. HTTP Sender Missing on NeoForge 1.21.1

**Severity:** Critical
**Impact:** Core feature not available on current build

**What's missing in neoforge/ module:**
- HttpSenderBlock.java
- HttpSenderBlockEntity.java
- HttpSenderSettingsScreen.java
- HttpClientImpl.java
- Global parameters support
- Power modes (Toggle/Timer)
- Related network packets

**Source code exists in:** `common/src/main/java/` (for MC 1.20.2)

**Resolution:** Port HTTP Sender code from common/ to neoforge/ with 1.21.1 API updates

---

### 4. NeoForge Module Partially Complete

**Severity:** High
**Impact:** Some features work, others need completion

**What feenixnet added:**
- ✅ HttpReceiverBlock for 1.21.1
- ✅ HttpReceiverBlockEntity for 1.21.1
- ✅ HttpReceiverSettingsScreen for 1.21.1
- ✅ HTTP Server implementation
- ✅ Registry system (BlockRegistry, ItemRegistry, BlockEntityRegistry)
- ✅ Network packets (CSyncHttpReceiverValuesPacket, SUpdateHttpReceiverValuesPacket)
- ✅ Config system (HttpServerConfig)

**Still needs:**
- ❌ HTTP Sender implementation (see item #3)
- ❌ Server lifecycle event handlers (ServerStarting, ServerStarted, ServerStopping)
- ⚠️ Mixin config package name fix (see item below)

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

### 4. NeoForge Mixin Config Wrong Package

**Severity:** High
**Impact:** Mixins won't load for NeoForge

**File:** `neoforge/src/main/resources/neoforge.neoforge.mixins.json`

**Current:**
```json
{
  "package": "com.example.examplemod.mixin"
}
```

**Should be:**
```json
{
  "package": "com.clapter.httpautomator.mixin"
}
```

---

### 5. Memory Leak - Handler Not Cleaned Up

**Severity:** High
**Impact:** HTTP handlers persist after block destruction

**Location:** `HttpReceiverBlock.onRemove()` / `HttpSenderBlock.onRemove()`

**Issue:** When a block is destroyed, its HTTP handler remains registered in `HttpServerImpl.handlerMap`

**Resolution:**
```java
@Override
public void onRemove(BlockState state, Level level, BlockPos pos, ...) {
    if (!level.isClientSide()) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof HttpReceiverBlockEntity receiver) {
            CommonClass.HTTP_SERVER.unregisterHandler(receiver.getUrl());
        }
    }
    super.onRemove(state, level, pos, ...);
}
```

---

### 6. Event System Migration Needed

**Severity:** High
**Impact:** NeoForge uses different event system

**Forge pattern:**
```java
MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
```

**NeoForge pattern:**
```java
NeoForge.EVENT_BUS.addListener(this::onServerStarting);
// OR
@EventBusSubscriber(modid = MOD_ID, bus = Bus.FORGE)
```

---

## Medium Debt

### 7. Unused Imports

**Severity:** Medium
**Impact:** Code cleanliness

**Files:**
| File | Import | Line |
|------|--------|------|
| `common/.../block/HttpReceiverBlock.java` | `WorldLoadEvent` | 9 |
| `forge/.../HttpAutomator.java` | `WorldLoadEvent` | 6 |

---

### 8. Deprecated Block Methods

**Severity:** Medium
**Impact:** Future compatibility

**File:** `common/.../block/HttpReceiverBlock.java`

**Methods flagged:**
- `onPlace()` - deprecated but still functional
- `onRemove()` - deprecated but still functional
- `getSignal()` - deprecated but still functional
- `createBlockStateDefinition()` - deprecated but still functional

**Note:** Comment in code says "This is not a problem in Forge/NeoForge"

---

### 9. Default LAN Binding (Security)

**Severity:** Medium
**Impact:** Security exposure

**Current default:** `192.168.0.1` (LAN)
**Recommended default:** `127.0.0.1` (localhost)

**File:** `forge/.../platform/config/HttpServerConfig.java`

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
| 1 | Merge dev branch | #1 | merge dev→main | Pending |
| 2 | Merge feenixnet | #2 | merge feenixnet→main | Pending |
| 3 | Fix NeoForge mixin | #4 | fix/neoforge-mixin | Pending |
| 4 | Clean unused imports | #5 | fix/unused-imports | Pending |
| 5 | Fix memory leak | #6 | fix/handler-cleanup | Pending |
| 6 | Default to localhost | #7 | fix/localhost-default | Pending |
| 7 | NeoForge events | #8 | feature/neoforge-events | Pending |
| 8 | NeoForge networking | #9 | feature/neoforge-network | Pending |
| 9 | NeoForge registry | #10 | feature/neoforge-registry | Pending |
| 10 | NeoForge config | #11 | feature/neoforge-config | Pending |
| 11 | Rename to Eira Relay | #12 | refactor/rename-eira | Pending |

See `doc/new/tasks.md` and `doc/new/backlog.md` for full PR details.
