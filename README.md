# CustomJoinMessages

Permission-based custom join/leave messages for Spigot/Paper, with a GUI so players can pick and switch their message at any time.

## Features

- **GUI selector** (`/jm`): players see every message they've unlocked, with a preview, and equip or unequip it with one click. The menu is paged and its layout is fully configurable.
- **Unlimited messages**: each one has its own join and leave text, icon, description and permission (`customjoin.message.<id>`).
- **Rank prefixes**: `{prefix}` / `{suffix}` come from LuckPerms automatically. `settings.prefix-format` can replace them with any text.
- **PlaceholderAPI**: any placeholder works inside messages.
- **DiscordSRV**: join/leave messages are posted to Discord as an embed with the player's head, or as plain text. DiscordSRV's own join/leave messages are turned off automatically, so there are no duplicates.
- **Vouchers**: `/jm voucher <id>` gives an item that unlocks and equips a message on right-click.
- **EssentialsX friendly**: the plugin runs after Essentials and warns in console if Essentials still has its own join/quit message.
- Hex colors (`&#RRGGBB`) and legacy `&` codes.

## Requirements

- Spigot or Paper 1.20+ and Java 17+
- Optional: LuckPerms, PlaceholderAPI, DiscordSRV, EssentialsX

## Installation

1. Drop the jar into `plugins/` and restart the server.
2. If you use EssentialsX, set `custom-join-message: "none"` and `custom-quit-message: "none"` in `plugins/Essentials/config.yml`.
3. Edit `plugins/CustomJoinMessages/config.yml`, then run `/jm reload`.

## Commands

| Command | Description | Permission |
| --- | --- | --- |
| `/jm` | Open the menu | `customjoin.use` |
| `/jm clear` | Remove your message | `customjoin.use` |
| `/jm list` | List message IDs | `customjoin.admin` |
| `/jm set <player> <id>` | Set a player's message | `customjoin.admin` |
| `/jm reset <player>` | Remove a player's message | `customjoin.admin` |
| `/jm voucher <id> [player] [amount]` | Give a voucher | `customjoin.admin` |
| `/jm reload` | Reload the config | `customjoin.admin` |

Aliases: `/joinmessages`, `/joinmsg`, `/joinmessage`.

## Placeholders

`{player}`, `{displayname}`, `{prefix}`, `{suffix}`, plus any PlaceholderAPI placeholder.

## Building

```
mvn package
```

The jar is written to `target/`.
