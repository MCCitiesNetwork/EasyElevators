package com.minecraftcitiesnetwork.easyelevators;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;

public final class EasyElevatorsPlugin extends JavaPlugin {

    private ElevatorConfig elevatorConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!reloadElevatorConfig()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands registrar = event.registrar();
            registrar.register(
                    Commands.literal("easyelevators")
                            .then(Commands.literal("reload")
                                    .requires(ctx -> ctx.getSender().hasPermission("easyelevators.reload"))
                                    .executes(ctx -> {
                                        var sender = ctx.getSource().getSender();
                                        if (reloadElevatorConfig()) {
                                            sender.sendPlainMessage("EasyElevators config reloaded.");
                                            return Command.SINGLE_SUCCESS;
                                        }
                                        sender.sendPlainMessage("Failed to reload EasyElevators config. Check console logs.");
                                        return 0;
                                    }))
                            .build(),
                    "EasyElevators commands"
            );
        });

        getServer().getPluginManager().registerEvents(new ElevatorListener(this), this);
        getLogger().info("EasyElevators enabled.");
    }

    @Override
    public void onDisable() {
        PlayerData.hideAllBossBars();
        getLogger().info("EasyElevators disabled.");
    }

    public boolean reloadElevatorConfig() {
        try {
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .path(getDataFolder().toPath().resolve("config.yml"))
                    .build();
            ConfigurationNode root = loader.load();
            ElevatorConfig loaded = root.get(ElevatorConfig.class);
            if (loaded == null) {
                throw new IOException("Invalid config.yml structure: file is empty");
            }
            loaded.sanitize(getLogger());
            this.elevatorConfig = loaded;
            return true;
        } catch (SerializationException e) {
            getLogger().severe("Invalid config.yml structure: " + e.getMessage());
            return false;
        } catch (IOException e) {
            getLogger().severe("Failed to load config.yml: " + e.getMessage());
            return false;
        }
    }

    public ElevatorConfig elevatorConfig() {
        return elevatorConfig;
    }
}
