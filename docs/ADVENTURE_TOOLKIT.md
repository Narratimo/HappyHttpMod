# Eira Relay Adventure Toolkit

A guide for creating interactive mixed-reality adventures using Eira Relay's HTTP integration.

## Quick Start

1. Install Eira Relay on your Minecraft server
2. Configure the HTTP server (default: `127.0.0.1:8080`)
3. Use HTTP Receiver blocks to trigger in-game events from external sources
4. Use HTTP Sender blocks to notify external systems of in-game events
5. Use Scene Sequencer blocks to chain multiple HTTP calls for complex sequences

## Core Blocks

### HTTP Receiver
Listens for incoming HTTP requests and emits a redstone signal.

- **Endpoint**: Configure the URL path (e.g., `/door/open`)
- **Power Modes**: Switch (toggle) or Timer (pulse duration)
- **Security**: Optional secret token validation

**Example**: A receiver at `/checkpoint/1` triggers when players scan a QR code in the real world.

### HTTP Sender
Sends HTTP requests when powered by redstone.

- **Methods**: GET or POST
- **Parameters**: JSON body (POST) or query string (GET)
- **Authentication**: None, Bearer, Basic, or Custom Header

**Example**: A sender posts to Discord when a player completes a puzzle.

### Scene Sequencer
Chains multiple HTTP requests with delays and conditions.

- **Steps**: Up to 8 sequential HTTP calls
- **Delays**: Configurable tick delay before each step
- **Conditions**: Execute always, on success, or on failure
- **Loop**: Repeat sequence continuously
- **Stop Behavior**: Ignore, stop, or reset on redstone off

**Example**: A sequencer triggers lights, plays sound effects, and opens a door in sequence.

## Example Setups

### 1. Webhook Door
A hidden door that opens when an external webhook is received.

**Setup**:
1. Place an HTTP Receiver behind a piston door
2. Set endpoint to `/door/secret`
3. Connect redstone to pistons
4. Set power mode to Timer (60 ticks = 3 seconds)

**Trigger**:
```bash
curl -X POST http://localhost:8080/door/secret
```

### 2. QR Code Checkpoint
Scan a QR code in the real world to trigger in-game progress.

**Setup**:
1. Place an HTTP Receiver at the checkpoint location
2. Set endpoint to `/checkpoint/1`
3. Connect to fireworks/particle effects via redstone

**QR Code Content**: URL to your webhook endpoint

### 3. Alarm System
External motion sensor triggers in-game alarm with multiple effects.

**Setup using Scene Sequencer**:
1. Place a Scene Sequencer
2. Configure steps:
   - Step 1: POST to Discord webhook (delay: 0)
   - Step 2: POST to smart lights API (delay: 20 ticks)
   - Step 3: Internal webhook to open escape route (delay: 40 ticks)
3. Connect to external motion sensor via HTTP Receiver

### 4. Team Scoreboard
Track checkpoint completions on an external scoreboard.

**Setup**:
1. Place an HTTP Sender on a pressure plate
2. Configure URL to your scoreboard API
3. Set method to POST
4. Add parameters: `team`, `checkpoint`, `timestamp`

**Example Request Body**:
```json
{
  "team": "red",
  "checkpoint": "tower",
  "timestamp": "2024-01-15T14:30:00Z"
}
```

### 5. Interactive Story Sequence
Create a cinematic experience with timed events.

**Setup using Scene Sequencer**:
1. Step 1: Trigger narration audio (delay: 0)
2. Step 2: Open first door (delay: 100 ticks)
3. Step 3: Spawn particles/effects (delay: 200 ticks)
4. Step 4: Reveal treasure (delay: 300 ticks)
5. Enable loop for repeating attractions

## Configuration Guide

### Local Testing
```
Server IP: 127.0.0.1
Port: 8080
Test URL: http://localhost:8080/your-endpoint
```

Test with curl:
```bash
# Test receiver endpoint
curl -X POST http://localhost:8080/test

# Test with JSON body
curl -X POST http://localhost:8080/event \
  -H "Content-Type: application/json" \
  -d '{"action": "open"}'
```

