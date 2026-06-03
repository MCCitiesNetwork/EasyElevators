package com.minecraftcitiesnetwork.easyelevators;

import com.minecraftcitiesnetwork.easyelevators.elevator.ElevatorBossBar;
import com.minecraftcitiesnetwork.easyelevators.elevator.ElevatorColumn;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerData {

    private static final Map<UUID, PlayerData> PLAYERS = new ConcurrentHashMap<>();

    private final ElevatorBossBar bossBar;
    private int currentFloor;
    private int totalFloors;

    private UUID cachedWorldId;
    private int cachedX = Integer.MIN_VALUE;
    private int cachedZ = Integer.MIN_VALUE;
    private int[] cachedFloors = new int[0];

    private PlayerData(Player player) {
        this.bossBar = new ElevatorBossBar(player);
    }

    public static PlayerData get(Player player, boolean createIfAbsent) {
        if (createIfAbsent) {
            return PLAYERS.computeIfAbsent(player.getUniqueId(), id -> new PlayerData(player));
        }
        return PLAYERS.get(player.getUniqueId());
    }

    public static PlayerData get(Player player) {
        return get(player, false);
    }

    public static void remove(Player player) {
        PlayerData data = PLAYERS.remove(player.getUniqueId());
        if (data != null) {
            data.bossBar.hide();
        }
    }

    public static void hideAllBossBars() {
        for (PlayerData data : PLAYERS.values()) {
            data.bossBar.hide();
        }
    }

    public ElevatorBossBar bossBar() {
        return bossBar;
    }

    public int currentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }

    public int totalFloors() {
        return totalFloors;
    }

    public void setTotalFloors(int totalFloors) {
        this.totalFloors = totalFloors;
    }

    public int[] floorsFor(ElevatorColumn column, World world, ElevatorConfig config) {
        if (column.matches(world, cachedX, cachedZ) && world.getUID().equals(cachedWorldId)) {
            return cachedFloors;
        }
        cachedWorldId = world.getUID();
        cachedX = column.x();
        cachedZ = column.z();
        cachedFloors = ElevatorColumn.scanFloors(world, column.x(), column.z(), config);
        return cachedFloors;
    }

    public void clearFloorCache() {
        cachedFloors = new int[0];
        cachedX = Integer.MIN_VALUE;
        cachedZ = Integer.MIN_VALUE;
        cachedWorldId = null;
    }
}
