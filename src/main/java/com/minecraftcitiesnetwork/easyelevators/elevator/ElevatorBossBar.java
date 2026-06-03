package com.minecraftcitiesnetwork.easyelevators.elevator;

import com.minecraftcitiesnetwork.easyelevators.ElevatorConfig;
import com.minecraftcitiesnetwork.easyelevators.text.Texts;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

public final class ElevatorBossBar {

    private final Player player;
    private BossBar bossBar;
    private int currentFloor;
    private int totalFloors;

    public ElevatorBossBar(Player player) {
        this.player = player;
    }

    public void display(ElevatorConfig config, int currentFloor, int totalFloors) {
        if (!config.bossBarEnabled() || totalFloors < 2) {
            return;
        }

        this.currentFloor = currentFloor;
        this.totalFloors = Math.max(currentFloor, totalFloors);

        float progress = this.totalFloors == 0 ? 0f : (float) this.currentFloor / (float) this.totalFloors;
        if (progress < 0f || progress > 1f) {
            hide();
            return;
        }

        var title = Texts.parse(
                config.bossBarMessage(),
                Placeholder.parsed("floor", String.valueOf(this.currentFloor)),
                Placeholder.parsed("total_floors", String.valueOf(this.totalFloors))
        );

        if (bossBar == null) {
            bossBar = BossBar.bossBar(title, progress, config.bossBarColor(), config.bossBarOverlay());
            player.showBossBar(bossBar);
        } else {
            bossBar.name(title);
            bossBar.progress(progress);
        }
    }

    public void hide() {
        if (bossBar != null) {
            player.hideBossBar(bossBar);
            bossBar = null;
        }
    }

    public int currentFloor() {
        return currentFloor;
    }

    public int totalFloors() {
        return totalFloors;
    }
}
