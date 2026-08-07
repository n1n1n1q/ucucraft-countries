package com.ucucraft.countries.path;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import com.ucucraft.countries.claim.ClaimManager;
import com.ucucraft.countries.config.Messages;
import com.ucucraft.countries.manager.CountryManager;
import com.ucucraft.countries.model.Country;
import com.ucucraft.countries.path.buff.AbilityBuff;
import com.ucucraft.countries.path.buff.BuffContext;
import com.ucucraft.countries.path.buff.PathBuff;

public final class PathManager {

    public enum ChoiceResult { OK, DISABLED, UNKNOWN, LOCKED }

    public enum AbilityResult { OK, UNKNOWN, ON_COOLDOWN, UNAVAILABLE }

    private final PathRegistry registry;
    private final CountryManager countries;
    private final ClaimManager claims;
    private final Messages lang;

    public PathManager(PathRegistry registry, CountryManager countries, ClaimManager claims, Messages lang) {
        this.registry = registry;
        this.countries = countries;
        this.claims = claims;
        this.lang = lang;
    }

    public PathRegistry registry() {
        return registry;
    }

    public boolean enabled() {
        return registry.enabled() && !registry.isEmpty();
    }

    public Path pathOf(Country country) {
        return registry.of(country);
    }

    public ChoiceResult choose(Country country, String pathId) {
        if (!enabled()) {
            return ChoiceResult.DISABLED;
        }
        Path path = registry.byId(pathId);
        if (path == null) {
            return ChoiceResult.UNKNOWN;
        }
        if (country.hasPath() && !registry.allowChange()) {
            return ChoiceResult.LOCKED;
        }
        set(country, path.getId());
        return ChoiceResult.OK;
    }

    /** Sets the path without any rule check; used by the admin command. */
    public void set(Country country, String pathId) {
        country.setPathId(pathId);
        country.setPathSince(System.currentTimeMillis());
        country.getAbilityCooldowns().clear();
        countries.save();
    }

    public BuffContext context(Player player, Country country) {
        return new BuffContext(player, country, claims, registry.undergroundY());
    }

    /** Buffs of the given kind whose scope currently holds for that player. */
    public <T extends PathBuff> List<T> active(Player player, Country country, Class<T> type) {
        List<T> all = registry.buffs(country, type);
        if (all.isEmpty() || !enabled()) {
            return List.of();
        }
        BuffContext context = context(player, country);
        List<T> result = new ArrayList<>();
        for (T buff : all) {
            if (buff.scope().test(context)) {
                result.add(buff);
            }
        }
        return result;
    }

    public List<AbilityBuff> abilities(Country country) {
        return registry.buffs(country, AbilityBuff.class);
    }

    public AbilityBuff ability(Country country, String id) {
        for (AbilityBuff ability : abilities(country)) {
            if (ability.id().equalsIgnoreCase(id)) {
                return ability;
            }
        }
        return null;
    }

    /** Seconds left before the ability can be used again; 0 when it is ready. */
    public long cooldownLeft(Country country, AbilityBuff ability) {
        Long used = country.getAbilityCooldowns().get(ability.id());
        if (used == null) {
            return 0;
        }
        long left = ability.cooldownSeconds() - (System.currentTimeMillis() - used) / 1000L;
        return Math.max(0, left);
    }

    public AbilityResult use(Player player, Country country, String id) {
        AbilityBuff ability = ability(country, id);
        if (ability == null) {
            return AbilityResult.UNKNOWN;
        }
        if (cooldownLeft(country, ability) > 0) {
            return AbilityResult.ON_COOLDOWN;
        }
        if (!ability.scope().test(context(player, country))) {
            return AbilityResult.UNAVAILABLE;
        }
        List<Player> targets = targets(player, country, ability);
        for (Player target : targets) {
            for (AbilityBuff.TimedPotion potion : ability.effects()) {
                target.addPotionEffect(new PotionEffect(potion.type(), potion.seconds() * 20,
                        potion.amplifier(), registry.potionAmbient(), registry.potionParticles(),
                        registry.potionIcon()));
            }
            if (!target.equals(player)) {
                lang.send(target, "ability-received", "player", player.getName(),
                        "ability", ability.display());
            }
        }
        country.getAbilityCooldowns().put(ability.id(), System.currentTimeMillis());
        countries.save();
        lang.send(player, "ability-used", "ability", ability.display(),
                "targets", String.valueOf(targets.size()));
        if (ability.broadcast()) {
            lang.broadcast("ability-broadcast", "country", country.getName(),
                    "ability", ability.display());
        }
        return AbilityResult.OK;
    }

    private List<Player> targets(Player player, Country country, AbilityBuff ability) {
        return switch (ability.target()) {
            case SELF -> List.of(player);
            case COUNTRY -> online(country);
            case NEARBY -> {
                List<Player> nearby = new ArrayList<>();
                double radiusSquared = ability.radius() * ability.radius();
                for (Player member : online(country)) {
                    if (member.equals(player) || (member.getWorld().equals(player.getWorld())
                            && member.getLocation().distanceSquared(player.getLocation()) <= radiusSquared)) {
                        nearby.add(member);
                    }
                }
                yield nearby;
            }
        };
    }

    public List<Player> online(Country country) {
        List<Player> players = new ArrayList<>();
        for (UUID member : country.getMembers()) {
            Player player = Bukkit.getPlayer(member);
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }

    /** Tells the citizens which tier the era they just entered unlocked. */
    public void announceUnlock(Country country) {
        if (!enabled() || !registry.announceUnlock()) {
            return;
        }
        Path path = pathOf(country);
        List<PathTier> tiers = registry.tiersAt(country, country.getEraIndex());
        if (path == null || tiers.isEmpty()) {
            return;
        }
        for (Player member : online(country)) {
            for (PathTier tier : tiers) {
                lang.send(member, "path-buff-unlocked",
                        "path", path.getDisplay(), "tier", tier.getDisplay());
                for (String line : tier.getDescription()) {
                    lang.sendRaw(member, "path-tier-line", "text", line);
                }
            }
        }
    }
}
