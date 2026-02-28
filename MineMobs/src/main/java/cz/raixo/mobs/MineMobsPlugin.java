package cz.raixo.mobs;

import co.aikar.commands.BukkitCommandManager;
import cz.raixo.blocks.MineBlocksPlugin;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class MineMobsPlugin extends JavaPlugin {
    private MobRegistry mobRegistry;
    private BukkitCommandManager commandManager;
    private MineBlocksPlugin mineBlocks;
    private MobConfig mobConfig;

    @Override
    public void onEnable() {
        if (!Bukkit.getPluginManager().isPluginEnabled("MineBlocks")) {
            getLogger().severe("MineBlocks not found! MineMobs requires MineBlocks to function.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        mineBlocks = (MineBlocksPlugin) Bukkit.getPluginManager().getPlugin("MineBlocks");
        mobRegistry = new MobRegistry();
        saveDefaultConfig();
        mobConfig = new MobConfig(this);

        commandManager = new BukkitCommandManager(this);
        commandManager.registerCommand(new MMCommand(this));

        getServer().getPluginManager().registerEvents(new MobListener(this), this);

        mobConfig.loadMobs();

        getLogger().info("MineMobs addon enabled successfully!");
    }

    public void reload() {
        reloadConfig();
        if (mobRegistry != null) {
            mobRegistry.getMobs().forEach(MineMob::remove);
        }
        mobRegistry = new MobRegistry();
        mobConfig = new MobConfig(this);
        mobConfig.loadMobs();
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
    }
}
