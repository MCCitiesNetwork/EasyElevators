package com.minecraftcitiesnetwork.easyelevators.elevator;

import com.minecraftcitiesnetwork.easyelevators.Direction;
import com.minecraftcitiesnetwork.easyelevators.ElevatorConfig;
import com.minecraftcitiesnetwork.easyelevators.PlayerData;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class ElevatorTravel {

    private final ArrowParticles arrows = new ArrowParticles();
    private final Player player;
    private final Location origin;
    private final ElevatorColumn column;
    private final Direction direction;
    private final ElevatorCheck check;
    private final ElevatorConfig config;
    private final PlayerData data;

    public ElevatorTravel(
            Player player,
            Location origin,
            ElevatorColumn column,
            Direction direction,
            ElevatorCheck check,
            ElevatorConfig config,
            PlayerData data
    ) {
        this.player = player;
        this.origin = origin;
        this.column = column;
        this.direction = direction;
        this.check = check;
        this.config = config;
        this.data = data;
    }

    public void run() {
        int floorY = check.targetFloorY(direction);
        if (floorY == ElevatorCheck.NO_FLOOR) {
            return;
        }
        elevate(toFloor(floorY));
    }

    private Location toFloor(int floorBlockY) {
        World world = player.getWorld();
        return new Location(
                world,
                column.x() + 0.5,
                floorBlockY + 1.0,
                column.z() + 0.5,
                origin.getYaw(),
                origin.getPitch()
        );
    }

    private void elevate(Location destination) {
        boolean vanished = isVanished(player);

        if (!vanished && config.arrowsEnabled() && config.arrowsOnCurrentFloor()) {
            arrows.spawn(player, direction, config);
        }

        player.teleport(destination);

        int floorDelta = direction == Direction.UP ? 1 : -1;
        ElevatorBossBar bossBar = data.bossBar();
        bossBar.display(config, bossBar.currentFloor() + floorDelta, bossBar.totalFloors());
        data.setCurrentFloor(data.currentFloor() + floorDelta);

        if (!vanished) {
            (direction == Direction.UP ? config.soundUp() : config.soundDown()).playSound(player);
            if (config.arrowsEnabled() && config.arrowsOnDestinationFloor()) {
                arrows.spawn(player, direction, config);
            }
        }
    }

    private static boolean isVanished(Player player) {
        if (!player.hasMetadata("vanished")) {
            return false;
        }
        for (var value : player.getMetadata("vanished")) {
            if (value.asBoolean()) {
                return true;
            }
        }
        return false;
    }
}
