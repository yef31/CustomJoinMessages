package com.yeferson31.customjoin.message;

import org.bukkit.Material;

import java.util.List;

/** One configurable join/leave pair, unlocked by its permission. */
public record JoinMessage(
        String id,
        String permission,
        String name,
        Material material,
        List<String> description,
        String join,
        String leave
) {
}
