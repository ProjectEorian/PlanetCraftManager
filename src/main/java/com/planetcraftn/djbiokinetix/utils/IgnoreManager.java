// com/planetcraftn/djbiokinetix/utils/IgnoreManager.java
package com.planetcraftn.djbiokinetix.utils;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor de ignorados de mensajes privados.
 */
public class IgnoreManager {

    private IgnoreManager() {}

    private static final Map<UUID, Set<UUID>> ignoreMap = new ConcurrentHashMap<>();

    public static boolean ignore(UUID player, UUID target) {
        return ignoreMap.computeIfAbsent(player, k -> ConcurrentHashMap.newKeySet()).add(target);
    }

    public static boolean unignore(UUID player, UUID target) {
        Set<UUID> ignored = ignoreMap.get(player);
        return ignored != null && ignored.remove(target);
    }

    public static boolean isIgnoring(UUID sender, UUID receiver) {
        Set<UUID> ignoredByReceiver = ignoreMap.get(receiver);
        return ignoredByReceiver != null && ignoredByReceiver.contains(sender);
    }
}
