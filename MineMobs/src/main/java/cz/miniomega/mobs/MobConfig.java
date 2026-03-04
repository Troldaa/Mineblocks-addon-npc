package cz.miniomega.mobs;

import cz.miniomega.mobs.health.MobHealth;
import cz.miniomega.mobs.messages.MobMessages;
import cz.miniomega.mobs.rewards.MobRewards;
import cz.miniomega.mobs.rewards.Reward;
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
    public final MineMobsPlugin plugin;
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
            mob.setHealth(new MobHealth(null, mobSection.getInt("health", 10)));

            List<Reward> lastRewards = new LinkedList<>();
            ConfigurationSection rewardsSec = mobSection.getConfigurationSection("rewards");
            if (rewardsSec != null) {
                for (String rName : rewardsSec.getKeys(false)) {
                    lastRewards.add(Reward.parse(rewardsSec.getConfigurationSection(rName)));
                }
            }
            mob.setRewards(new MobRewards(plugin, new LinkedList<>(), lastRewards));

            mob.setMessages(new MobMessages(mobSection.getString("defeat-message", "&aMob %player% was defeated!")));
            mob.setPermission(mobSection.getString("permission"));

            mob.setHologramLines(mobSection.getStringList("hologram-lines"));
            mob.setHitSound(Sound.valueOf(mobSection.getString("hit-sound", "ENTITY_EXPERIENCE_ORB_PICKUP")));
            mob.setCooldownSeconds(mobSection.getInt("cooldown", 10));
            mob.setRegenerationIdleSeconds(mobSection.getInt("regeneration-idle-seconds", 5));
            mob.setDefeatSound(Sound.valueOf(mobSection.getString("defeat-sound", "ENTITY_FIREWORK_ROCKET_LARGE_BLAST")));
            mob.setGlowMode(mobSection.getInt("glow-mode", 2));
            mob.setFireworkEffect(mobSection.getBoolean("firework-effect", true));
            mob.setLaunchMode(mobSection.getBoolean("launch-mode", false));
            mob.setLaunchChance(mobSection.getInt("launch-chance", 100));
            mob.setLaunchRange(mobSection.getDouble("launch-range", 5.0));
            mob.setFireworkHeight(mobSection.getInt("firework-height", 5));
            mob.setTntCannonEffect(mobSection.getBoolean("tnt-cannon", true));
            mob.setTntCannonCount(mobSection.getInt("tnt-count", 8));
            mob.setChickenLauncherEffect(mobSection.getBoolean("chicken-launcher", false));
            mob.setChickenLauncherRange(mobSection.getDouble("chicken-range", 1.2));
            mob.setMobScale(mobSection.getDouble("mob-scale", 1.0));

            plugin.mobRegistry.register(mob);
        }
    }

    public void saveMobs() {
        config.set("mobs", null);
        for (MineMob mob : plugin.mobRegistry.getMobs()) {
            String path = "mobs." + mob.getId();
            config.set(path + ".type", mob.getType().name());
            config.set(path + ".location", mob.getLocation());
            config.set(path + ".health", mob.health.maxHealth);
            config.set(path + ".defeat-message", mob.getMessages().defeatMessage);
            config.set(path + ".permission", mob.getPermission());
            config.set(path + ".hologram-lines", mob.getHologramLines());
            config.set(path + ".hit-sound", mob.getHitSound().name());
            config.set(path + ".defeat-sound", mob.getDefeatSound().name());
            config.set(path + ".cooldown", mob.cooldownSeconds);
            config.set(path + ".regeneration-idle-seconds", mob.regenerationIdleSeconds);
            config.set(path + ".glow-mode", mob.glowMode);
            config.set(path + ".firework-effect", mob.fireworkEffect);
            config.set(path + ".launch-mode", mob.launchMode);
            config.set(path + ".launch-chance", mob.launchChance);
            config.set(path + ".launch-range", mob.launchRange);
            config.set(path + ".firework-height", mob.fireworkHeight);
            config.set(path + ".tnt-cannon", mob.tntCannonEffect);
            config.set(path + ".tnt-count", mob.tntCannonCount);
            config.set(path + ".chicken-launcher", mob.chickenLauncherEffect);
            config.set(path + ".chicken-range", mob.chickenLauncherRange);
            config.set(path + ".mob-scale", mob.mobScale);

            ConfigurationSection rewardsSec = config.createSection(path + ".rewards");
            for (Reward reward : mob.rewards.getLastRewards()) {
                Reward.save(rewardsSec.createSection(reward.getName()), reward);
            }
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
