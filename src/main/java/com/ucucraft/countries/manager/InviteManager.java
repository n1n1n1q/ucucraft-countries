package com.ucucraft.countries.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.ucucraft.countries.config.PluginConfig;

public final class InviteManager {

    public static final class Invite {
        private final UUID inviter;
        private final String inviterName;
        private final String countryKey;
        private final String countryName;
        private final long expiresAt;

        Invite(UUID inviter, String inviterName, String countryKey, String countryName, long expiresAt) {
            this.inviter = inviter;
            this.inviterName = inviterName;
            this.countryKey = countryKey;
            this.countryName = countryName;
            this.expiresAt = expiresAt;
        }

        public UUID getInviter() {
            return inviter;
        }

        public String getInviterName() {
            return inviterName;
        }

        public String getCountryKey() {
            return countryKey;
        }

        public String getCountryName() {
            return countryName;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    private final PluginConfig config;
    // invitee -> (inviter name lowercased -> invite)
    private final Map<UUID, Map<String, Invite>> invites = new HashMap<>();

    public InviteManager(PluginConfig config) {
        this.config = config;
    }

    public boolean hasInvite(UUID invitee, String inviterName) {
        Map<String, Invite> byInviter = invites.get(invitee);
        if (byInviter == null) {
            return false;
        }
        Invite invite = byInviter.get(inviterName.toLowerCase());
        return invite != null && !invite.isExpired();
    }

    public void invite(UUID invitee, UUID inviter, String inviterName, String countryKey, String countryName) {
        long expiresAt = System.currentTimeMillis() + config.inviteDurationSeconds() * 1000L;
        invites.computeIfAbsent(invitee, k -> new HashMap<>())
                .put(inviterName.toLowerCase(), new Invite(inviter, inviterName, countryKey, countryName, expiresAt));
    }

    /** Removes and returns the matching invite (even if expired), or null if none exists. */
    public Invite take(UUID invitee, String inviterName) {
        Map<String, Invite> byInviter = invites.get(invitee);
        if (byInviter == null) {
            return null;
        }
        Invite invite = byInviter.remove(inviterName.toLowerCase());
        if (byInviter.isEmpty()) {
            invites.remove(invitee);
        }
        return invite;
    }

    public List<String> pendingInviterNames(UUID invitee) {
        List<String> names = new ArrayList<>();
        Map<String, Invite> byInviter = invites.get(invitee);
        if (byInviter != null) {
            for (Invite invite : byInviter.values()) {
                if (!invite.isExpired()) {
                    names.add(invite.getInviterName());
                }
            }
        }
        return names;
    }

    public void sweep() {
        Iterator<Map<String, Invite>> outer = invites.values().iterator();
        while (outer.hasNext()) {
            Map<String, Invite> byInviter = outer.next();
            byInviter.values().removeIf(Invite::isExpired);
            if (byInviter.isEmpty()) {
                outer.remove();
            }
        }
    }

    public void clear() {
        invites.clear();
    }
}
