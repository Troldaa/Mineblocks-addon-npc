package cz.miniomega.mobs;

import cz.miniomega.mobs.health.MobHealth;
import cz.miniomega.mobs.messages.MobMessages;
import cz.miniomega.mobs.playerdata.PlayerData;
import cz.miniomega.mobs.rewards.MobRewards;
import cz.miniomega.mobs.top.MobTop;
import cz.miniomega.mobs.util.color.Colors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;
import java.util.*;

@Getter
@Setter
@RequiredArgsConstructor
public class MineMob {
    public final MineMobsPlugin plugin;
    public String id;
    public EntityType type;
    public Location location;
    public MobHealth health;
    public MobRewards rewards;
    public MobMessages messages;
    public String permission;
    public MobTop top = new MobTop();
    public Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    public List<String> hologramLines = new ArrayList<>();
    public Sound hitSound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
    public Sound defeatSound = Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST;
    public int cooldownSeconds = 10;
    public boolean isCoolingDown = false;
    public int remainingCooldown = 0;
    public int regenerationIdleSeconds = 5;
    public long lastHitTime = 0;

    public int glowMode = 0; // 0: None, 1: White, 2: Red
    public boolean fireworkEffect = false;
    public boolean launchMode = false;
    public int launchChance = 100;
    public double launchRange = 5.0;
    public int fireworkHeight = 5;
    public boolean tntCannonEffect = false;
    public int tntCannonCount = 8;
    public boolean chickenLauncherEffect = false;
    public double chickenLauncherRange = 1.2;
    public double mobScale = 1.0;

    public Entity spawnedEntity;
    public cz.miniomega.mobs.integration.models.hologram.Hologram hologram;
    public BukkitTask ticker;

