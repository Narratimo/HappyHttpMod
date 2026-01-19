# Eira Relay

A Minecraft mod that bridges in-game events with external HTTP systems. Trigger redstone from webhooks, send HTTP requests from redstone, and build interactive experiences that connect the physical and virtual worlds.

Maintained by [Eira](https://www.eira.no), a non-commercial organisation teaching young people to code.

## Project Status

**Current version:** 1.1.0
**Lifecycle stage:** Beta - actively maintained, core features stable
**Platforms:** NeoForge 1.21.1, NeoForge 1.21.4, Forge 1.20.2

The mod is functional for production use cases but lacks some polish features. Breaking changes are unlikely but possible before 1.0 stable release.

## Why This Project Exists

Existing Minecraft automation mods focus on in-game mechanics. Eira Relay fills a gap: connecting Minecraft to external systems without requiring programming knowledge.

**Use cases we designed for:**
- Educational escape rooms where QR codes trigger in-game events
- Home automation integration (lights, doors, sensors)
- Discord/Slack notifications from in-game achievements
- Interactive museum exhibits with physical triggers
- Team-based scavenger hunts with checkpoint tracking

**Design principles:**
- Configuration over code - all setup via in-game GUI
- Security by default - localhost binding, token authentication
- Builder-friendly - visual feedback, clear error messages

## Core Features

| Feature | Description |
|---------|-------------|
| **HTTP Receiver Block** | Listens for webhooks, emits redstone signal |
| **HTTP Sender Block** | Sends HTTP requests when powered |
| **Power Modes** | Toggle (switch) or pulse (timer with configurable duration) |
| **Authentication** | Bearer, Basic, Custom headers for outgoing requests |
| **Secret Tokens** | Protect receiver endpoints with token validation |
| **Visual Feedback** | Particles and glow effects indicate block activity |
| **Built-in Endpoints** | `/trigger/{id}`, `/status`, `/redstone`, `/broadcast` |
| **Player Detection** | Detect nearby players when receiver is triggered |

### What This Project Does Not Do

- **No in-game scripting** - Use external tools for complex logic
- **No persistent state** - Stateless by design; use Eira Server for game state
- **No cross-server sync** - Single server only
- **No Fabric support** - Paused indefinitely

## Quick Start

### Requirements
- Minecraft with NeoForge 1.21.1+ or Forge 1.20.2
- Java 21 (NeoForge) or Java 17 (Forge)

### Installation
1. Download from [Releases](https://github.com/Narratimo/HappyHttpMod/releases)
2. Place `.jar` in your `mods` folder
3. Launch Minecraft with the mod loader

### Minimal Example

**Receive a webhook:**
1. Place HTTP Receiver block, right-click to configure
2. Set endpoint: `/door`
3. Connect redstone to a piston door
4. Trigger: `curl -X POST http://localhost:8080/door`

**Send a webhook:**
1. Place HTTP Sender block, right-click to configure
2. Set URL: `https://discord.com/api/webhooks/...`
3. Set method: POST, add parameter `content` = `Player reached checkpoint!`
4. Power with redstone button

## Usage Overview

### HTTP Receiver Block
Receives POST requests and outputs redstone signal.

```bash
# Basic trigger
curl -X POST http://localhost:8080/your-endpoint

# With authentication
curl -X POST http://localhost:8080/your-endpoint \
  -H "Authorization: Bearer your-secret-token"
```

**Response format (JSON):**
```json
{
  "status": "ok",
  "message": "Signal sent to 1 block(s)",
  "blocks": [
    {"x": 100, "y": 64, "z": 200, "player": {"uuid": "...", "name": "Steve", "distance": 3.5}}
  ]
}
```

### HTTP Sender Block
Sends requests when powered by redstone.

- **GET**: Parameters appended as query string
- **POST**: Parameters sent as JSON body
- **Auth types**: None, Bearer, Basic (user:pass), Custom header

### Built-in Endpoints

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/trigger/{id}` | POST | Named triggers for QR codes, sensors |
| `/status` | GET | Health check with uptime and version |
| `/redstone` | POST | Emit redstone at coordinates `{"x":0,"y":64,"z":0}` |
| `/broadcast` | POST | Send chat/title/actionbar to players |

## Configuration

File: `.minecraft/config/eirarelay-common.toml`

```toml
[server]
port = 8080
bind_address = "127.0.0.1"  # Use "0.0.0.0" for external access
rate_limit = 0              # Requests per minute per IP (0 = disabled)
cors_origins = ""           # Comma-separated origins (empty = disabled)
```

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                    Eira Relay Mod                       │
├─────────────────────────────────────────────────────────┤
│  HTTP Server (com.sun.net.httpserver)                   │
│    ├── /trigger/{id}  → TriggerHandler                  │
│    ├── /status        → StatusHandler                   │
│    ├── /redstone      → RedstoneHandler                 │
│    ├── /broadcast     → BroadcastHandler                │
│    └── /{custom}      → HttpReceiverBlockHandler        │
├─────────────────────────────────────────────────────────┤
│  Blocks                                                 │
│    ├── HttpReceiverBlock  → receives HTTP, emits signal │
│    └── HttpSenderBlock    → receives signal, sends HTTP │
├─────────────────────────────────────────────────────────┤
│  HTTP Client (java.net.http)                            │
│    └── Async requests with retry logic                  │
├─────────────────────────────────────────────────────────┤
│  Eira Core (optional dependency)                        │
│    └── Event bus for cross-mod communication            │
└─────────────────────────────────────────────────────────┘
```

**Extension points:**
- Custom `IHttpHandler` implementations for new endpoints
- Event subscribers via Eira Core API
- Block entity subclasses for specialised behaviour

---

## Roadmap

### Completed Features

| Feature | PR | Status |
|---------|-----|--------|
| HTTP Receiver/Sender blocks | - | Done |
| Power modes (Switch/Timer) | #54, #60 | Done |
| Authentication (Bearer/Basic/Custom) | #65 | Done |
| Secret token validation | #66 | Done |
| Visual feedback (particles, glow) | #68 | Done |
| Built-in endpoints (/trigger, /status, /redstone, /broadcast) | #67 | Done |
| Rate limiting and CORS | #67 | Done |
| Discord webhook preset | #65 | Done |
| Norwegian translations | #64 | Done |
| Test button for sender | #63 | Done |
| NeoForge 1.21.4 support | #73 | Done |
| Eira Core integration | #71, #72 | Done |
| Player detection on triggers | #94 | Done |

### In Progress (Phase 2)

| Feature | Issue | Description |
|---------|-------|-------------|
| Response Variables | #76 | Parse API responses, store values, use in subsequent requests |
| Comparator Output | #77 | Analog redstone signal based on HTTP response status |
| Visual Wiring Preview | #79 | Debug overlay showing block connections |
| Request Templates | #81 | Save/load block configurations as presets |

### Planned (Phase 3)

| Feature | Issue | Priority | Description |
|---------|-------|----------|-------------|
| WebSocket Support | #80 | High | Real-time bidirectional communication |
| Scheduled Requests | #82 | High | Time-based periodic HTTP triggers |
| Data Flow in Sequences | #83 | Medium | Pass response data between Scene Sequencer steps |
| Conditional Logic Block | #84 | Medium | Make redstone decisions based on API responses |
| Batch/Multicast Sender | #85 | Medium | Send to multiple endpoints simultaneously |
| Audio Integration | #86 | Low | Play sounds from HTTP triggers |

### Future Vision (Phase 4+)

| Feature | Issue | Description |
|---------|-------|-------------|
| Mobile Companion App | #87 | Trigger endpoints from phone |
| Eira Hub Cloud Relay | #88 | Easy internet access without port forwarding |
| Integration Marketplace | #89 | Community-shared presets for common services |
| Visual Flow Builder | #90 | Drag-and-drop sequence designer |

### Additional Features Under Consideration

| Feature | Rationale |
|---------|-----------|
| **Redstone Detector Block** | Publish events when redstone changes (for Eira Core subscribers) |
| **HTTP Filter Block** | Validate/transform incoming requests before triggering |
| **Relay Controller Block** | Central hub GUI for managing multiple triggers |
| **Webhook History/Logs** | Per-block request history for debugging |
| **Response Routing** | Route different HTTP responses to different redstone outputs |
| **mDNS/Bonjour Discovery** | Auto-discover Eira Relay instances on local network |
| **Prometheus Metrics** | Expose metrics for monitoring dashboards |

---

## Tech Debt, Bugs, and Unfinished Work

### Known Issues

| Issue | Severity | Description |
|-------|----------|-------------|
| Javadoc warnings | Low | ~100 missing Javadoc comments on public APIs |
| Handler cleanup race | Medium | Block removal during HTTP request can cause orphan handlers |
| No request timeout config | Medium | HTTP client uses hardcoded 30s timeout |
| Scene Sequencer GUI overflow | Low | Long step lists can overflow the GUI panel |

### Technical Debt

| Area | Description | Effort |
|------|-------------|--------|
| **Forge parity** | Forge module missing: player detection, Scene Sequencer, new endpoints | High |
| **Fabric module** | Incomplete implementation, disabled indefinitely | High |
| **Test coverage** | No automated tests; manual testing only | High |
| **Error handling** | Some HTTP errors swallowed silently | Medium |
| **GUI code duplication** | Sender/Receiver screens share similar logic | Medium |
| **Magic numbers** | Hardcoded values (timeouts, limits) should be configurable | Low |
| **Unused Player import** | `PlayerDetector.java` imports unused `Player` class | Trivial |

### Incomplete Features

| Feature | Status | What's Missing |
|---------|--------|----------------|
| Redstone patterns (pulse, fade, SOS) | Planned | Not started |
| Multiple API keys | Planned | Not started |
| Per-block rate limiting | Issue #32 | Not started |
| Request validation/filtering | Issue #29 | Not started |

---

## Contributing

Contributions are welcome. Before starting significant work, open an issue to discuss the approach.

**What we're looking for:**
- Bug fixes with clear reproduction steps
- Features from the roadmap (check issues first)
- Documentation improvements
- Translations (see `lang/` folder structure)

**Code expectations:**
- Follow existing patterns in the codebase
- Test builds pass (`./gradlew :neoforge:build`)
- One feature per PR
- Update relevant documentation

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

## Governance

**Maintainer:** [Eira](https://www.eira.no) development team

**Decision process:**
- Issues discuss features before implementation
- PRs require review before merge
- Breaking changes require maintainer approval
- Roadmap priorities set by maintainers based on educational use cases

**Roadmap location:** This README (above) and [GitHub Issues](https://github.com/Narratimo/HappyHttpMod/issues)

## Licence

MIT License. See [LICENSE](LICENSE) for full text.

**Usage expectations:** This mod is designed for educational and creative purposes. We ask that you not use it for malicious purposes (DDoS, spam, harassment).

## Community and Support

- **Issues:** [GitHub Issues](https://github.com/Narratimo/HappyHttpMod/issues) for bugs and feature requests
- **Discord:** [Join our server](https://discord.gg/DVuQSV27pa) for questions and discussion
- **Response time:** We aim to respond within a week; faster for security issues

---

## Closing Note

Eira Relay exists because we wanted to build interactive experiences for young people that connect the digital and physical worlds. We believe the best way to learn technology is to build things that feel magical.

If you're building something creative with this mod, we'd love to hear about it.

*— The Eira Team*
