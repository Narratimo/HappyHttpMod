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
        "reaction_success": "💡",
        "reaction_fail": "❌",
        "message_success": "Lights toggled!",
        "message_fail": "Failed to toggle lights",
        "description": "Toggle lights"
    },
    "!door": {
        "endpoint": "/door",
        "reaction_success": "🚪",
        "reaction_fail": "❌",
        "message_success": "Door activated!",
        "message_fail": "Failed to activate door",
        "description": "Open/close door"
    },
    "!alarm": {
        "endpoint": "/alarm",
        "reaction_success": "🚨",
        "reaction_fail": "❌",
        "message_success": "ALARM TRIGGERED!",
        "message_fail": "Alarm system offline",
        "description": "Trigger alarm"
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
            # Success reactions and messages
            if config["reaction_success"]:
                await message.add_reaction(config["reaction_success"])
            if config["message_success"]:
                await message.channel.send(config["message_success"])
            print(f"[{trigger}] Triggered {config['endpoint']}! Response: {response.text}")
        else:
            # Error reactions and messages
            if config["reaction_fail"]:
                await message.add_reaction(config["reaction_fail"])
            if config["message_fail"]:
                await message.channel.send(config["message_fail"])
            print(f"[{trigger}] Error: {response.status_code} - {response.text}")

    except requests.exceptions.ConnectionError:
        if config["reaction_fail"]:
            await message.add_reaction(config["reaction_fail"])
        if config["message_fail"]:
            await message.channel.send(config["message_fail"])
        print(f"[{trigger}] Error: Could not connect to Minecraft server")
    except Exception as e:
        if config["reaction_fail"]:
            await message.add_reaction(config["reaction_fail"])
        await message.channel.send(f"Error: {e}")
        print(f"[{trigger}] Error: {e}")

# Run the bot
client.run(BOT_TOKEN)
