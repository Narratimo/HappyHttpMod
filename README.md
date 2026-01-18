# Eira Relay - Minecraft HTTP Automation Mod

Eira Relay enables you to create adventures that bridge the physical world and the Minecraft world. Each world can trigger events in the other. **The project is maintained by [Eira](https://www.eira.no)**, a non-commercial organization focused on teaching kids and teenagers to code and learn technology.

This Minecraft mod introduces two custom blocks that interact with webhooks and HTTP requests, enabling powerful integrations and automations both within and outside the Minecraft world.

![Screenshot 2024-06-12 155630](https://github.com/clapters/HappyHttpMod/assets/128842272/59fcf0e0-b03a-4e82-ba70-7696e9f79b77)

## Table of Contents

- [Installation](#installation)
- [Usage](#usage)
- [Features](#features)
- [Configuration](#configuration)
- [Contributing](#contributing)
- [License](#license)


## Introduction

**Eira Relay** allows you to connect your Minecraft world to your external world using HTTP requests. Can for example be used with home automation. Let's say you want a secret door in Minecraft to open when a QR code is scanned, or when a motion sensor is triggered. Or you want to send an SMS when you enter a location in Minecraft, or trigger a redstone circuit. Only your imagination stops you from finding fun ways of using Eira Relay.

### Usage


- **HTTP Receiver Block**:
  - **Starting Automations in Minecraft from Outside Triggers**: Use the HTTP Receiver Block to initiate Minecraft automations based on external events. For example, trigger in-game events from a smart home system when motion is detected, or start a Minecraft mechanism when a specific condition is met on an external server or service.

  ![reciever](https://github.com/clapters/HappyHttpMod/assets/128842272/9c3c15d5-357c-4c22-b073-bd78d8bf8872)


- **HTTP Sender Block**:
  - **Trigger Automation on APIs, Home Automation Systems, etc.**: Use the HTTP Sender Block to send HTTP requests to external APIs or services. For example, trigger actions on a home automation system, like turning on lights or unlocking doors, when a redstone signal is received, or send notifications or update external systems based on in-game events.

  ![sender](https://github.com/clapters/HappyHttpMod/assets/128842272/611827f1-b15a-46ef-b44b-bb3de7673dae)

## Features

- **HTTP Receiver Block**: Sends a redstone signal when a webhook with the correct parameters is accessed. Ideal for starting automations in Minecraft from outside triggers.
- **HTTP Sender Block**: Sends an HTTP request to a specified URL with parameters when it receives a redstone signal. Perfect for triggering automation on APIs, home automation systems, and more.
- **Cross-Platform**: Integrated webhook server. Works on both Windows and Linux. Binds to localhost by default for security.

## Installation

### Requirements

#### Supported Platforms

- **NeoForge 1.21.1** (Active)

### Steps

1. **Download the Mod**:
   - Download the latest release of the mod from the [Releases](https://github.com/Narratimo/HappyHttpMod/releases) page.

2. **Install NeoForge:**
   Download and install NeoForge for Minecraft 1.21.1 from [NeoForge's official site](https://neoforged.net/).

3. **Add the Mod to Minecraft:**
   - Navigate to your Minecraft installation folder.
   - Open the `mods` folder (create it if it doesn't exist).
   - Place the downloaded mod `.jar` file into the `mods` folder.

4. **Launch Minecraft:**
   - Open the Minecraft Launcher.
   - Select the NeoForge profile.
   - Start the game.

## Usage

1. **Configure the HTTP Server**:
   - After the first run, a configuration file will be generated in the Minecraft configuration directory (usually `.minecraft/config/`).
   - The server binds to localhost (127.0.0.1) by default for security.
   - Default port: 8080

2. **Place and Configure Blocks**:
   - **HTTP Receiver Block**:
     - Place the HTTP Receiver Block in your Minecraft world.
     - Right-click the block (creative mode) to open its configuration interface.
     - Set up the endpoint URL (e.g., `/secret/door`).
     - When an HTTP POST is received at `http://localhost:8080/secret/door`, the block emits a redstone signal.

   - **HTTP Sender Block**:
     - Place the HTTP Sender Block in your Minecraft world.
     - Right-click the block (creative mode) to open its configuration interface.
     - Set the target URL endpoint.
     - Select HTTP method (GET or POST).
     - Configure parameters as key-value pairs.
     - When the block receives a redstone signal, it sends the HTTP request.

3. **Set Up Redstone Circuits**:
   - Connect the HTTP Receiver Block to redstone dust and a redstone lamp or any other redstone mechanism.
   - Ensure the HTTP Sender Block is connected to a redstone input source (like a button or lever).

## Configuration

The mod configuration is stored in the Minecraft config directory. The HTTP server binds to localhost (127.0.0.1) by default for security.

**Port Forwarding (for external access)**:
- To allow webhooks from outside your local network, you need to set up port forwarding on your router.
- Configure your router to forward the webhook port (default: 8080) to your Minecraft server IP.

## Contributing

Guidelines for contributing to the project:

- Fork the repository.
- Create your feature branch (`git checkout -b feature/AmazingFeature`).
- Commit your changes (`git commit -m 'Add some AmazingFeature'`).
- Push to the branch (`git push origin feature/AmazingFeature`).
- Open a pull request.

[Join our Discord server](https://discord.gg/DVuQSV27pa)

## Support

If you encounter any issues or have questions, please open an issue on the [GitHub Issues](https://github.com/Narratimo/HappyHttpMod/issues) page.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

Enjoy automating your Minecraft world with Eira Relay!
