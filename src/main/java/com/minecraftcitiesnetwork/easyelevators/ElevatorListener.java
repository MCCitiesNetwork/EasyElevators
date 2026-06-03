package com.minecraftcitiesnetwork.easyelevators;

import com.minecraftcitiesnetwork.easyelevators.elevator.ElevatorCheck;
import com.minecraftcitiesnetwork.easyelevators.elevator.ElevatorColumn;
import com.minecraftcitiesnetwork.easyelevators.elevator.ElevatorTravel;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public final class ElevatorListener implements Listener {

    private static final double JUMP_MIN_RISE = 0.08;
    private static final double JUMP_MAX_RISE = 0.8;

    private final EasyElevatorsPlugin plugin;

    public ElevatorListener(EasyElevatorsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerData.remove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.hasChangedPosition()) {
            return;
        }

        Player player = event.getPlayer();
        ElevatorConfig config = plugin.elevatorConfig();

        if (player.getGameMode() == GameMode.SPECTATOR) {
            hideBossBar(player);
            return;
        }

        if (!config.isWorldAllowed(player.getWorld().getName())) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) {
            return;
        }

        boolean horizontalMove = from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ();
        boolean rising = to.getY() > from.getY();

        if (!rising && !horizontalMove && !config.bossBarEnabled()) {
            return;
        }

        ElevatorColumn column = ElevatorColumn.find(to.getBlock(), config);
        if (column == null && horizontalMove) {
            column = ElevatorColumn.find(from.getBlock(), config);
        }
        if (column == null) {
            if (horizontalMove || config.bossBarEnabled()) {
                hideBossBar(player);
            }
            return;
        }

        PlayerData data = PlayerData.get(player, true);
        int[] floors = data.floorsFor(column, to.getWorld(), config);
        ElevatorCheck check = new ElevatorCheck(column, floors);

        if (config.bossBarEnabled() && (horizontalMove || rising)) {
            updateBossBar(data, check, config, to.getBlockY());
        }

        if (rising && isJumpTrigger(from, to)) {
            tryElevate(player, data, column, check, config, Direction.UP, from);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        PlayerData data = PlayerData.get(event.getPlayer());
        if (data != null) {
            data.bossBar().hide();
            data.clearFloorCache();
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking() || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        ElevatorConfig config = plugin.elevatorConfig();
        if (!config.isWorldAllowed(player.getWorld().getName())) {
            return;
        }

        ElevatorColumn column = ElevatorColumn.find(player.getLocation().getBlock(), config);
        if (column == null) {
            return;
        }

        PlayerData data = PlayerData.get(player, true);
        int[] floors = data.floorsFor(column, player.getWorld(), config);
        tryElevate(player, data, column, new ElevatorCheck(column, floors), config, Direction.DOWN, player.getLocation());
    }

    private void updateBossBar(PlayerData data, ElevatorCheck check, ElevatorConfig config, int feetBlockY) {
        int floors = check.floorCount();
        if (floors > 1) {
            int current = check.currentFloorNumber(feetBlockY);
            data.bossBar().display(config, current, floors);
            data.setCurrentFloor(current);
            data.setTotalFloors(floors);
        } else {
            data.bossBar().hide();
        }
    }

    private void hideBossBar(Player player) {
        PlayerData data = PlayerData.get(player);
        if (data != null) {
            data.bossBar().hide();
        }
    }

    private static boolean isJumpTrigger(Location from, Location to) {
        double rise = to.getY() - from.getY();
        return rise >= JUMP_MIN_RISE
                && rise < JUMP_MAX_RISE
                && from.getY() <= from.getBlockY() + 0.25;
    }

    private void tryElevate(
            Player player,
            PlayerData data,
            ElevatorColumn column,
            ElevatorCheck check,
            ElevatorConfig config,
            Direction direction,
            Location origin
    ) {
        if (!check.hasTarget(direction)) {
            return;
        }
        new ElevatorTravel(player, origin, column, direction, check, config, data).run();
    }
}
