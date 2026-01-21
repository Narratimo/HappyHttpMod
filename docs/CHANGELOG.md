# Changelog

All notable changes to Eira Relay will be documented in this file.

---

## [1.2.0] - 2026-01-21

### Added
- **Player Detection** (#94) - HTTP Receiver blocks can detect nearby players when triggered
  - Configurable detection radius (1-64 blocks)
  - Player UUID, name, and distance included in JSON response
  - Player info passed to Eira Core events
- **Scene Sequencer Block** (#74) - Chain multiple HTTP calls with delays
  - Configurable steps with URL, method, delay, and conditions
  - Loop and stop behavior options
  - Visual feedback during sequence execution
- **Adventure Toolkit Documentation** (#75) - Ready-made examples and integration guides

### Changed
- HTTP Receiver responses now use JSON format with detailed block information
- Improved documentation with comprehensive roadmap and tech debt tracking

---

## [1.1.0] - 2026-01-19

### Added
- **NeoForge 1.21.4 Support** (#73) - Multi-version architecture
- **Eira Core Integration** (#71, #72) - Event bus for cross-mod communication
  - Teams, Players, Stories, Adventures APIs
  - Event types: HttpReceived, ExternalTrigger, RedstoneChange, CheckpointCompleted
- **New HTTP Endpoints** (#67)
  - `/trigger/{id}` - Named triggers for QR codes and sensors
  - `/status` - Health check with uptime and version
  - `/redstone` - Direct redstone control at coordinates
  - `/broadcast` - Send chat/title/actionbar messages
- **Rate Limiting** (#67) - Configurable per-IP request limits
- **CORS Support** (#67) - Cross-origin requests for web integrations
- **Visual Feedback** (#68) - Particles and glow effects when blocks are active
- **Auth UX Improvements** (#69) - Masked token fields, copy/generate buttons
- **API Reference Documentation** (#70)

### Changed
- Renamed from "HTTP Automator" to "Eira Relay"
- Package renamed from `com.clapters` to `no.eira.relay`
- Improved error handling and logging

---

## [1.0.5] - 2026-01-15

### Added
- **Secret Token Validation** (#66) - Protect receiver endpoints with Bearer tokens
- **Authentication Helpers** (#65) - Bearer, Basic, Custom header auth for senders
- **Discord Integration** (#65) - One-click preset for Discord webhooks
- **Parameter Editor** (#65) - Key-value editor for JSON body parameters
- **Norwegian Translations** (#64)
- **Test Button** (#63) - Verify sender configuration in-game
- **Port/IP Info** (#62) - Display local IP and port in receiver settings

---

## [1.0.0] - 2026-01-01

### Added
- **Power Modes** (#54, #60) - Switch (toggle) and Timer (pulse) modes
- **HTTP Sender Block** - Send HTTP requests when powered by redstone
- **Global Variables** (#61) - Server-wide parameters for all requests

### Changed
- Multi-version support: NeoForge 1.21.1, Forge 1.20.2

---

## [0.9.0] - 2024-12-12

### Added
- **NeoForge 1.21.x Support** - Full port to NeoForge mod loader
- Java 21 support (required for NeoForge 1.21.x)

### Changed
- Migrated from Forge to NeoForge for Minecraft 1.21.x compatibility
- Updated Gradle wrapper to version 8.8
- Updated registry system to use NeoForge's `DeferredRegister`

### Fixed
- Resolved crash on launch with Minecraft 1.21.x
- Fixed ASM module conflict causing `IllegalStateException`
- Fixed blocks not appearing in creative inventory

---

## [0.1.0] - Initial Release

### Added
- HTTP Receiver Block - Receives HTTP requests and outputs redstone signal
- Built-in HTTP server (default port: 8080)
- Configurable endpoints per block
- GUI for configuring HTTP Receiver blocks
- Support for POST requests
- Redstone signal toggling on HTTP request

---

## Version Support

| Version | Minecraft | Mod Loader | Java | Status |
|---------|-----------|------------|------|--------|
| 1.2.x | 1.21.4 | NeoForge 21.4.x | 21 | Active |
| 1.2.x | 1.21.1 | NeoForge 21.1.x | 21 | Active |
| 1.0.x | 1.20.2 | Forge 48.x | 17 | Maintained |

---

## Migration Notes

### Upgrading to 1.2.x

1. Existing HTTP Receiver blocks work without changes
2. JSON responses are now returned instead of plain text
3. Player detection is opt-in (disabled by default)
4. Scene Sequencer is a new block in the creative menu

### Upgrading from Forge to NeoForge

1. Remove the old mod JAR from your mods folder
2. Install NeoForge (not Forge) for Minecraft 1.21.x
3. Download the NeoForge version of Eira Relay
4. Place in mods folder and launch

**Note:** Always backup your worlds before upgrading.
