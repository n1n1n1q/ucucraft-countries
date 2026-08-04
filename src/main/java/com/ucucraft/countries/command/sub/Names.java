package com.ucucraft.countries.command.sub;

import org.bukkit.entity.Player;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.command.NameArgs;
import com.ucucraft.countries.manager.CountryManager.NameStatus;

final class Names {

    private Names() {
    }

    /** Joins args[from..) into a single whitespace-normalized name. */
    static String join(String[] args, int from) {
        return NameArgs.join(args, from);
    }

    /** Sends the matching error message and returns false when the name is not usable. */
    static boolean report(Services services, Player player, NameStatus status, String name) {
        switch (status) {
            case OK:
                return true;
            case TOO_SHORT:
                services.messages.send(player, "name-too-short",
                        "min", String.valueOf(services.config.nameMinLength()));
                return false;
            case TOO_LONG:
                services.messages.send(player, "name-too-long",
                        "max", String.valueOf(services.config.nameMaxLength()));
                return false;
            case INVALID:
                services.messages.send(player, "name-invalid");
                return false;
            case TAKEN:
                services.messages.send(player, "name-taken", "country", name);
                return false;
            default:
                return false;
        }
    }
}
