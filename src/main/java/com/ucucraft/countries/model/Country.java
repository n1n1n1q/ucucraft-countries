package com.ucucraft.countries.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;

public final class Country {

    private String name;
    private UUID leader;
    private final Set<UUID> members = new LinkedHashSet<>();

    public Country(String name, UUID leader) {
        this.name = name;
        this.leader = leader;
        this.members.add(leader);
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
    }

    public String getLeaderName() {
        String name = Bukkit.getOfflinePlayer(leader).getName();
        return name != null ? name : leader.toString();
    }

    public List<String> getMemberNames() {
        List<String> names = new ArrayList<>();
        for (UUID uuid : members) {
            String name = Bukkit.getOfflinePlayer(uuid).getName();
            names.add(name != null ? name : uuid.toString());
        }
        return names;
    }
}
