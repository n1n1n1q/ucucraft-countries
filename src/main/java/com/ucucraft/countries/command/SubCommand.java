package com.ucucraft.countries.command;

import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Player;

public interface SubCommand {

    String name();

    void execute(Player player, String[] args);

    default List<String> complete(Player player, String[] args) {
        return Collections.emptyList();
    }
}
