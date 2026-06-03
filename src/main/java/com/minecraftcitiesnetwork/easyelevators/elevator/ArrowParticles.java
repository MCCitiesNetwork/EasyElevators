package com.minecraftcitiesnetwork.easyelevators.elevator;

import com.minecraftcitiesnetwork.easyelevators.Direction;
import com.minecraftcitiesnetwork.easyelevators.ElevatorConfig;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

final class ArrowParticles {

    void spawn(Player player, Direction direction, ElevatorConfig config) {
        Location base = player.getLocation();
        double size = config.arrowSize();
        Particle.DustOptions dust = dustFor(
                direction == Direction.UP ? config.arrowColorUp() : config.arrowColorDown()
        );

        double centerX = base.getBlockX() + 0.5;
        double centerZ = base.getBlockZ() + 0.5;
        int steps = Math.max(1, (int) (size * 10));
        for (int step = 0; step < steps; step++) {
            double y = base.getY() + size - step / 10.0;
            player.spawnParticle(Particle.DUST, centerX, y, centerZ, 1, dust);
        }

        Location[] corners = cornersForYaw(base.getYaw(), base, size);
        if (corners == null) {
            return;
        }

        for (int side = 0; side < 2; side++) {
            Location start = direction == Direction.UP
                    ? new Location(base.getWorld(), centerX, base.getBlockY() + size, centerZ)
                    : new Location(base.getWorld(), centerX, base.getBlockY(), centerZ);
            Location end = direction == Direction.UP
                    ? (side == 0 ? corners[2] : corners[3])
                    : (side == 0 ? corners[0] : corners[1]);

            Vector step = end.toVector().subtract(start.toVector()).normalize().multiply(0.1);
            double length = direction == Direction.UP ? start.distance(corners[2]) : start.distance(corners[0]);
            int segmentSteps = Math.max(0, (int) (length / 2.0 * size / 0.1));
            Location cursor = start.clone();
            for (int i = 0; i < segmentSteps; i++) {
                cursor.add(step);
                player.spawnParticle(Particle.DUST, cursor, 1, dust);
            }
        }
    }

    private static Location[] cornersForYaw(float yaw, Location base, double arrowSize) {
        float normalized = ((yaw - 90.0f) % 360.0f + 360.0f) % 360.0f;
        double cx = base.getBlockX() + 0.5;
        double cz = base.getBlockZ() + 0.5;
        double by = base.getBlockY() + 1.0;
        double ty = base.getBlockY() + arrowSize - 1.0;
        var world = base.getWorld();

        if ((normalized < 45.0f || normalized > 135.0f) && (normalized < 225.0f || normalized > 315.0f)) {
            if ((normalized < 135.0f || normalized > 225.0f) && (normalized < 315.0f || normalized > 45.0f)) {
                return null;
            }
            return new Location[]{
                    new Location(world, cx, by, cz + 0.7),
                    new Location(world, cx, by, cz - 0.7),
                    new Location(world, cx, ty, cz + 0.7),
                    new Location(world, cx, ty, cz - 0.7)
            };
        }
        return new Location[]{
                new Location(world, cx + 0.7, by, cz),
                new Location(world, cx - 0.7, by, cz),
                new Location(world, cx + 0.7, ty, cz),
                new Location(world, cx - 0.7, ty, cz)
        };
    }

    private static Particle.DustOptions dustFor(String colorName) {
        double[] rgb = resolveColor(colorName);
        return new Particle.DustOptions(
                Color.fromRGB((int) (rgb[0] * 255), (int) (rgb[1] * 255), (int) (rgb[2] * 255)),
                1.0f
        );
    }

    private static double[] resolveColor(String colorName) {
        if ("random".equalsIgnoreCase(colorName)) {
            colorName = RANDOM_COLOR_NAMES[ThreadLocalRandom.current().nextInt(RANDOM_COLOR_NAMES.length)];
        }
        return switch (colorName.toLowerCase(Locale.ROOT)) {
            case "black" -> rgb(0.001, 0.0, 0.0);
            case "dark_blue" -> rgb(0.001, 0.0, 0.66667);
            case "dark_green" -> rgb(0.001, 0.66667, 0.0);
            case "dark_aqua" -> rgb(0.001, 0.66667, 0.66667);
            case "dark_red" -> rgb(0.66667, 0.0, 0.0);
            case "dark_purple" -> rgb(0.66667, 0.0, 0.66667);
            case "gold" -> rgb(1.0, 0.66667, 0.0);
            case "gray" -> rgb(0.66667, 0.66667, 0.66667);
            case "dark_gray" -> rgb(0.33333, 0.33333, 0.33333);
            case "blue" -> rgb(0.33333, 0.33333, 1.0);
            case "green" -> rgb(0.33333, 1.0, 0.33333);
            case "aqua" -> rgb(0.33333, 1.0, 1.0);
            case "red" -> rgb(1.0, 0.33333, 0.33333);
            case "light_purple" -> rgb(1.0, 0.33333, 1.0);
            case "yellow" -> rgb(1.0, 1.0, 0.33333);
            case "white" -> rgb(1.0, 1.0, 1.0);
            default -> rgb(0.33333, 1.0, 0.33333);
        };
    }

    private static double[] rgb(double r, double g, double b) {
        return new double[]{r, g, b};
    }

    private static final String[] RANDOM_COLOR_NAMES = {
            "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple",
            "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", "white"
    };
}
