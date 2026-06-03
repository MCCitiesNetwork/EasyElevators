package com.minecraftcitiesnetwork.easyelevators;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public final class EasyElevatorsPlugin extends JavaPlugin {

    private ElevatorConfig elevatorConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands registrar = event.registrar();
            registrar.register(
                    Commands.literal("easyelevators")
                            .then(Commands.literal("reload")
                                    .requires(ctx -> ctx.getSender().hasPermission("easyelevators.reload"))
                                    .executes(ctx -> {
                                        CommandSender sender = ctx.getSource().getSender();
                                        reloadElevatorConfigAsync().thenAccept(success ->
                                                runOnMain(() -> sendReloadResult(sender, success)));
                                        return Command.SINGLE_SUCCESS;
                                    }))
                            .build(),
                    "EasyElevators commands"
            );
        });

        reloadElevatorConfigAsync().thenAccept(success -> runOnMain(() -> {
            if (!success) {
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            getServer().getPluginManager().registerEvents(new ElevatorListener(this), this);
            getLogger().info("EasyElevators enabled.");
        }));
    }

    @Override
    public void onDisable() {
        PlayerData.hideAllBossBars();
        getLogger().info("EasyElevators disabled.");
    }

    public CompletableFuture<Boolean> reloadElevatorConfigAsync() {
        Path configPath = getDataFolder().toPath().resolve("config.yml");
        return CompletableFuture.supplyAsync(() -> readConfigFromDisk(configPath))
                .thenCompose(loaded -> {
                    if (loaded == null) {
                        return CompletableFuture.completedFuture(false);
                    }
                    CompletableFuture<Boolean> applied = new CompletableFuture<>();
                    runOnMain(() -> {
                        try {
                            loaded.sanitize(getLogger());
                            elevatorConfig = loaded;
                            applied.complete(true);
                        } catch (RuntimeException e) {
                            getLogger().severe("Failed to apply config.yml: " + e.getMessage());
                            applied.complete(false);
                        }
                    });
                    return applied;
                });
    }

    private ElevatorConfig readConfigFromDisk(Path configPath) {
        try {
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .path(configPath)
                    .build();
            ConfigurationNode root = loader.load();
            ElevatorConfig loaded = root.get(ElevatorConfig.class);
            if (loaded == null) {
                getLogger().severe("Invalid config.yml structure: file is empty");
                return null;
            }
            return loaded;
        } catch (SerializationException e) {
            getLogger().severe("Invalid config.yml structure: " + e.getMessage());
            return null;
        } catch (IOException e) {
            getLogger().severe("Failed to load config.yml: " + e.getMessage());
            return null;
        }
    }

    private void runOnMain(Runnable task) {
        if (!isEnabled()) {
            return;
        }
        getServer().getScheduler().runTask(this, task);
    }

    private void sendReloadResult(CommandSender sender, boolean success) {
        if (success) {
            sender.sendPlainMessage("EasyElevators config reloaded.");
        } else {
            sender.sendPlainMessage("Failed to reload EasyElevators config. Check console logs.");
        }
    }

    public ElevatorConfig elevatorConfig() {
        return elevatorConfig;
    }
}
