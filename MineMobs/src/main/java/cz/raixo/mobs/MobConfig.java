package cz.raixo.mobs;

import cz.raixo.blocks.block.health.BlockHealth;
import cz.raixo.blocks.block.messages.BlockMessages;
import cz.raixo.blocks.block.rewards.BlockRewards;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class MobConfig {
    private final MineMobsPlugin plugin;
    private final File file;
    private FileConfiguration config;

    public MobConfig(MineMobsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "mobs.yml");
        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void loadMobs() {
        ConfigurationSection section = config.getConfigurationSection("mobs");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            ConfigurationSection mobSection = section.getConfigurationSection(id);
            if (mobSection == null) continue;

            MineMob mob = new MineMob(plugin);
            mob.setId(id);
            mob.setType(EntityType.valueOf(mobSection.getString("type", "ZOMBIE")));
            mob.setLocation(mobSection.getLocation("location"));
            mob.setHealth(new BlockHealth(null, mobSection.getInt("health", 10)));
            mob.setRewards(new BlockRewards(plugin.getMineBlocks(), null, new LinkedList<>(), new LinkedList<>())); // Basic implementation
            mob.setMessages(new BlockMessages(mobSection.getString("break-message", "&aMob %player% was defeated!")));
            mob.setPermission(mobSection.getString("permission"));

            mob.setHologramLines(mobSection.getStringList("hologram-lines"));
            mob.setHitSound(Sound.valueOf(mobSection.getString("hit-sound", "ENTITY_EXPERIENCE_ORB_PICKUP")));
            mob.setCooldownSeconds(mobSection.getInt("cooldown", 10));
            mob.setRegenerationIdleTicks(mobSection.getInt("regeneration-idle-ticks", 100));

            plugin.getMobRegistry().register(mob);
        }
    }

    public void saveMobs() {
        config.set("mobs", null);
        for (MineMob mob : plugin.getMobRegistry().getMobs()) {
            String path = "mobs." + mob.getId();
            config.set(path + ".type", mob.getType().name());
            config.set(path + ".location", mob.getLocation());
            config.set(path + ".health", mob.getHealth().getMaxHealth());
            config.set(path + ".break-message", mob.getMessages().getBreakMessage());
            config.set(path + ".permission", mob.getPermission());
            config.set(path + ".hologram-lines", mob.getHologramLines());
            config.set(path + ".hit-sound", mob.getHitSound().name());
            config.set(path + ".cooldown", mob.getCooldownSeconds());
            config.set(path + ".regeneration-idle-ticks", mob.getRegenerationIdleTicks());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