    public void spawn() {
        if (id == null || location == null || type == null) return;
        Entity old = spawnedEntity;
        if (spawnedEntity != null) {
            spawnedEntity.remove();
        }
        spawnedEntity = location.getWorld().spawnEntity(location, type);
        spawnedEntity.addScoreboardTag("minemob");
        spawnedEntity.addScoreboardTag("minemob_" + id);

        plugin.getMobRegistry().updateEntityMap(this, old, spawnedEntity);
        if (spawnedEntity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) spawnedEntity;
            living.setAI(false);
            living.setRemoveWhenFarAway(false);
            living.setPersistent(true);

            Attribute scaleAttrRef = null;
            try {
                scaleAttrRef = (Attribute) Attribute.class.getField("GENERIC_SCALE").get(null);
            } catch (Exception e) {
                try {
                    scaleAttrRef = (Attribute) Attribute.class.getField("SCALE").get(null);
                } catch (Exception ignored) {
                    try {
                        scaleAttrRef = Attribute.valueOf("GENERIC_SCALE");
                    } catch (Exception ignored2) {
                        try {
                            scaleAttrRef = Attribute.valueOf("SCALE");
                        } catch (Exception ignored3) {}
                    }
                }
            }

            if (scaleAttrRef != null) {
                AttributeInstance scaleAttr = living.getAttribute(scaleAttrRef);
                if (scaleAttr != null) {
                    scaleAttr.setBaseValue(mobScale);
                }
            }

            updateName();
        }
        createHologram();
        startTicker();
    }

    private void startTicker() {
        if (ticker != null) ticker.cancel();
        ticker = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (isCoolingDown) {
                if (remainingCooldown > 0) {
                    remainingCooldown--;
                    updateHologram();
                }
                return;
            }
            if (health != null && health.health < health.maxHealth && System.currentTimeMillis() - lastHitTime > (long)regenerationIdleSeconds * 1000L) {
                health.setHealth(health.health + 1);
                updateHologram();
            }
        }, 20L, 20L);
    }

    public void updateName() {
        if (spawnedEntity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) spawnedEntity;
            living.setCustomName("");
            living.setCustomNameVisible(false);
        }
    }

    public void remove() {
        if (spawnedEntity != null) {
            if (spawnedEntity instanceof Wither) {
                ((Wither) spawnedEntity).setHealth(0);
            }
            spawnedEntity.remove();
            spawnedEntity = null;
        }
        removeHologram();
        if (ticker != null) {
            ticker.cancel();
            ticker = null;
        }
        isCoolingDown = false;
        remainingCooldown = 0;
    }

    public Runnable onDamage(Player player) {
        if (isCoolingDown) return () -> {};

        if (launchMode && new Random().nextInt(100) < launchChance) {
            Vector direction = player.getLocation().toVector().subtract(location.toVector()).normalize();
            direction.setY(0.5);
            player.setVelocity(direction.multiply(launchRange / 2.0));
        }

        health.decrement();
        lastHitTime = System.currentTimeMillis();
        player.playSound(location, hitSound, 1.0f, 1.0f);

        // Play red hurt animation
        if (spawnedEntity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) spawnedEntity;
            try {
                Method playHurt = living.getClass().getMethod("playHurtAnimation", float.class);
                playHurt.invoke(living, 0f);
            } catch (Exception e) {
                // Fallback for older versions
                living.damage(0.001);
            }
        }

        List<Runnable> runnables = new LinkedList<>();
        PlayerData playerData = playerDataMap.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerData(uuid, player.getName()));
        playerData.incrementHits();
        top.update(playerData);
        runnables.add(rewards.giveRewards(playerData));

        if (health.health <= 0) {
            runnables.add(onDeath(player));
        } else {
            updateName();
            updateHologram();
        }

        return () -> runnables.forEach(Runnable::run);
    }

    private Runnable onDeath(Player player) {
        Runnable runnable = rewards.giveLastRewards(player.getUniqueId(), playerDataMap);
        broadcast(messages.defeatMessage, player);

        player.playSound(location, defeatSound, 1.0f, 1.0f);
        if (fireworkEffect) spawnFirework();
        if (tntCannonEffect) spawnTntCannon();
        if (chickenLauncherEffect) spawnChickenLauncher();

        isCoolingDown = true;
        remainingCooldown = cooldownSeconds;
        if (spawnedEntity instanceof LivingEntity) {
            if (glowMode == 1) {
                spawnedEntity.setGlowing(true);
            } else if (glowMode == 2) {
                spawnedEntity.setGlowing(true);
                setRedGlow(spawnedEntity);
            }
        }
        updateHologram();

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            isCoolingDown = false;
            remainingCooldown = 0;
            if (spawnedEntity != null) {
                spawnedEntity.setGlowing(false);
                clearGlow(spawnedEntity);
            }
            reset();
        }, (long)cooldownSeconds * 20L);

        return runnable;
    }

    private void setRedGlow(Entity entity) {
        String teamName = "minemob_red";
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(teamName);
        if (team == null) {
            team = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam(teamName);
        }
        team.setColor(ChatColor.RED);
        String entry = entity instanceof Player ? entity.getName() : entity.getUniqueId().toString();
        if (!team.hasEntry(entry)) {
            team.addEntry(entry);
        }
    }

    private void clearGlow(Entity entity) {
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("minemob_red");
        if (team != null) {
            String entry = entity instanceof Player ? entity.getName() : entity.getUniqueId().toString();
            team.removeEntry(entry);
        }
    }

    private void spawnFirework() {
        double height = (spawnedEntity != null ? spawnedEntity.getHeight() : 1.0) + 0.1;
        Location fireworkLoc = location.clone().add(0, height, 0);
        Firework firework = location.getWorld().spawn(fireworkLoc, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder()
                .with(FireworkEffect.Type.CREEPER)
                .withColor(Color.BLUE)
                .withFade(Color.AQUA)
                .build());
        meta.setPower(fireworkHeight / 2);
        firework.setFireworkMeta(meta);
        if (fireworkHeight <= 0) firework.detonate();
    }

    private void spawnTntCannon() {
        for (int i = 0; i < tntCannonCount; i++) {
            double h = (spawnedEntity != null ? spawnedEntity.getHeight() : 1.0) / 2.0;
            TNTPrimed tnt = location.getWorld().spawn(location.clone().add(0, h, 0), TNTPrimed.class);
            tnt.setFuseTicks(40);
            tnt.setYield(0);
            tnt.setIsIncendiary(false);

            double angle = i * (Math.PI / 4);
            Vector velocity = new Vector(Math.cos(angle), 0.5, Math.sin(angle)).multiply(0.5);
            tnt.setVelocity(velocity);
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Player p : location.getWorld().getPlayers()) {
                if (p.getLocation().distance(location) < 5) {
                    Vector dir = p.getLocation().toVector().subtract(location.toVector()).normalize();
                    dir.setY(0.8);
                    p.setVelocity(dir.multiply(1.2));
                }
            }
        }, 40L);
    }

    private void spawnChickenLauncher() {
        Chicken chicken = location.getWorld().spawn(location.clone().add(0, 1, 0), Chicken.class);
        chicken.setVelocity(new Vector(0, chickenLauncherRange, 0));
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Location loc = chicken.getLocation();
            chicken.remove();
            Firework firework = loc.getWorld().spawn(loc, Firework.class);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder()
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .withColor(Color.YELLOW)
                    .withFade(Color.ORANGE)
                    .build());
            meta.setPower(0);
            firework.setFireworkMeta(meta);
            firework.detonate();
        }, 15L);
    }

    public void reset() {
        health.reset();
        playerDataMap.clear();
        top.clear();
        remainingCooldown = 0;
        isCoolingDown = false;
        if (spawnedEntity == null || spawnedEntity.isDead()) {
            spawn();
        } else {
            updateName();
            updateHologram();
        }
    }

    public void broadcast(String message, Player attacker) {
        if (message == null || message.isEmpty()) return;
        String coloredMessage = Colors.colorize(message.replace("%player%", attacker.getName()));
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            p.sendMessage(plugin.getIntegrationManager().setPlaceholders(p, coloredMessage));
        }
    }

    public void createHologram() {
        removeHologram();
        if (hologramLines.isEmpty()) return;
        if (plugin.getIntegrationManager().getHologramProvider() == null) return;
        double height = 2.0;
        if (spawnedEntity != null) {
            height = spawnedEntity.getHeight() + 0.5;
        }
        hologram = plugin.getIntegrationManager().getHologramProvider().provide("minemob_" + id, location.clone().add(0, height, 0));
        updateHologram();
    }

    public void updateHologram() {
        if (hologram == null) return;
        List<String> lines = new ArrayList<>();
        for (String line : hologramLines) {
            if (!isCoolingDown && line.contains("%cooldown%")) continue;
            lines.add(replacePlaceholders(line));
        }
        hologram.setLines(lines);
    }

    public void removeHologram() {
        if (hologram != null) {
            hologram.delete();
            hologram = null;
        }
    }

    private String formatTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        String format;
        if (hours > 0) {
            format = plugin.getConfig().getString("time-format.hours", "%h hours %m minutes %s seconds");
        } else if (minutes > 0) {
            format = plugin.getConfig().getString("time-format.minutes", "%m minutes %s seconds");
        } else {
            format = plugin.getConfig().getString("time-format.seconds", "%s seconds");
        }

        return format.replace("%h", String.valueOf(hours))
                     .replace("%m", String.valueOf(minutes))
                     .replace("%s", String.valueOf(seconds));
    }

    public String replacePlaceholders(String line) {
        line = line.replace("%name%", id)
                   .replace("%health%", health == null ? "0" : String.valueOf(health.health))
                   .replace("%max_health%", health == null ? "0" : String.valueOf(health.maxHealth));

        if (isCoolingDown) {
            line = line.replace("%cooldown%", formatTime(remainingCooldown));
        } else {
            line = line.replace("%cooldown%", "");
        }

        // Regeneration placeholder
        if (!isCoolingDown && health != null && health.health < health.maxHealth) {
            long remaining = (long)regenerationIdleSeconds - (System.currentTimeMillis() - lastHitTime) / 1000L;
            line = line.replace("%regen%", remaining > 0 ? String.valueOf(remaining) : "0");
        } else {
            line = line.replace("%regen%", "");
        }

        List<PlayerData> players = top.getPlayers();
        for (int i = 1; i <= 3; i++) {
            if (players.size() >= i) {
                PlayerData data = players.get(i - 1);
                line = line.replace("%player_name_" + i + "%", data.displayName)
                           .replace("%playerhits_" + i + "%", String.valueOf(data.hits));
            } else {
                line = line.replace("%player_name_" + i + "%", "---")
                           .replace("%playerhits_" + i + "%", "0");
            }
        }
        return Colors.colorize(line);
    }
}
