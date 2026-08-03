package com.ucucraft.countries.claim;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.BlockBreakEvent;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.api.ChunkPos;
import com.ucucraft.countries.model.Country;

public final class ClaimProtectionListener implements Listener {

    private static final String BYPASS_PERMISSION = "countries.admin.bypassclaim";

    private final Services services;
    private final Map<UUID, Long> lastMessage = new HashMap<>();

    public ClaimProtectionListener(Services services) {
        this.services = services;
    }

    /** Country owning the chunk, or null when the chunk is unowned or the player may act freely there. */
    private Country restrictedOwner(Player player, ChunkPos pos) {
        if (!services.config.claimProtectionEnabled() || player.hasPermission(BYPASS_PERMISSION)) {
            return null;
        }
        UUID ownerId = services.claims.ownerOf(pos);
        if (ownerId == null) {
            return null;
        }
        Country owner = services.countries.getById(ownerId);
        if (owner != null && owner.isMember(player.getUniqueId())) {
            return null;
        }
        return owner;
    }

    private boolean blocked(Player player, Block block) {
        Country owner = restrictedOwner(player, ChunkPos.of(block.getLocation()));
        if (owner == null) {
            return false;
        }
        notify(player, owner, "claim-protected");
        return true;
    }

    private void notify(Player player, Country owner, String key) {
        long now = System.currentTimeMillis();
        Long last = lastMessage.get(player.getUniqueId());
        if (last != null && now - last < 1500L) {
            return;
        }
        lastMessage.put(player.getUniqueId(), now);
        String name = owner != null ? owner.getName() : services.messages.text("claim-wilderness");
        services.messages.send(player, key, "country", name);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (!services.config.claimProtectionBuild()) {
            return;
        }
        if (blocked(event.getPlayer(), event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (!services.config.claimProtectionBuild()) {
            return;
        }
        if (blocked(event.getPlayer(), event.getBlock())) {
            event.setCancelled(true);
        }
    }

    private boolean isProtectedInteraction(Block block) {
        BlockState state = block.getState();
        if (state instanceof Container) {
            return true;
        }
        return Tag.DOORS.isTagged(block.getType())
                || Tag.TRAPDOORS.isTagged(block.getType())
                || Tag.FENCE_GATES.isTagged(block.getType())
                || Tag.BUTTONS.isTagged(block.getType())
                || Tag.PRESSURE_PLATES.isTagged(block.getType())
                || block.getType().name().equals("LEVER");
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!services.config.claimProtectionContainers()
                || event.getAction() != Action.RIGHT_CLICK_BLOCK
                || event.getClickedBlock() == null
                || !isProtectedInteraction(event.getClickedBlock())) {
            return;
        }
        if (blocked(event.getPlayer(), event.getClickedBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!services.config.claimProtectionBuckets()) {
            return;
        }
        if (blocked(event.getPlayer(), event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (!services.config.claimProtectionBuckets()) {
            return;
        }
        if (blocked(event.getPlayer(), event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (!services.config.claimProtectionBuild() || !(event.getRemover() instanceof Player player)) {
            return;
        }
        if (blocked(player, event.getEntity().getLocation().getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent event) {
        if (!services.config.claimProtectionExplosions()) {
            return;
        }
        event.blockList().removeIf(block -> services.claims.ownerOf(ChunkPos.of(block.getLocation())) != null);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!services.config.claimProtectionExplosions()) {
            return;
        }
        event.blockList().removeIf(block -> services.claims.ownerOf(ChunkPos.of(block.getLocation())) != null);
    }

    @EventHandler(ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent event) {
        if (!services.config.claimProtectionFire()) {
            return;
        }
        if (services.claims.ownerOf(ChunkPos.of(event.getBlock().getLocation())) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBurn(BlockBurnEvent event) {
        if (!services.config.claimProtectionFire()) {
            return;
        }
        if (services.claims.ownerOf(ChunkPos.of(event.getBlock().getLocation())) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpread(BlockSpreadEvent event) {
        if (!services.config.claimProtectionFire() || event.getSource().getType() != org.bukkit.Material.FIRE) {
            return;
        }
        if (services.claims.ownerOf(ChunkPos.of(event.getBlock().getLocation())) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (!services.config.claimProtectionMobGriefing() || event.getEntity() instanceof Player) {
            return;
        }
        if (services.claims.ownerOf(ChunkPos.of(event.getBlock().getLocation())) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();

        if (damager instanceof Player attacker && victim instanceof Player defender) {
            onPvp(event, attacker, defender);
            return;
        }
        if (services.config.claimProtectionBuild() && damager instanceof Player attacker
                && (victim instanceof Hanging || victim instanceof ArmorStand)) {
            if (blocked(attacker, victim.getLocation().getBlock())) {
                event.setCancelled(true);
            }
        }
    }

    private void onPvp(EntityDamageByEntityEvent event, Player attacker, Player defender) {
        if (!services.config.claimPvpEnabled()) {
            return;
        }
        ChunkPos pos = ChunkPos.of(defender.getLocation());
        UUID ownerId = services.claims.ownerOf(pos);
        if (ownerId == null || defender.hasPermission(BYPASS_PERMISSION) || attacker.hasPermission(BYPASS_PERMISSION)) {
            return;
        }
        Country owner = services.countries.getById(ownerId);
        Country attackerCountry = services.countries.getByPlayer(attacker.getUniqueId());

        if (owner != null && attackerCountry != null && owner.getId().equals(attackerCountry.getId())) {
            if (!services.config.claimPvpFriendlyFire()) {
                event.setCancelled(true);
                notify(attacker, owner, "claim-protected-pvp");
            }
            return;
        }

        String mode = services.config.claimPvpMode();
        boolean allowed = switch (mode) {
            case "always-allowed" -> true;
            case "always-blocked" -> false;
            default -> owner != null && attackerCountry != null && owner.isAtWar(attackerCountry.getId());
        };
        if (!allowed) {
            event.setCancelled(true);
            notify(attacker, owner, "claim-protected-pvp");
        }
    }
}
