# NeoForge 1.21.x Porting Guide

## Issue Summary

**Original Issue:** The mod was not compatible with Minecraft 1.21.x on Forge. Users reported crashes when launching the game.

**Environment:**
- Minecraft Version: 1.21.x (1.21.1 specifically targeted)
- Original Mod Loader: Forge (for 1.20.2)
- New Mod Loader: NeoForge (required for 1.21.x)
- Affected: Both Clients and Servers

**Root Cause:** Starting with Minecraft 1.20.5, the Forge mod loader split into two projects:
- **Forge** - Continued support for older versions (1.20.4 and below)
- **NeoForge** - The new standard for Minecraft 1.20.5+ and 1.21.x

The original mod was built for Forge 1.20.2 and could not run on Minecraft 1.21.x without being ported to NeoForge.

---

## Changes Made

### 1. Project Structure Changes

#### 1.1 Settings.gradle Updates
The project was reconfigured to build the NeoForge module instead of the Forge module.

**File:** `settings.gradle`

```gradle
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            name = 'Fabric'
            url = 'https://maven.fabricmc.net/'
        }
        maven {
            name = 'NeoForge'
            url = 'https://maven.neoforged.net/releases/'
        }
        maven {
            name = 'Forge'
            url = 'https://maven.minecraftforge.net/'
        }
        maven {
            name = 'Sponge Snapshots'
            url = 'https://repo.spongepowered.org/repository/maven-public/'
        }
    }
}

plugins {
    id 'org.gradle.toolchains.foojay-resolver-convention' version '0.5.0'
}

rootProject.name = 'HttpAutomator'
//include("common")  // Disabled - code moved to neoforge module
//include("forge")   // Disabled - porting to NeoForge 1.21
include("neoforge")  // Active module for 1.21.x
```

#### 1.2 Gradle Properties Updates
**File:** `gradle.properties`

```properties
# Updated for NeoForge 1.21.x
minecraft_version=1.21.1
minecraft_version_range=[1.21,1.22)
neoforge_version=21.1.77
neoforge_loader_version_range=[4,)

# Gradle settings
org.gradle.jvmargs=-Xmx3G
org.gradle.daemon=false
```

---

### 2. Build Configuration Changes

#### 2.1 NeoForge build.gradle
**File:** `neoforge/build.gradle`

Key changes:
- Updated to use `net.neoforged.gradle.userdev` plugin version `7.0.163`
- Set Java toolchain to Java 21 (required for NeoForge 1.21.x)
- Added ASM version forcing to resolve module conflicts
- Configured DeferredRegister-based registration

```gradle
plugins {
    id 'idea'
    id 'maven-publish'
    id 'net.neoforged.gradle.userdev' version '7.0.163'
    id 'java-library'
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

// Force consistent ASM versions to avoid module conflicts
configurations.all {
    resolutionStrategy {
        force 'org.ow2.asm:asm:9.7'
        force 'org.ow2.asm:asm-commons:9.7'
        force 'org.ow2.asm:asm-tree:9.7'
        force 'org.ow2.asm:asm-util:9.7'
        force 'org.ow2.asm:asm-analysis:9.7'
    }
}

dependencies {
    implementation "net.neoforged:neoforge:${neoforge_version}"
}
```

#### 2.2 Gradle Wrapper Update
**File:** `gradle/wrapper/gradle-wrapper.properties`

Updated Gradle to version 8.8 for Java 21 compatibility:

```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.8-bin.zip
```

---

### 3. NeoForge Module Code Changes

#### 3.1 Main Mod Class
**File:** `neoforge/src/main/java/com/clapter/httpautomator/HttpAutomator.java`

The main mod class was updated to:
- Use NeoForge's `@Mod` annotation
- Register DeferredRegisters to the mod event bus
- Use NeoForge event system for server lifecycle events

```java
package com.clapter.httpautomator;

import com.clapter.httpautomator.platform.config.HttpServerConfig;
import com.clapter.httpautomator.platform.registry.BlockEntityRegistry;
import com.clapter.httpautomator.platform.registry.BlockRegistry;
import com.clapter.httpautomator.platform.registry.ItemRegistry;
import com.clapter.httpautomator.registry.ModBlocks;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@Mod(Constants.MOD_ID)
public class HttpAutomator {

    public HttpAutomator(IEventBus modEventBus, ModContainer modContainer) {
        // Register config
        modContainer.registerConfig(ModConfig.Type.COMMON, HttpServerConfig.COMMON_SPEC);
        
        // Register DeferredRegisters to the mod event bus FIRST
        BlockRegistry.register(modEventBus);
        ItemRegistry.register(modEventBus);
        BlockEntityRegistry.register(modEventBus);
        
        // Initialize common code (registers blocks, items, etc.)
        CommonClass.init();
        
        // Register mod event listeners
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::addCreative);
        
        // Register game event listeners
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
        
        Constants.LOG.info("HttpAutomator initialized for NeoForge 1.21!");
    }
    
    // ... event handler methods
}
```

#### 3.2 Registry Classes
The registry system was updated to use NeoForge's `DeferredRegister`:

**File:** `neoforge/src/main/java/com/clapter/httpautomator/platform/registry/BlockRegistry.java`

```java
package com.clapter.httpautomator.platform.registry;

import com.clapter.httpautomator.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BlockRegistry implements IBlockRegistry {

    private static final DeferredRegister<Block> BLOCKS = 
        DeferredRegister.create(BuiltInRegistries.BLOCK, Constants.MOD_ID);

    @Override
    public void registerBlock(ResourceLocation identifier, Supplier<Block> block) {
        BLOCKS.register(identifier.getPath(), block);
    }

    @Override
    public void finishRegistry() {
        // Registration happens via event bus
    }
    
    // Critical: This method must be called from the main mod class
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
```

