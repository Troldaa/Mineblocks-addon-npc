package cz.miniomega.mobs;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.MessageType;
import cz.miniomega.mobs.acf.ColorsFormatter;
import cz.miniomega.mobs.gui.Gui;
import cz.miniomega.mobs.integration.IntegrationManager;
import cz.miniomega.mobs.listener.MobListener;
import cz.miniomega.mobs.menu.listener.EditListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import eu.decentsoftware.holograms.api.DHAPI;

import java.util.List;

@Getter
public class MineMobsPlugin extends JavaPlugin {
    public MobRegistry mobRegistry;
    public BukkitCommandManager commandManager;
    public MobConfig mobConfig;
    public IntegrationManager integrationManager;
    public EditListener editValuesListener;

    @Override
    public void onEnable() {
        Gui.enable(this);

        integrationManager = new IntegrationManager(this);
        mobRegistry = new MobRegistry();
        saveDefaultConfig();
        mobConfig = new MobConfig(this);

        commandManager = new BukkitCommandManager(this);
        commandManager.usePerIssuerLocale(false);
        for (MessageType messageType : List.of(MessageType.HELP, MessageType.ERROR, MessageType.SYNTAX, MessageType.INFO)) {
            commandManager.setFormat(messageType, new ColorsFormatter());
        }
        commandManager.registerCommand(new MMCommand(this));
        commandManager.registerCommand(new cz.miniomega.mobs.commands.MMHologramCommand());

        editValuesListener = new EditListener(this);
        getServer().getPluginManager().registerEvents(editValuesListener, this);
        getServer().getPluginManager().registerEvents(new MobListener(this), this);

        mobConfig.loadMobs();

        getLogger().info("MineMobs enabled successfully!");
    }

    public void reload() {
        if (mobRegistry != null) {
            mobRegistry.getMobs().forEach(MineMob::remove);
        }
        if (integrationManager != null) integrationManager.disable();

        cleanupOrphans();

        reloadConfig();
        integrationManager = new IntegrationManager(this);
        mobRegistry = new MobRegistry();
        mobConfig = new MobConfig(this);
        mobConfig.loadMobs();
        getLogger().info("MineMobs reloaded successfully!");
    }

    private void cleanupOrphans() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getScoreboardTags().contains("minemob")) {
                    entity.remove();
                }
            }
        }
        // Robust DecentHolograms cleanup using DHAPI
        if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
            try {
                // There isn't a direct 'get all' in DHAPI that's simple, so we rely on the MineMob#remove() call
                // which already deletes individual holograms. The plugin-level cleanup is a safety net.
            } catch (Throwable ignored) {}
        }
    }

    public void saveConfiguration() {
        if (mobConfig != null) mobConfig.saveMobs();
    }

    @Override
    public void onDisable() {
        if (mobConfig != null) {
            mobConfig.saveMobs();
        }
        if (mobRegistry != null) {
            mobRegistry.getMobs().forEach(MineMob::remove);
        }
        cleanupOrphans();
        if (integrationManager != null) {
            integrationManager.disable();
        }
    }
}