### LAN Play
1. Change server binding to `0.0.0.0` or your LAN IP
2. Configure firewall to allow inbound connections on port 8080
3. Access from other devices: `http://YOUR_LAN_IP:8080/endpoint`

### Internet Access (Advanced)
For public internet access, use a reverse proxy:

**Cloudflare Tunnel**:
```bash
cloudflared tunnel --url http://localhost:8080
```

**Nginx Reverse Proxy**:
```nginx
location /minecraft/ {
    proxy_pass http://localhost:8080/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}
```

**Security Considerations**:
- Always enable secret token authentication for public endpoints
- Use HTTPS via reverse proxy
- Consider rate limiting
- Whitelist IP addresses if possible

## Integration Examples

### Discord Webhooks
Send in-game events to a Discord channel.

**HTTP Sender Configuration**:
```
URL: https://discord.com/api/webhooks/YOUR_WEBHOOK_ID/YOUR_TOKEN
Method: POST
Auth: None (webhook URL includes auth)
```

Use the Discord button in the GUI for easy setup, then customize the message:
```json
{
  "content": "A player reached the checkpoint!",
  "embeds": [{
    "title": "Checkpoint Reached",
    "color": 5814783
  }]
}
```

### Home Assistant
Trigger smart home automations from Minecraft.

**HTTP Sender Configuration**:
```
URL: http://homeassistant.local:8123/api/webhook/minecraft_event
Method: POST
Body: {"event": "door_opened", "location": "castle"}
```

**Trigger Minecraft from Home Assistant**:
```yaml
# configuration.yaml
rest_command:
  minecraft_trigger:
    url: "http://minecraft-server:8080/trigger"
    method: POST
```

### IFTTT Integration
Connect to thousands of services via IFTTT webhooks.

**HTTP Sender → IFTTT**:
```
URL: https://maker.ifttt.com/trigger/minecraft_event/with/key/YOUR_KEY
Method: POST
Body: {"value1": "checkpoint", "value2": "tower"}
```

### OBS Studio
Control OBS scenes from in-game events.

**HTTP Sender → OBS WebSocket**:
```
URL: http://localhost:4455/emit
Method: POST
Body: {"event": "SetCurrentScene", "scene": "Minecraft_Cam2"}
```

## Troubleshooting

### Receiver Not Responding
1. Check server console for HTTP requests
2. Verify endpoint URL matches exactly (case-sensitive)
3. Test with curl to confirm server is accessible
4. Check firewall settings

### Sender Requests Failing
1. Check server console for error messages
2. Verify URL is correct and accessible
3. Test endpoint manually with curl
4. Check authentication settings

### Scene Sequencer Issues
1. Ensure URLs are configured for each step
2. Check delay values (in ticks, 20 = 1 second)
3. Verify conditional steps have correct settings
4. Check stop behavior configuration

### Network Issues
1. Verify Minecraft server and HTTP server are on same network
2. Check port forwarding for remote access
3. Disable VPN if having connectivity issues
4. Check for conflicting applications on port 8080

## Best Practices

1. **Test Locally First**: Always test HTTP endpoints with curl before building redstone
2. **Use Meaningful Endpoints**: `/checkpoint/tower` is better than `/endpoint1`
3. **Add Delays**: Give players time to see effects with Scene Sequencer delays
4. **Secure Public Endpoints**: Enable token authentication for internet-facing receivers
5. **Log Events**: Send events to a logging service for debugging
6. **Plan for Failure**: Use conditional steps to handle HTTP failures gracefully

## Creative Ideas

- **Escape Room**: Timer-based puzzles with Scene Sequencer countdowns
- **Scavenger Hunt**: QR codes around a venue trigger in-game rewards
- **Live Event**: Audience votes via web app control in-game outcomes
- **Smart Home Game**: Real lights/sounds synchronize with Minecraft
- **Educational**: Real-world sensors (temperature, light) affect the game world
- **Twitch Integration**: Chat commands trigger in-game events via webhooks
