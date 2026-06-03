package com.minecraftcitiesnetwork.easyelevators;

import com.minecraftcitiesnetwork.easyelevators.elevator.ElevatorSound;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

@ConfigSerializable
public final class ElevatorConfig {

    @Setting("elevator-material")
    public String elevatorMaterial = "minecraft:iron_block";

    @Setting("ignore-materials")
    public List<String> ignoreMaterials = List.of(
            "minecraft:air",
            "minecraft:cave_air",
            "minecraft:void_air",
            "minecraft:light",
            "minecraft:torch",
            "minecraft:wall_torch",
            "minecraft:soul_torch",
            "minecraft:soul_wall_torch",
            "minecraft:redstone_torch",
            "minecraft:redstone_wall_torch",
            "minecraft:lever",
            "#minecraft:buttons",
            "#minecraft:all_signs"
    );

    @Setting("disabled-worlds")
    public List<String> disabledWorlds = List.of();

    @Setting("sounds")
    public Sounds sounds = new Sounds();

    @Setting("boss-bar")
    public BossBarSettings bossBar = new BossBarSettings();

    @Setting("arrows")
    public Arrows arrows = new Arrows();

    private Material resolvedElevatorMaterial = Material.IRON_BLOCK;
    private Set<Material> resolvedIgnoreMaterials = Set.of();
    private BossBar.Color resolvedBossBarColor = BossBar.Color.RED;
    private BossBar.Overlay resolvedBossBarOverlay = BossBar.Overlay.PROGRESS;
    private ElevatorSound resolvedSoundUp;
    private ElevatorSound resolvedSoundDown;

    public void sanitize(Logger log) {
        Material material = resolveMaterial(elevatorMaterial, Material.IRON_BLOCK, "elevator-material", log);
        this.resolvedElevatorMaterial = material == null ? Material.IRON_BLOCK : material;
        this.resolvedIgnoreMaterials = resolveMaterialList(ignoreMaterials, "ignore-materials", log);

        Sounds soundSection = sounds == null ? new Sounds() : sounds;
        sanitizeSoundEntry(soundSection.up, "sounds.up", log);
        sanitizeSoundEntry(soundSection.down, "sounds.down", log);
        this.resolvedSoundUp = new ElevatorSound(soundSection.up, 0.5f);
        this.resolvedSoundDown = new ElevatorSound(soundSection.down, 1.0f);

        BossBarSettings bossBarSection = bossBar == null ? new BossBarSettings() : bossBar;
        this.resolvedBossBarColor = parseBossBarColor(bossBarSection.color, log);
        this.resolvedBossBarOverlay = parseBossBarOverlay(bossBarSection.style, log);
    }

    public boolean isWorldAllowed(String worldName) {
        return disabledWorlds == null || disabledWorlds.isEmpty() || !disabledWorlds.contains(worldName);
    }

    public Material elevatorMaterial() {
        return resolvedElevatorMaterial;
    }

    public Set<Material> ignoreMaterials() {
        return resolvedIgnoreMaterials;
    }

    public boolean bossBarEnabled() {
        return bossBar != null && bossBar.enabled;
    }

    public String bossBarMessage() {
        return bossBar == null ? "" : bossBar.message;
    }

    public BossBar.Color bossBarColor() {
        return resolvedBossBarColor;
    }

    public BossBar.Overlay bossBarOverlay() {
        return resolvedBossBarOverlay;
    }

    public boolean arrowsEnabled() {
        return arrows != null && arrows.enabled;
    }

    public boolean arrowsOnCurrentFloor() {
        return arrows != null && arrows.currentFloor;
    }

    public boolean arrowsOnDestinationFloor() {
        return arrows != null && arrows.destinationFloor;
    }

    public String arrowColorUp() {
        return arrows == null ? "green" : arrows.colorUp;
    }

    public String arrowColorDown() {
        return arrows == null ? "red" : arrows.colorDown;
    }

    public double arrowSize() {
        return arrows == null ? 2.0 : arrows.size;
    }

    public ElevatorSound soundUp() {
        return resolvedSoundUp;
    }

    public ElevatorSound soundDown() {
        return resolvedSoundDown;
    }

    private static BossBar.Color parseBossBarColor(String value, Logger log) {
        if (value == null || value.isBlank()) {
            return BossBar.Color.RED;
        }
        try {
            return BossBar.Color.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            log.warning("Invalid boss-bar.color '" + value + "', using RED.");
            return BossBar.Color.RED;
        }
    }

    private static BossBar.Overlay parseBossBarOverlay(String value, Logger log) {
        if (value == null || value.isBlank()) {
            return BossBar.Overlay.PROGRESS;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "SOLID", "PROGRESS" -> BossBar.Overlay.PROGRESS;
            case "SEGMENTED_6", "NOTCHED_6" -> BossBar.Overlay.NOTCHED_6;
            case "SEGMENTED_10", "NOTCHED_10" -> BossBar.Overlay.NOTCHED_10;
            case "SEGMENTED_12", "NOTCHED_12" -> BossBar.Overlay.NOTCHED_12;
            case "SEGMENTED_20", "NOTCHED_20" -> BossBar.Overlay.NOTCHED_20;
            default -> {
                log.warning("Invalid boss-bar.style '" + value + "', using PROGRESS.");
                yield BossBar.Overlay.PROGRESS;
            }
        };
    }

