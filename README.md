# Happy HTTP Minecraft Mod

The purpose of happy http is to be able to create adventures with a mix of the ordinary world and the minecraft world. Each world can trigger events in the other world. **The project is maintained by Eira**. A non-commercial organization with the purpose of teaching kids and teenagers to code and learn technolgy. 

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

**Happy http mod** allows you to connect your Minecraft world to your external world using HTTP requests. Can for example be used with home automation. Let's say you want a secret door in Minecraft to open when a a QR code is scanned, or when a motion sensor is triggered. Or you want to send an SMS when you enter a location in Minecraft, or trigger a redstone circuit. Only your imagination stops you from finding fun ways of using the happy http mod. 

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
- **Cross-Platform**: Integrated webhook server. Works on both Windows and Linux. It binds to the IP of the host machine on a configurable port.


## Installation

### Requirements

#### Client

- Minecraft with Forge for Minecraft version 1.20.2 
- (roadmap: NeoForge version 1.21.X)

#### Server

- Minecraft Forge Server for Minecraft version 1.20.2 
- (roadmap: Minecraft NeoForge Server for Minecraft version 1.21.X) 


### Steps

1. **Download the Mod**:
   - Download the latest release of the mod from the [Releases](https://github.com/clapters/happyhttpmod/releases) page.

2. **Install Minecraft Forge:**
   Download and install the appropriate version of Minecraft Forge from [Forge's official site](https://files.minecraftforge.net/).

3. **Add the Mod to Minecraft:**
   - Navigate to your Minecraft installation folder.
   - Open the `mods` folder (create it if it doesn't exist).
   - Place the downloaded mod `.jar` file into the `mods` folder.

4. **Launch Minecraft:**
   - Open the Minecraft Launcher.
   - Select the Forge profile.
   - Start the game.

## Usage

1. **Configure the Webhook Server**:
   - After the first run, a configuration file named `happyhttpmod-common.toml` will be generated in the Minecraft configuration directory (usually `.minecraft/config/`).
   - Edit the `happyhttpmod-common.toml` file to set up the webhook server's IP address and port:
       ``` 
      ["Http Server Settings"]
    	#Http Server Port
    	#Range: 0 ~ 999999
	    port = 8080
	    #Local adress of the machine. Leave empty to determine automatically (May be wrong if more than one Network Interface)
	    local_adress = "192.168.0.1"
     ```
   - To use the webhook, configure your local IP address in the settings. If your machine has multiple IP addresses, specify the desired IP address to bind the server to that specific IP.

2. **Place and Configure Blocks**:
   - **HTTP Receiver Block**: 
     - Place the HTTP Receiver Block in your Minecraft world.
     - Right-click the block to open its configuration interface.
     - Set up the endpoint. Local IP is for use on local network only. External IP is for outside the local network.
     - Power type: Select if the block should just switch between on/off, or if it should send a redstone signal for a defined number of seconds.
     - Optional: Set up the parameters that the block should listen for when a webhook request is received, such as expected parameter keys and values. To use parameters, use this format: URL/endpoint?parameter1=value1&parameter2=value2&parameter3=value3
     - Optional: Configure a redirect URL. For redirection to a different site, use full protocol (http:// or https://) and then the URL. 

   - **HTTP Sender Block**:
     - Place the HTTP Sender Block in your Minecraft world.
     - Right-click the block to open its configuration interface.
     - Set the target URL (endpoint)
     - Select if the request should be GET or POST
     - Optional: Set up the parameters that the block should submit, such as expected parameter keys and values. To use parameters, use this format: URL/endpoint?parameter1=value1&parameter2=value2&parameter3=value3
     
3. **Set Up Redstone Circuits**:
   - Connect the HTTP Receiver Block to redstone dust and a redstone lamp or any other redstone mechanism.
   - Ensure the HTTP Sender Block is connected to a redstone input source (like a button or lever).

## Configuration
**Configure the Webhook Server**:
   - After the first run, a configuration file named `happyhttpmod-common.toml` will be generated in the Minecraft configuration directory (usually `.minecraft/config/`).
   - Edit the `happyhttpmod-common.toml` file to set up the webhook server's IP address and port:
     ```      
      ["Http Server Settings"]
    	#Http Server Port
    	#Range: 0 ~ 999999
	    port = 8080
	    #Local adress of the machine. Leave empty to determine automatically (May be wrong if more than one Network Interface)
	    local_adress = "192.168.0.1"
     ```
   - To use the webhook, configure your local IP address in the settings. If your machine has multiple IP addresses, specify the desired IP address to bind the server to that specific IP.

**Configure your router**:

- To allow webhooks on your server to be allowed to pass through your router, you often need to set up port forwarding on your router. Configure that the webhook port you have defined in the config is forwarded to the Minecraft server IP. 

<img width="802" height="904" alt="image" src="https://github.com/user-attachments/assets/ca5b7191-daea-409f-8a75-02bc8720d860" />


## Contributing
Guidelines for contributing to the project.

- Fork the repository.
- Create your feature branch (git checkout -b feature/AmazingFeature).
- Commit your changes (git commit -m 'Add some AmazingFeature').
- Push to the branch (git push origin feature/AmazingFeature).
- Open a pull request.

[Join our Discord server](https://discord.gg/DVuQSV27pa)

## Support

If you encounter any issues or have questions, please open an issue on the [GitHub Issues](https://github.com/clapters/happyhttpmod/issues) page.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

Enjoy automating your Minecraft world with webhooks and HTTP requests!
