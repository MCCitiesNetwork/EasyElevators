package com.minecraftcitiesnetwork.easyelevators.elevator;

import com.minecraftcitiesnetwork.easyelevators.ElevatorConfig;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class ElevatorSound {

    private final boolean enabled;
    private final boolean world;
    private final float volume;
    private final float pitch;
    private final Sound sound;

    public ElevatorSound(ElevatorConfig.SoundEntry entry, float defaultPitch) {
        if (entry == null) {
            this.enabled = true;
            this.world = false;
            this.volume = 1.0f;
            this.pitch = defaultPitch;
            this.sound = Registry.SOUNDS.get(NamespacedKey.minecraft("entity.experience_orb.pickup"));
        } else {
            this.enabled = entry.enabled;
            this.world = entry.world;
            this.volume = entry.volume;
            this.pitch = entry.pitch != 0 ? entry.pitch : defaultPitch;
            NamespacedKey key = NamespacedKey.fromString(entry.sound);
            Sound resolved = key == null ? null : Registry.SOUNDS.get(key);
            this.sound = resolved == null
                    ? Registry.SOUNDS.get(NamespacedKey.minecraft("entity.experience_orb.pickup"))
                    : resolved;
        }
    }

    public void playSound(Player player) {
        if (!enabled || sound == null) {
            return;
        }
        if (world) {
            player.getWorld().playSound(player.getLocation(), sound, volume, pitch);
        } else {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }
}
