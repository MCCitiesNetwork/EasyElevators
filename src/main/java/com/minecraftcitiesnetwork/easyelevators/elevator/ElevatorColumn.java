package com.minecraftcitiesnetwork.easyelevators.elevator;

import com.minecraftcitiesnetwork.easyelevators.ElevatorConfig;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** A vertical stack of elevator blocks at a fixed X/Z. */
public final class ElevatorColumn {

    private final UUID worldId;
    private final int x;
    private final int y;
    private final int z;

    private ElevatorColumn(UUID worldId, int x, int y, int z) {
        this.worldId = worldId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public UUID worldId() {
        return worldId;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public boolean matches(World world, int blockX, int blockZ) {
        return world.getUID().equals(worldId) && x == blockX && z == blockZ;
    }

    public static ElevatorColumn at(Block surfaceBlock) {
        return new ElevatorColumn(
                surfaceBlock.getWorld().getUID(),
                surfaceBlock.getX(),
                surfaceBlock.getY(),
                surfaceBlock.getZ()
        );
    }

    public static ElevatorColumn find(Block reference, ElevatorConfig config) {
        if (reference == null || config == null) {
            return null;
        }
        Material elevator = config.elevatorMaterial();
        Block below = reference.getRelative(BlockFace.DOWN);
        if (below.getType() == elevator) {
            return at(below);
        }
        if (reference.getType() == elevator) {
            return at(reference);
        }
        return null;
    }

    public static int[] scanFloors(World world, int x, int z, ElevatorConfig config) {
        Material elevator = config.elevatorMaterial();
        Set<Material> headroom = config.ignoreMaterials();
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight();
        List<Integer> found = new ArrayList<>(16);
        for (int blockY = minY; blockY < maxY; blockY++) {
            if (world.getBlockAt(x, blockY, z).getType() != elevator) {
                continue;
            }
            if (hasHeadroom(world, x, blockY, z, headroom)) {
                found.add(blockY);
            }
        }
        int[] floors = new int[found.size()];
        for (int i = 0; i < found.size(); i++) {
            floors[i] = found.get(i);
        }
        return floors;
    }

    public static boolean isFloor(World world, int x, int blockY, int z, ElevatorConfig config) {
        return world.getBlockAt(x, blockY, z).getType() == config.elevatorMaterial();
    }

    public static boolean hasHeadroom(World world, int x, int blockY, int z, Set<Material> passable) {
        if (passable.isEmpty()) {
            return true;
        }
        return passable.contains(world.getBlockAt(x, blockY + 1, z).getType())
                && passable.contains(world.getBlockAt(x, blockY + 2, z).getType());
    }
}
