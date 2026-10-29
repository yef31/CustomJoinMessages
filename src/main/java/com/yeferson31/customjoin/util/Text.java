package com.yeferson31.customjoin.util;

import com.yeferson31.customjoin.hook.Prefixes;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Text {

    private static final Pattern HEX = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private Text() {
    }

    /** Translates & color codes and &#RRGGBB hex colors. */
    public static String color(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        Matcher matcher = HEX.matcher(input);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            String hex = net.md_5.bungee.api.ChatColor.of("#" + matcher.group(1)).toString();
            matcher.appendReplacement(out, Matcher.quoteReplacement(hex));
        }
        matcher.appendTail(out);
        return ChatColor.translateAlternateColorCodes('&', out.toString());
    }

    public static List<String> color(List<String> lines) {
        List<String> out = new ArrayList<>(lines.size());
        for (String line : lines) {
            out.add(color(line));
        }
        return out;
    }

    public static String withPlayer(String input, Player player) {
        if (input == null) {
            return "";
        }
        String out = input.replace("{player}", player.getName())
                .replace("{displayname}", player.getDisplayName());
        if (out.contains("{prefix}")) {
            out = out.replace("{prefix}", Prefixes.prefix(player));
        }
        if (out.contains("{suffix}")) {
            out = out.replace("{suffix}", Prefixes.suffix(player));
        }
        return Prefixes.placeholders(player, out);
    }
}
