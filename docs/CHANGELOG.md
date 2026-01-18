# Changelog

All notable changes to the HTTP Automator mod will be documented in this file.

## [1.1.0] - 2024-12-12

### Added
- **NeoForge 1.21.x Support** - Full port to NeoForge mod loader for Minecraft 1.21.1
- New `neoforge` module with complete NeoForge implementation
- Java 21 support (required for NeoForge 1.21.x)

### Changed
- Migrated from Forge to NeoForge for Minecraft 1.21.x compatibility
- Updated Gradle wrapper to version 8.8
- Updated NeoGradle plugin to version 7.0.163
- Refactored registry system to use NeoForge's `DeferredRegister`
- Updated configuration system to use `ModConfigSpec` (NeoForge)
- Updated event system to use `NeoForge.EVENT_BUS`
- Updated mod metadata format to `neoforge.mods.toml`
- Resource pack format updated to version 26

### Fixed
- **Critical:** Resolved crash on launch with Minecraft 1.21.x ([#Issue])
- Fixed ASM module conflict causing `IllegalStateException`
- Fixed blocks not appearing in creative inventory
- Fixed DeferredRegister not being connected to mod event bus

### Technical Changes

#### Build System
- Added ASM version forcing to resolve module conflicts:
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

#### API Migrations
| Old (Forge 1.20.2) | New (NeoForge 1.21.x) |
|--------------------|----------------------|
| `MinecraftForge.EVENT_BUS` | `NeoForge.EVENT_BUS` |
| `ForgeConfigSpec` | `ModConfigSpec` |
| `net.minecraftforge.*` | `net.neoforged.*` |
| `mods.toml` | `neoforge.mods.toml` |

### Deprecated
- Forge 1.20.2 module (`forge/`) - No longer actively maintained

---

## [1.0.0] - Initial Release

### Added
- HTTP Receiver Block - Receives HTTP requests and outputs redstone signal
- Built-in HTTP server (default port: 8080)
- Configurable endpoints per block
- GUI for configuring HTTP Receiver blocks
- Support for POST requests
- Redstone signal toggling on HTTP request

### Supported Platforms
- Minecraft 1.20.2
- Forge 48.x
- Java 17

---

## Version Support

| Version | Minecraft | Mod Loader | Status |
|---------|-----------|------------|--------|
| 1.1.x | 1.21.1 | NeoForge 21.1.x | ✅ Active |
| 1.0.x | 1.20.2 | Forge 48.x | ⚠️ Legacy |

---

## Migration Notes

### Upgrading from 1.0.x to 1.1.x

If you're upgrading from the Forge version (1.0.x) to the NeoForge version (1.1.x):

1. **Remove the old mod JAR** from your mods folder
2. **Install NeoForge** (not Forge) for Minecraft 1.21.x
3. **Download the new mod JAR** (`HttpAutomator-neoforge-1.21.1-1.1.0.jar`)
4. **Place in mods folder** and launch

**Note:** World data should be compatible, but always backup your worlds before upgrading.

