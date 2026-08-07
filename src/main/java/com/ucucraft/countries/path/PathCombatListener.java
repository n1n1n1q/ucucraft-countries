package com.ucucraft.countries.path;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import com.ucucraft.countries.manager.CountryManager;
import com.ucucraft.countries.model.Country;
import com.ucucraft.countries.path.buff.DamageBuff;

/** Applies the damage multipliers of a path. */
public final class PathCombatListener implements Listener {

    private final PathManager paths;
    private final CountryManager countries;

    public PathCombatListener(PathManager paths, CountryManager countries) {
        this.paths = paths;
        this.countries = countries;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamageTaken(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player victim) {
            apply(event, victim, DamageBuff.Direction.TAKEN);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamageDealt(EntityDamageByEntityEvent event) {
        Player attacker = attacker(event.getDamager());
        if (attacker != null) {
            apply(event, attacker, DamageBuff.Direction.DEALT);
        }
    }

    private Player attacker(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            return shooter;
        }
        return null;
    }

    private void apply(EntityDamageEvent event, Player player, DamageBuff.Direction direction) {
        Country country = countries.getByPlayer(player.getUniqueId());
        if (country == null || !country.hasPath()) {
            return;
        }
        double multiplier = 1.0;
        for (DamageBuff buff : paths.active(player, country, DamageBuff.class)) {
            if (buff.direction() == direction && buff.matches(event.getCause())) {
                multiplier *= buff.multiplier();
            }
        }
        if (multiplier != 1.0) {
            event.setDamage(event.getDamage() * multiplier);
        }
    }
}
