# Using JSON parameters with Happy HTTP blocks

This guide explains how the Happy HTTP sender and receiver blocks handle JSON payloads and shows how to connect them to Discord webhooks or bot commands.

## How the mod treats JSON

- **Sender block (POST):** When the sender block is set to `POST`, the parameter map is converted into a JSON body and sent with `Content-Type: application/json`. Each key/value pair you configure becomes a string property in the JSON object.
- **Sender block (GET):** When the sender block is set to `GET`, parameters are URL-encoded and appended to the query string instead of being sent as JSON.
- **Receiver block:** The receiver only accepts `POST` requests. It reads the request body as JSON and only keeps string properties. The request is considered a match when all configured key/value pairs exist in the JSON body with identical string values.

> **Important:** Non-string JSON values (numbers, booleans, arrays, objects) are ignored by the receiver's parameter matching. Stick to flat string properties when wiring external services to a receiver block.

## Sending a Discord message with a sender block

1. Create a Discord webhook URL in your channel settings.
2. Place an HTTP sender block and set the **URL** to the webhook URL.
3. Set **Method** to **POST** so the block sends JSON.
4. Add parameters that Discord expects. For a basic message:
   - `content`: The message text you want to post.
   - (Optional) `username`, `avatar_url`, etc., if you want to override defaults for that webhook.
5. Power the block with redstone. The block will emit a JSON body like:
   ```json
   {
     "content": "Hello from Happy HTTP!"
   }
   ```
   Discord treats that as the message payload for the webhook.

## Triggering redstone from a Discord bot command

You can have a Discord bot listen for commands and then call the receiver block's endpoint to flip redstone in Minecraft.

1. Configure the **HTTP receiver block**:
   - Choose an endpoint path (e.g., `/discord/trigger`).
   - Add the parameters your bot will send, such as `command` = `!door` and `user` = `Steve`.
2. In your bot code, send a `POST` request to the Happy HTTP server whenever the command is detected:
   ```bash
   curl -X POST "http://<happy-http-host>:<port>/discord/trigger" \
        -H "Content-Type: application/json" \
        -d '{"command":"!door","user":"Steve"}'
   ```
3. When the JSON body contains matching string parameters, the receiver block powers on (switch or timer mode depending on its settings), letting you start the redstone event.

## Tips for reliable integrations

- Use unique endpoint paths per automation to avoid overlapping receiver handlers.
- Keep all parameters as strings to ensure the receiver matches them.
- If you need to debug, temporarily remove parameters from the receiver block to confirm that requests reach Minecraft, then add them back one by one.
