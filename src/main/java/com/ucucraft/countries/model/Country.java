package com.ucucraft.countries.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;

public final class Country {

    private final UUID id;
    private String name;
    private UUID leader;
    private final Set<UUID> members = new LinkedHashSet<>();
    private final Set<UUID> trusted = new LinkedHashSet<>();
    private final Set<UUID> allies = new LinkedHashSet<>();
    private final Set<UUID> wars = new LinkedHashSet<>();
    private final Set<String> completedCriteria = new LinkedHashSet<>();
    private final Map<String, Long> abilityCooldowns = new LinkedHashMap<>();
    private int eraIndex;
    private long eraSince = System.currentTimeMillis();
    private String pathId;
    private long pathSince;

    public Country(UUID id, String name, UUID leader) {
        this.id = id;
        this.name = name;
        this.leader = leader;
        this.members.add(leader);
    }

    public Country(String name, UUID leader) {
        this(UUID.randomUUID(), name, leader);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String key() {
        return name.toLowerCase();
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    public boolean isLeader(UUID uuid) {
        return leader.equals(uuid);
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }

    public void addMember(UUID uuid) {
        members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
        trusted.remove(uuid);
    }

    public Set<UUID> getTrusted() {
        return trusted;
    }

    public boolean canWithdraw(UUID uuid) {
        return isLeader(uuid) || trusted.contains(uuid);
    }

    public Set<UUID> getAllies() {
        return allies;
    }

    public boolean isAllied(UUID countryId) {
        return allies.contains(countryId);
    }

    public Set<UUID> getWars() {
        return wars;
    }

    public boolean isAtWar(UUID countryId) {
        return wars.contains(countryId);
    }

    public Set<String> getCompletedCriteria() {
        return completedCriteria;
    }

    public int getEraIndex() {
        return eraIndex;
    }

    public void setEraIndex(int eraIndex) {
        this.eraIndex = eraIndex;
    }

    public long getEraSince() {
        return eraSince;
    }

    public void setEraSince(long eraSince) {
        this.eraSince = eraSince;
    }

    /** Id of the chosen path from paths.yml, or null when none was picked. */
    public String getPathId() {
        return pathId;
    }

    public void setPathId(String pathId) {
        this.pathId = pathId;
    }

    public boolean hasPath() {
        return pathId != null && !pathId.isEmpty();
    }

    public long getPathSince() {
        return pathSince;
    }

    public void setPathSince(long pathSince) {
        this.pathSince = pathSince;
    }

    /** Ability id to the epoch millis it was last used. */
    public Map<String, Long> getAbilityCooldowns() {
        return abilityCooldowns;
    }

    public String getLeaderName() {
        String leaderName = Bukkit.getOfflinePlayer(leader).getName();
        return leaderName != null ? leaderName : leader.toString();
    }

    public List<String> getMemberNames() {
        List<String> names = new ArrayList<>();
        for (UUID uuid : members) {
            String memberName = Bukkit.getOfflinePlayer(uuid).getName();
            names.add(memberName != null ? memberName : uuid.toString());
        }
        return names;
    }
}
