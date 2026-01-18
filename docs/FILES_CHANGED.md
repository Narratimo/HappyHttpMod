# Files Changed for NeoForge 1.21.x Port

This document lists all files that were created, modified, or affected during the port from Forge 1.20.2 to NeoForge 1.21.x.

## Summary

- **Files Created:** 15+
- **Files Modified:** 5
- **Modules Affected:** `neoforge`, root project

---

## Root Project Files

### Modified Files

| File | Change Type | Description |
|------|-------------|-------------|
| `settings.gradle` | Modified | Disabled `forge` module, enabled `neoforge` module |
| `gradle.properties` | Modified | Added NeoForge version properties, updated Minecraft version to 1.21.1 |
| `gradle/wrapper/gradle-wrapper.properties` | Modified | Updated Gradle to 8.8 for Java 21 compatibility |

---

## NeoForge Module (`neoforge/`)

### Build Configuration

| File | Change Type | Description |
|------|-------------|-------------|
| `neoforge/build.gradle` | Modified | Updated plugin, added ASM resolution strategy, Java 21 toolchain |

### Main Mod Class

| File | Change Type | Description |
|------|-------------|-------------|
| `neoforge/src/main/java/com/clapter/httpautomator/HttpAutomator.java` | Modified | Added DeferredRegister initialization |

### Platform Implementation

| File | Status | Description |
|------|--------|-------------|
| `neoforge/src/main/java/com/clapter/httpautomator/platform/registry/BlockRegistry.java` | Created | NeoForge block registration |
| `neoforge/src/main/java/com/clapter/httpautomator/platform/registry/ItemRegistry.java` | Created | NeoForge item registration |
| `neoforge/src/main/java/com/clapter/httpautomator/platform/registry/BlockEntityRegistry.java` | Created | NeoForge block entity registration |
| `neoforge/src/main/java/com/clapter/httpautomator/platform/config/HttpServerConfig.java` | Created | NeoForge config using ModConfigSpec |
| `neoforge/src/main/java/com/clapter/httpautomator/platform/network/PacketHandler.java` | Created | NeoForge packet handling |
| `neoforge/src/main/java/com/clapter/httpautomator/platform/NeoForgePlatformHelper.java` | Created | Platform helper implementation |

### Core Classes

| File | Status | Description |
|------|--------|-------------|
| `neoforge/src/main/java/com/clapter/httpautomator/CommonClass.java` | Created | Shared initialization logic |
| `neoforge/src/main/java/com/clapter/httpautomator/Constants.java` | Created | Mod constants (ID, logger) |
| `neoforge/src/main/java/com/clapter/httpautomator/block/HttpReceiverBlock.java` | Created | HTTP Receiver block implementation |
| `neoforge/src/main/java/com/clapter/httpautomator/blockentity/HttpReceiverBlockEntity.java` | Created | Block entity for HTTP Receiver |
| `neoforge/src/main/java/com/clapter/httpautomator/client/gui/HttpReceiverSettingsScreen.java` | Created | Settings GUI screen |

### Registry Classes

| File | Status | Description |
|------|--------|-------------|
| `neoforge/src/main/java/com/clapter/httpautomator/registry/ModBlocks.java` | Created | Block registration definitions |
| `neoforge/src/main/java/com/clapter/httpautomator/registry/ModItems.java` | Created | Item registration definitions |
| `neoforge/src/main/java/com/clapter/httpautomator/registry/ModBlockEntities.java` | Created | Block entity registration definitions |
| `neoforge/src/main/java/com/clapter/httpautomator/registry/ModNetworkPackets.java` | Created | Network packet registration |

### HTTP Server

| File | Status | Description |
|------|--------|-------------|
| `neoforge/src/main/java/com/clapter/httpautomator/http/HttpServerImpl.java` | Created | HTTP server implementation |
| `neoforge/src/main/java/com/clapter/httpautomator/http/handlers/HttpReceiverBlockHandler.java` | Created | HTTP request handler |
| `neoforge/src/main/java/com/clapter/httpautomator/http/api/IHttpServer.java` | Created | HTTP server interface |
| `neoforge/src/main/java/com/clapter/httpautomator/http/api/IHttpHandler.java` | Created | HTTP handler interface |

### Network Packets

| File | Status | Description |
|------|--------|-------------|
| `neoforge/src/main/java/com/clapter/httpautomator/network/packet/SUpdateHttpReceiverValuesPacket.java` | Created | Server-bound update packet |
| `neoforge/src/main/java/com/clapter/httpautomator/network/packet/CSyncHttpReceiverValuesPacket.java` | Created | Client-bound sync packet |
| `neoforge/src/main/java/com/clapter/httpautomator/network/packet/BasePacket.java` | Created | Base packet class |

### Resources

| File | Status | Description |
|------|--------|-------------|
| `neoforge/src/main/resources/META-INF/neoforge.mods.toml` | Created | NeoForge mod metadata |
| `neoforge/src/main/resources/pack.mcmeta` | Created | Resource pack metadata |
| `neoforge/src/main/resources/META-INF/services/*` | Created | ServiceLoader configuration files |
| `neoforge/src/main/resources/assets/httpautomator/` | Copied | Textures, models, lang files |

---

## Disabled/Legacy Files

The following modules were disabled but not deleted (for reference):

| Module | Status | Reason |
|--------|--------|--------|
| `forge/` | Disabled | Legacy Forge 1.20.2 code |
| `common/` | Disabled | Code merged into loader-specific modules |

---

## Key Changes Summary

### 1. Registration System
- Changed from manual Forge registry to NeoForge `DeferredRegister`
- Must call `register(IEventBus)` in main mod constructor

### 2. Event System
- Changed from `MinecraftForge.EVENT_BUS` to `NeoForge.EVENT_BUS`
- Event classes moved to `net.neoforged.neoforge.event` package

### 3. Configuration
- Changed from `ForgeConfigSpec` to `ModConfigSpec`
- Located in `net.neoforged.neoforge.common` package

### 4. Mod Metadata
- Changed from `mods.toml` to `neoforge.mods.toml`
- Updated dependency format and version ranges

### 5. Build System
- Upgraded Gradle to 8.8
- Upgraded to Java 21 toolchain
- Added ASM version resolution to fix module conflicts