    private static void sanitizeSoundEntry(SoundEntry entry, String label, Logger log) {
        if (entry == null || entry.sound == null || entry.sound.isBlank()) {
            log.warning("Missing sound in " + label + ", using default.");
            entry.sound = "minecraft:entity.experience_orb.pickup";
            return;
        }
        NamespacedKey key = NamespacedKey.fromString(entry.sound);
        if (key == null || Registry.SOUNDS.get(key) == null) {
            log.warning("Invalid sound '" + entry.sound + "' in " + label + ", using minecraft:entity.experience_orb.pickup.");
            entry.sound = "minecraft:entity.experience_orb.pickup";
        }
    }

    private static Material resolveMaterial(String materialKey, Material fallback, String label, Logger log) {
        if (materialKey == null || materialKey.isBlank()) {
            return fallback;
        }
        NamespacedKey key = NamespacedKey.fromString(materialKey);
        if (key == null) {
            log.warning("Invalid material key in " + label + ": " + materialKey);
            return fallback;
        }
        Material material = Registry.MATERIAL.get(key);
        if (material == null) {
            log.warning("Unknown material in " + label + ": " + materialKey);
            return fallback;
        }
        return material;
    }

    private static Set<Material> resolveMaterialList(List<String> keys, String label, Logger log) {
        if (keys == null || keys.isEmpty()) {
            return Set.of();
        }
        Set<Material> materials = new HashSet<>();
        for (String entry : keys) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            resolveMaterialEntry(entry.trim(), label, materials, log);
        }
        return Set.copyOf(materials);
    }

    private static void resolveMaterialEntry(String entry, String label, Set<Material> materials, Logger log) {
        boolean tagEntry = entry.startsWith("#");
        String keyString = tagEntry ? entry.substring(1) : entry;
        NamespacedKey key = NamespacedKey.fromString(keyString);
        if (key == null) {
            log.warning("Invalid material or tag in " + label + ": " + entry);
            return;
        }
        if (tagEntry) {
            if (!addTagMaterials(key, label, materials, log)) {
                log.warning("Unknown block tag in " + label + ": " + entry
                        + " (see https://minecraft.wiki/w/Block_tag_(Java_Edition))");
            }
            return;
        }
        Material material = Registry.MATERIAL.get(key);
        if (material != null) {
            materials.add(material);
            return;
        }
        if (addTagMaterials(key, label, materials, log)) {
            return;
        }
        log.warning("Unknown material in " + label + ": " + entry);
    }

    private static boolean addTagMaterials(NamespacedKey tagKey, String label, Set<Material> materials, Logger log) {
        Tag<Material> tag = Bukkit.getTag(Tag.REGISTRY_BLOCKS, tagKey, Material.class);
        if (tag == null || tag.getValues().isEmpty()) {
            return false;
        }
        int before = materials.size();
        materials.addAll(tag.getValues());
        log.info("Loaded tag " + tagKey + " (" + (materials.size() - before) + " materials) for " + label);
        return true;
    }

    @ConfigSerializable
    public static final class Sounds {
        @Setting("up")
        public SoundEntry up = new SoundEntry(0.5f);

        @Setting("down")
        public SoundEntry down = new SoundEntry(1.0f);
    }

    @ConfigSerializable
    public static final class SoundEntry {
        @Setting("enabled")
        public boolean enabled = true;

        @Setting("sound")
        public String sound = "minecraft:entity.experience_orb.pickup";

        @Setting("pitch")
        public float pitch;

        @Setting("volume")
        public float volume = 1.0f;

        @Setting("world")
        public boolean world = false;

        public SoundEntry() {
            this.pitch = 0.5f;
        }

        public SoundEntry(float defaultPitch) {
            this.pitch = defaultPitch;
        }
    }

    @ConfigSerializable
    public static final class BossBarSettings {
        @Setting("enabled")
        public boolean enabled = true;

        @Setting("color")
        public String color = "RED";

        @Setting("style")
        public String style = "PROGRESS";

        @Setting("message")
        public String message = "<yellow>Floor <floor> of <total_floors>";
    }

    @ConfigSerializable
    public static final class Arrows {
        @Setting("enabled")
        public boolean enabled = true;

        @Setting("current-floor")
        public boolean currentFloor = true;

        @Setting("destination-floor")
        public boolean destinationFloor = true;

        @Setting("color-up")
        public String colorUp = "green";

        @Setting("color-down")
        public String colorDown = "red";

        @Setting("size")
        public double size = 2.0;
    }
}
