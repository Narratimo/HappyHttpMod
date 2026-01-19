# Eira Relay - Minecraft HTTP Automation Mod

Eira Relay enables you to create adventures that bridge the physical world and the Minecraft world. Each world can trigger events in the other. **The project is maintained by [Eira](https://www.eira.no)**, a non-commercial organization focused on teaching kids and teenagers to code and learn technology.

This Minecraft mod introduces custom blocks and HTTP endpoints that interact with webhooks and HTTP requests, enabling powerful integrations and automations both within and outside the Minecraft world.

![Screenshot 2024-06-12 155630](https://github.com/clapters/HappyHttpMod/assets/128842272/59fcf0e0-b03a-4e82-ba70-7696e9f79b77)

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [HTTP Endpoints](#http-endpoints)
- [Configuration](#configuration)
- [Security](#security)
- [Contributing](#contributing)
- [License](#license)

## Introduction

**Eira Relay** allows you to connect your Minecraft world to your external world using HTTP requests. Use it with home automation, QR codes, sensors, Discord webhooks, and more. Let your imagination run wild!

**Example use cases:**
- Open a secret door in Minecraft when a QR code is scanned
- Trigger redstone circuits from motion sensors
- Send Discord notifications when players reach checkpoints
- Control smart home devices from in-game buttons
- Create interactive scavenger hunts with real-world triggers

## Features

### Blocks

| Block | Purpose |
|-------|---------|
| **HTTP Receiver** | Emits redstone signal when webhook is triggered |
| **HTTP Sender** | Sends HTTP requests when powered by redstone |
| **Scene Sequencer** | Chains multiple HTTP requests with delays and conditions |

### Core Capabilities

- **Power Modes**: Switch (toggle) or Timer (pulse for configurable duration)
- **Authentication**: Bearer tokens, Basic auth, Custom headers for outgoing requests
- **Secret Token Validation**: Protect receivers with token authentication
- **Visual Feedback**: Particles and glow effects when blocks are active
- **Discord Integration**: One-click preset for Discord webhook setup
- **Global Parameters**: Server-wide parameters applied to all requests
- **Test Button**: Verify sender configuration without redstone
- **Rate Limiting**: Configurable request limits per IP (disabled by default)
- **CORS Support**: Cross-origin requests for web integrations

### HTTP Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/trigger/{id}` | POST | Named triggers for QR codes, sensors |
| `/status` | GET | Health check with uptime and version |
| `/redstone` | POST | Emit redstone signal at coordinates |
| `/broadcast` | POST | Send chat, title, or actionbar messages |
| `/{custom}` | POST | Custom endpoints from Receiver blocks |

### Platform Support

| Platform | Minecraft | Status |
|----------|-----------|--------|
| **NeoForge** | 1.21.1 | Active |
| **Forge** | 1.20.2 | Active |

## Installation

### Requirements

- Minecraft with NeoForge 1.21.1 or Forge 1.20.2
- Java 21 (NeoForge) or Java 17 (Forge)

### Steps

1. **Download the Mod** from [Releases](https://github.com/Narratimo/HappyHttpMod/releases)

2. **Install Mod Loader**:
   - NeoForge: [neoforged.net](https://neoforged.net/)
   - Forge: [files.minecraftforge.net](https://files.minecraftforge.net/)

3. **Add to Minecraft**:
   - Place the `.jar` file in your `mods` folder
   - Launch with the appropriate mod loader profile

## Usage

### HTTP Receiver Block

Listens for incoming HTTP requests and emits a redstone signal.

![reciever](https://github.com/clapters/HappyHttpMod/assets/128842272/9c3c15d5-357c-4c22-b073-bd78d8bf8872)

**Setup:**
1. Place the block and right-click (creative mode) to configure
2. Set the endpoint URL (e.g., `/secret/door`)
3. Choose power mode:
   - **Switch**: Toggles on/off with each request
   - **Timer**: Pulses for a set duration (ticks or seconds)
4. (Optional) Set a secret token for authentication
5. Connect redstone to your mechanism

**Triggering:**
```bash
# Basic request
curl -X POST http://localhost:8080/secret/door

# With secret token (header)
curl -X POST http://localhost:8080/secret/door \
  -H "Authorization: Bearer your-secret-token"

# With secret token (query parameter)
curl -X POST "http://localhost:8080/secret/door?token=your-secret-token"
```

### HTTP Sender Block

Sends HTTP requests when powered by redstone.

![sender](https://github.com/clapters/HappyHttpMod/assets/128842272/611827f1-b15a-46ef-b44b-bb3de7673dae)

**Setup:**
1. Place the block and right-click (creative mode) to configure
2. Set the target URL
3. Choose HTTP method (GET or POST)
4. Configure parameters (key-value pairs)
5. (Optional) Set authentication:
   - **Bearer**: Token-based auth
   - **Basic**: Username:password
   - **Custom Header**: Any header name/value
6. Choose power mode (Switch or Timer cooldown)
7. Use the **Test** button to verify configuration
8. Connect redstone input (button, lever, pressure plate, etc.)

**Discord Integration:**
1. Click the **Discord** button for webhook preset
2. Paste your Discord webhook URL
3. Add a `content` parameter with your message
4. Power with redstone to send messages to Discord!

## HTTP Endpoints

### `/trigger/{id}` - Named Triggers

Trigger named events for QR codes, sensors, or external systems.

```bash
curl -X POST http://localhost:8080/trigger/checkpoint1
```

### `/status` - Health Check

Get server status, uptime, and version info.

```bash
curl http://localhost:8080/status
```

Response:
```json
{
  "status": "ok",
  "mod": "Eira Relay",
  "version": "1.1.0",
  "uptime": 3600
}
```

### `/redstone` - Remote Redstone

Emit a redstone signal at specific coordinates.

```bash
curl -X POST http://localhost:8080/redstone \
  -H "Content-Type: application/json" \
  -d '{"x": 100, "y": 64, "z": 200, "strength": 15, "duration": 20}'
```

### `/broadcast` - Messages

Send messages to all players.

```bash
# Chat message
curl -X POST http://localhost:8080/broadcast \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello everyone!", "type": "chat"}'

# Title
curl -X POST http://localhost:8080/broadcast \
  -d '{"message": "Welcome!", "type": "title"}'

# Actionbar
curl -X POST http://localhost:8080/broadcast \
  -d '{"message": "Press E to continue", "type": "actionbar"}'
```

## Configuration

Configuration file location: `.minecraft/config/eirarelay-common.toml`

```toml
[server]
# HTTP server port
port = 8080

# Bind address (localhost for security, 0.0.0.0 for all interfaces)
bind_address = "127.0.0.1"

# Rate limiting (requests per minute per IP, 0 = disabled)
rate_limit = 0

# CORS allowed origins (comma-separated, empty = disabled)
cors_origins = ""

[global_params]
# Parameters added to all outgoing requests
# param1 = "value1"
```

## Security

### Recommendations

1. **Keep localhost binding** unless you need external access
2. **Use secret tokens** on Receiver blocks exposed to the internet
3. **Enable rate limiting** for public-facing endpoints
4. **Use authentication** on Sender blocks for sensitive APIs

### Port Forwarding

To allow external access:
1. Configure your router to forward port 8080 (or your configured port)
2. Set `bind_address = "0.0.0.0"` in config
3. Use secret tokens on all Receiver blocks
4. Consider using a reverse proxy (nginx, Cloudflare) for additional security

## Adventure Toolkit

Ready to build interactive mixed-reality experiences? See the [Adventure Toolkit Guide](docs/ADVENTURE_TOOLKIT.md) for:
- Example setups (webhook doors, QR checkpoints, alarm systems)
- Integration guides (Discord, Home Assistant, IFTTT, OBS)
- Networking configuration (local, LAN, internet)
- Best practices and troubleshooting

## Contributing

We welcome contributions!

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

[Join our Discord server](https://discord.gg/DVuQSV27pa)

## Support

- **Issues**: [GitHub Issues](https://github.com/Narratimo/HappyHttpMod/issues)
- **Discord**: [Join our server](https://discord.gg/DVuQSV27pa)

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

Enjoy automating your Minecraft world with Eira Relay!
