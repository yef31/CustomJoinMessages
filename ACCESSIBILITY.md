# Accessibility

CustomJoinMessages is a Minecraft server plugin. It shows join/leave messages in chat, in an in-game menu (`/jm`) and, optionally, on Discord. Everyone on a server should be able to read those messages and use the menu, including players who are colorblind, have low vision, use the Minecraft narrator, or find small or low-contrast text hard to read. Server owners and contributors should be able to configure and improve the plugin without barriers too.

This document describes what the project aims for, what contributors should keep in mind, how to report a barrier, and what is currently known to be limited.

## Priorities

There is no formal accessibility target and the plugin has not been evaluated against any standard. We aim for the following:

- **Readable text.** Every message, menu name and menu description is plain text in `config.yml`. Server owners can change wording, colors and length without touching code.
- **Never rely on color alone.** Menu states are shown with words as well as color. For example, an equipped message shows "✔ Equipped" and an enchantment glow, not just a green tint.
- **Configurable colors.** All colors, including the default ones, can be replaced. Owners can choose high-contrast colors or remove gradients.
- **Command alternatives to the menu.** Everything the menu does can also be done with commands (`/jm clear`, and `/jm set` / `/jm reset` for staff), so players who can't use the inventory menu can still be helped.
- **Plain language.** Default messages are short and avoid jargon.

## Contributor expectations

When you change anything players see, such as messages, menu items or Discord output:

- Don't use color as the only way to show a state or meaning. Add a word or symbol.
- Keep new default text short and clear, and keep it in `config.yml` so it can be translated or reworded.
- Don't make the menu the only way to reach a feature. Add or keep a command alternative.
- Describe the change in your pull request, with a screenshot of the chat or menu if possible, and mention any accessibility impact you noticed.

The project has no automated accessibility checks. Reviews are manual.

## Reporting accessibility issues

Please [open an issue](https://github.com/yef31/CustomJoinMessages/issues/new) and add "Accessibility" to the title. If GitHub isn't accessible for you, ask the server owner you play with to pass the report on.

Useful details, if you have them:

- What you were trying to do, for example "pick a message in the menu" or "read the join message".
- What happened and what you expected.
- Server version (Spigot/Paper), plugin version and Minecraft client version.
- Any assistive technology or settings you use, such as the narrator, a screen reader, a high-contrast resource pack or the text-size setting.

Screenshots or recordings are welcome but optional. You never need to tell us about a disability.

### Severity

Maintainers set the severity during triage; reporters don't need to.

- **High:** a player can't complete a task at all, for example they can't equip a message or can't read their own join message.
- **Medium:** the task is possible but much harder, for example very low-contrast default text or a menu item with no readable description.
- **Low:** a minor inconvenience with an easy workaround.

### How we respond

This is a small project maintained in spare time, so these are best-effort goals rather than guarantees:

- We aim to acknowledge reports within 7 days.
- When there is a workaround, such as a config change or a command, we share it in the issue.
- Fixes ship in the next release when possible, and the issue links the release so you can check the fix.

## Ownership and maintenance

The maintainer ([@yef31](https://github.com/yef31)) is responsible for accessibility: triaging reports, reviewing user-facing changes and keeping this document current. It is reviewed with each release. If maintenance moves to someone else, this section will be updated with the new contact.

## Supported environments

- **Server:** Spigot or Paper 1.20 or newer, Java 17+.
- **Players:** Minecraft Java Edition clients.
- **Optional integrations:** LuckPerms, PlaceholderAPI, EssentialsX, DiscordSRV.
- **Not tested:** Bedrock players joining through Geyser, and the Minecraft narrator with the inventory menu.

## Known limitations

- **The menu needs a mouse or controller click.** It is a standard Minecraft inventory menu. Players who can't use it can ask staff to run `/jm set <player> <id>`, but there is no player command yet to equip a message by ID without the menu.
- **The narrator may not read menu items.** The Minecraft narrator reads chat but generally doesn't read item names and descriptions in inventories.
- **Some default colors are low contrast.** A few default greys (for example `#5C6370`) can be hard to read on the chat background. Owners can change them in `config.yml`.
- **Gradient titles** split words into many colored letters, which can be harder to read. They can be replaced with a single color.
- **Discord embeds** use a colored side bar to separate joins from leaves. The message text also says what happened, so the color isn't the only cue.

Contrast has not been measured systematically and the plugin has not been tested with assistive technology.

## Feedback and improvements

Ideas are welcome, through [issues](https://github.com/yef31/CustomJoinMessages/issues) or pull requests. Accessibility improvements include clearer default text, better default contrast or a `/jm equip <id>` command. Notes from real use, including what already works well, help us decide what to fix next.