Similar changes were made to:
- `ItemRegistry.java`
- `BlockEntityRegistry.java`

#### 3.3 Configuration Class
**File:** `neoforge/src/main/java/com/clapter/httpautomator/platform/config/HttpServerConfig.java`

Updated to use NeoForge's config system:

```java
package com.clapter.httpautomator.platform.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class HttpServerConfig implements IHttpServerConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final HttpServerConfig INSTANCE;
    
    private static ModConfigSpec.ConfigValue<Integer> port;
    
    static {
        Pair<HttpServerConfig, ModConfigSpec> pair = 
            new ModConfigSpec.Builder().configure(HttpServerConfig::new);
        INSTANCE = pair.getLeft();
        COMMON_SPEC = pair.getRight();
    }

    public HttpServerConfig() {
        // Required no-arg constructor for ServiceLoader
    }

    public HttpServerConfig(ModConfigSpec.Builder builder) {
        builder.push("Http Server Settings");
        port = builder
                .comment("Http Server Port")
                .defineInRange("port", 8080, 0, 65535);
        builder.pop();
    }

    @Override
    public int getPort() {
        return port.get();
    }
}
```

---

### 4. Resource File Changes

#### 4.1 NeoForge Mod Metadata
**File:** `neoforge/src/main/resources/META-INF/neoforge.mods.toml`

```toml
modLoader = "javafml"
loaderVersion = "[4,)"
license = "All Rights Reserved"
issueTrackerURL = "https://github.com/clapter/httpautomator/issues"

[[mods]]
modId = "httpautomator"
version = "1.1.0"
displayName = "HTTP Automator"
authors = "Clapter"
description = '''
HTTP automation mod for Minecraft. Connect your Minecraft world to external services.
'''

[[dependencies.httpautomator]]
modId = "neoforge"
type = "required"
versionRange = "[21.1,)"
ordering = "NONE"
side = "BOTH"

[[dependencies.httpautomator]]
modId = "minecraft"
type = "required"
versionRange = "[1.21,1.22)"
ordering = "NONE"
side = "BOTH"
```

#### 4.2 Resource Pack Metadata
**File:** `neoforge/src/main/resources/pack.mcmeta`

```json
{
  "pack": {
    "pack_format": 26,
    "description": "Happy HTTP Mod resources"
  }
}
```

---

### 5. API Changes (Forge → NeoForge)

| Forge (1.20.2) | NeoForge (1.21.x) |
|----------------|-------------------|
| `MinecraftForge.EVENT_BUS` | `NeoForge.EVENT_BUS` |
| `ForgeConfigSpec` | `ModConfigSpec` |
| `@Mod.EventBusSubscriber` | `@EventBusSubscriber` (from neoforged) |
| `ForgeRegistry` | `DeferredRegister` with `BuiltInRegistries` |
| `net.minecraftforge.*` packages | `net.neoforged.*` packages |
| `mods.toml` | `neoforge.mods.toml` |

---

### 6. Common Issues Encountered

#### 6.1 ASM Module Conflict
**Error:** `IllegalStateException: Module named org.objectweb.asm.tree.analysis was already on the JVMs module path`

**Solution:** Force consistent ASM versions in `build.gradle`:
```gradle
configurations.all {
    resolutionStrategy {
        force 'org.ow2.asm:asm:9.7'
        force 'org.ow2.asm:asm-commons:9.7'
        force 'org.ow2.asm:asm-tree:9.7'
        force 'org.ow2.asm:asm-util:9.7'
        force 'org.ow2.asm:asm-analysis:9.7'
    }
}
```

#### 6.2 Blocks Not Appearing in Creative Inventory
**Error:** Blocks/items not visible in creative mode search.

**Cause:** DeferredRegisters not connected to the mod event bus.

**Solution:** Call `BlockRegistry.register(modEventBus)` in the main mod class constructor **before** `CommonClass.init()`.

#### 6.3 Java Version Mismatch
**Error:** `Unsupported class file major version 65`

**Cause:** NeoForge 1.21.x requires Java 21, but an older Java version was being used.

**Solution:** 
1. Install Java 21
2. Set `JAVA_HOME` to Java 21 path
3. Update `build.gradle` to use Java 21 toolchain

---

## Build and Run Instructions

### Prerequisites
- Java 21 JDK installed
- Gradle 8.8+ (handled by wrapper)

### Building the Mod

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
.\gradlew :neoforge:build --no-daemon
```

The built JAR will be in: `neoforge/build/libs/`

### Running the Development Client

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
.\gradlew :neoforge:runClient --no-daemon
```

### Testing the Mod

1. Create a new Creative mode world
2. Press **E** to open inventory
3. Search for "receiver"
4. Place the HTTP Receiver block
5. Right-click to configure with endpoint (e.g., `/test`)
6. Click Save
7. Test with HTTP request:
   ```powershell
   Invoke-WebRequest -Method POST -Uri "http://localhost:8080/test" -UseBasicParsing
   ```

---

## Version Compatibility Matrix

| Mod Version | Minecraft | Mod Loader | Java |
|-------------|-----------|------------|------|
| 1.0.x | 1.20.2 | Forge 48.x | 17 |
| 1.1.x | 1.21.1 | NeoForge 21.1.x | 21 |

---

## References

- [NeoForge Documentation](https://docs.neoforged.net/)
- [NeoForge Migration Guide](https://docs.neoforged.net/docs/migration/)
- [NeoGradle Documentation](https://github.com/neoforged/NeoGradle)
- [Minecraft 1.21 Changes](https://minecraft.wiki/w/Java_Edition_1.21)

---

*Document created: December 2024*
*Last updated: December 12, 2025*

