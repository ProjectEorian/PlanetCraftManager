// com/planetcraftn/djbiokinetix/utils/FriendManager.java
package com.planetcraftn.djbiokinetix.utils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor de amigos y solicitudes de amistad.
 */
public class FriendManager {

    private FriendManager() {}

    // Mapa de amigos: jugador -> conjunto de UUIDs de amigos
    private static final Map<UUID, Set<UUID>> friendsMap = new ConcurrentHashMap<>();
    // Mapa de solicitudes: receptor -> conjunto de UUIDs de emisores
    private static final Map<UUID, Set<UUID>> requestMap = new ConcurrentHashMap<>();

    public static boolean sendRequest(UUID from, UUID to) {
        if (from.equals(to) || isFriends(from, to)) return false;
        Set<UUID> pending = requestMap.computeIfAbsent(to, k -> ConcurrentHashMap.newKeySet());
        if (pending.contains(from)) return false;
        pending.add(from);
        return true;
    }

    public static boolean acceptRequest(UUID to, UUID from) {
        Set<UUID> pending = requestMap.getOrDefault(to, Collections.emptySet());
        if (!pending.remove(from)) return false;
        friendsMap.computeIfAbsent(to, k -> ConcurrentHashMap.newKeySet()).add(from);
        friendsMap.computeIfAbsent(from, k -> ConcurrentHashMap.newKeySet()).add(to);
        return true;
    }

    public static boolean declineRequest(UUID to, UUID from) {
        Set<UUID> pending = requestMap.get(to);
        return pending != null && pending.remove(from);
    }

    public static boolean removeFriend(UUID a, UUID b) {
        boolean removed = false;
        Set<UUID> aFriends = friendsMap.get(a);
        if (aFriends != null) removed |= aFriends.remove(b);
        Set<UUID> bFriends = friendsMap.get(b);
        if (bFriends != null) removed |= bFriends.remove(a);
        return removed;
    }

    public static boolean isFriends(UUID a, UUID b) {
        Set<UUID> aFriends = friendsMap.getOrDefault(a, Collections.emptySet());
        return aFriends.contains(b);
    }

    public static Set<UUID> getFriends(UUID uuid) {
        return Collections.unmodifiableSet(friendsMap.getOrDefault(uuid, Collections.emptySet()));
    }

    public static Set<UUID> getRequests(UUID uuid) {
        return Collections.unmodifiableSet(requestMap.getOrDefault(uuid, Collections.emptySet()));
    }
}
