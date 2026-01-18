import discord
import requests
import os
from dotenv import load_dotenv

# Load environment variables from .env file
load_dotenv()

# === CONFIGURATION ===
BOT_TOKEN = os.getenv("DISCORD_BOT_TOKEN")
MINECRAFT_BASE_URL = "http://localhost:8080"  # Base URL for Minecraft HTTP Receiver

# Define your commands here!
# Each command maps to a Minecraft endpoint with custom reactions and messages
#
# For toggles: use reaction_on/reaction_off and message_on/message_off
# Minecraft should return JSON with {"state": "on"} or {"state": "off"}
#
# For non-toggles: use reaction_success and message_success
COMMANDS = {
    "!eira": {
        "endpoint": "/discord",           # Minecraft endpoint to call
        "reaction_success": "✅",          # Reaction on success (or None)
        "reaction_fail": "❌",             # Reaction on failure (or None)
        "message_success": None,           # Message to send on success (or None)
        "message_fail": "Could not reach Minecraft server!",  # Message on failure
        "description": "General trigger"
    },
    "!lights": {
        "endpoint": "/lights",
        "is_toggle": True,                 # Enable on/off mode
        "reaction_on": "💡",               # Reaction when turned ON
        "reaction_off": "🌑",              # Reaction when turned OFF
        "reaction_fail": "❌",
        "message_on": "Lights ON!",        # Message when turned ON
        "message_off": "Lights OFF!",      # Message when turned OFF
        "message_fail": "Failed to toggle lights",
        "description": "Toggle lights"
    },
    "!door": {
        "endpoint": "/door",
        "is_toggle": True,
        "reaction_on": "🚪",               # Door open
        "reaction_off": "🔒",              # Door closed/locked
        "reaction_fail": "❌",
        "message_on": "Door opened!",
        "message_off": "Door closed!",
        "message_fail": "Failed to activate door",
        "description": "Open/close door"
    },
    "!alarm": {
        "endpoint": "/alarm",
        "is_toggle": True,
        "reaction_on": "🚨",               # Alarm active
        "reaction_off": "🔕",              # Alarm off
        "reaction_fail": "❌",
        "message_on": "ALARM ACTIVATED!",
        "message_off": "Alarm deactivated",
        "message_fail": "Alarm system offline",
        "description": "Toggle alarm"
    },
}

# === BOT SETUP ===
intents = discord.Intents.default()
intents.message_content = True
client = discord.Client(intents=intents)

@client.event
async def on_ready():
    print(f"Bot is online as {client.user}")
    print(f"\nRegistered commands:")
    for cmd, config in COMMANDS.items():
        print(f"  {cmd} -> {config['endpoint']} ({config['description']})")
    print()

@client.event
async def on_message(message):
    # Ignore messages from the bot itself
    if message.author == client.user:
        return

    # Check each registered command
    for trigger, config in COMMANDS.items():
        if message.content.startswith(trigger):
            await handle_command(message, trigger, config)
            return  # Only handle first matching command

async def handle_toggle_response(message, trigger, config, response):
    """Handle response for toggle commands - picks on/off reaction based on state"""
    state = None

    # Try to parse JSON response from Minecraft
    try:
        data = response.json()
        state = data.get("state", "").lower()
    except:
        # If not JSON, check for "on" or "off" in response text
        text = response.text.lower()
        if "on" in text:
            state = "on"
        elif "off" in text:
            state = "off"

    # Pick reaction and message based on state
    if state == "on":
        if config.get("reaction_on"):
            await message.add_reaction(config["reaction_on"])
        if config.get("message_on"):
            await message.channel.send(config["message_on"])
    elif state == "off":
        if config.get("reaction_off"):
            await message.add_reaction(config["reaction_off"])
        if config.get("message_off"):
            await message.channel.send(config["message_off"])
    else:
        # Unknown state - use "on" as default (something happened)
        if config.get("reaction_on"):
            await message.add_reaction(config["reaction_on"])
        if config.get("message_on"):
            await message.channel.send(config["message_on"])

async def handle_command(message, trigger, config):
    """Handle a matched command"""
    url = MINECRAFT_BASE_URL + config["endpoint"]

    try:
        # Send POST request to Minecraft HTTP Receiver
        response = requests.post(
            url,
            json={
                "content": message.content,
                "author": str(message.author),
                "command": trigger
            },
            headers={"Content-Type": "application/json"},
            timeout=5
        )

        if response.status_code == 200:
            # Check if this is a toggle command
            if config.get("is_toggle"):
                await handle_toggle_response(message, trigger, config, response)
            else:
                # Non-toggle: use simple success reaction/message
                if config.get("reaction_success"):
                    await message.add_reaction(config["reaction_success"])
                if config.get("message_success"):
                    await message.channel.send(config["message_success"])
            print(f"[{trigger}] Triggered {config['endpoint']}! Response: {response.text}")
        else:
            # Error reactions and messages
            if config.get("reaction_fail"):
                await message.add_reaction(config["reaction_fail"])
            if config.get("message_fail"):
                await message.channel.send(config["message_fail"])
            print(f"[{trigger}] Error: {response.status_code} - {response.text}")

    except requests.exceptions.ConnectionError:
        if config.get("reaction_fail"):
            await message.add_reaction(config["reaction_fail"])
        if config.get("message_fail"):
            await message.channel.send(config["message_fail"])
        print(f"[{trigger}] Error: Could not connect to Minecraft server")
    except Exception as e:
        if config.get("reaction_fail"):
            await message.add_reaction(config["reaction_fail"])
        await message.channel.send(f"Error: {e}")
        print(f"[{trigger}] Error: {e}")

# Run the bot
client.run(BOT_TOKEN)
