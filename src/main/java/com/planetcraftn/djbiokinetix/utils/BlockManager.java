// com/planetcraftn/djbiokinetix/utils/BlockManager.java
package com.planetcraftn.djbiokinetix.utils;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor de bloqueos en memoria.
 */
public class BlockManager {

    private BlockManager() {}

    private static final Map<UUID, Set<UUID>> blockMap = new ConcurrentHashMap<>();

    public static boolean block(UUID player, UUID target) {
        return blockMap.computeIfAbsent(player, k -> ConcurrentHashMap.newKeySet()).add(target);
    }

    public static boolean unblock(UUID player, UUID target) {
        Set<UUID> blocked = blockMap.get(player);
        return blocked != null && blocked.remove(target);
    }

    public static boolean isBlocked(UUID sender, UUID receiver) {
        Set<UUID> blockedByReceiver = blockMap.get(receiver);
        return blockedByReceiver != null && blockedByReceiver.contains(sender);
    }
}
