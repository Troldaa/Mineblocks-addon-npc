package cz.raixo.mobs;

import cz.raixo.blocks.block.health.BlockHealth;
import cz.raixo.blocks.block.messages.BlockMessages;
import cz.raixo.blocks.block.playerdata.PlayerData;
import cz.raixo.blocks.block.rewards.BlockRewards;
import cz.raixo.blocks.block.top.BlockTop;
import cz.raixo.blocks.util.color.Colors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import cz.raixo.blocks.util.color.Colors;
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

import java.util.*;

@Getter
@Setter
@RequiredArgsConstructor
public class MineMob {
    private final MineMobsPlugin plugin;
    private String id;
    private EntityType type;
    private Location location;
    private BlockHealth health; // Reusing BlockHealth for simplicity
    private BlockRewards rewards; // Reusing BlockRewards
    private BlockMessages messages;
    private String permission;
    private BlockTop top = new BlockTop();
    private Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    private List<String> hologramLines = new ArrayList<>();
    private Sound hitSound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
    private Sound defeatSound = Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST;
    private int cooldownSeconds = 10;
    private boolean isCoolingDown = false;
    private int remainingCooldown = 0;
    private int regenerationIdleSeconds = 5;
    private long lastHitTime = 0;

    private boolean glowingRed = true;
    private boolean launchMode = false;
    private int launchChance = 100;
    private double launchRange = 5.0;
    private int fireworkHeight = 5;
    private boolean tntCannonEffect = true;
    private boolean chickenLauncherEffect = false;
    private double mobScale = 1.0;

    private Entity spawnedEntity;
    private cz.raixo.blocks.integration.models.hologram.Hologram hologram;
    private BukkitTask ticker;

    public void spawn() {
        if (spawnedEntity != null && !spawnedEntity.isDead()) {
            spawnedEntity.remove();
        }
        spawnedEntity = location.getWorld().spawnEntity(location, type);
        if (spawnedEntity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) spawnedEntity;
            living.setAI(false);
            living.setRemoveWhenFarAway(false);
            living.setPersistent(true);

            AttributeInstance scaleAttr = living.getAttribute(Attribute.valueOf("GENERIC_SCALE"));
            if (scaleAttr != null) {
                scaleAttr.setBaseValue(mobScale);
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
            if (health.getHealth() < health.getMaxHealth() && System.currentTimeMillis() - lastHitTime > regenerationIdleSeconds * 1000L) {
                health.setHealth(health.getHealth() + 1);
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
            spawnedEntity.remove();
            spawnedEntity = null;
        }
        removeHologram();
        if (ticker != null) ticker.cancel();
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

        List<Runnable> runnables = new LinkedList<>();
        PlayerData playerData = playerDataMap.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerData(uuid, player.getName()));
        playerData.incrementBreaks();
        top.update(playerData);
        runnables.add(rewards.giveRewards(playerData));

        if (health.getHealth() <= 0) {
            runnables.add(onDeath(player));
        } else {
            updateName();
            updateHologram();
        }

        return () -> runnables.forEach(Runnable::run);
    }

    private Runnable onDeath(Player player) {
        Runnable runnable = rewards.giveLastRewards(player.getUniqueId(), playerDataMap);
        broadcast(messages.getBreakMessage(), player);

        player.playSound(location, defeatSound, 1.0f, 1.0f);
        spawnFirework();
        if (tntCannonEffect) spawnTntCannon();
        if (chickenLauncherEffect) spawnChickenLauncher();

        isCoolingDown = true;
        remainingCooldown = cooldownSeconds;
        if (spawnedEntity instanceof LivingEntity) {
            spawnedEntity.setGlowing(true);
            if (glowingRed) {
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
        }, cooldownSeconds * 20L);

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
        meta.setPower(fireworkHeight / 2); // Approximate power to height
        firework.setFireworkMeta(meta);

        // If height > 0 we let it fly, if not we detonate immediately
        if (fireworkHeight <= 0) firework.detonate();
    }

    private void spawnTntCannon() {
        for (int i = 0; i < 8; i++) {
            double h = (spawnedEntity != null ? spawnedEntity.getHeight() : 1.0) / 2.0;
            TNTPrimed tnt = location.getWorld().spawn(location.clone().add(0, h, 0), TNTPrimed.class);
            tnt.setFuseTicks(40);
            tnt.setYield(0); // No block damage
            tnt.setIsIncendiary(false);

            double angle = i * (Math.PI / 4);
            Vector velocity = new Vector(Math.cos(angle), 0.5, Math.sin(angle)).multiply(0.5);
            tnt.setVelocity(velocity);
        }
    }

    private void spawnChickenLauncher() {
        Chicken chicken = location.getWorld().spawn(location.clone().add(0, 1, 0), Chicken.class);
        chicken.setVelocity(new Vector(0, 1.2, 0)); // Launch up
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
        }, 15L); // Explode after ~0.75 seconds (reached peak)
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
            p.sendMessage(plugin.getMineBlocks().getIntegrationManager().setPlaceholders(p, coloredMessage));
        }
    }

    public void createHologram() {
        removeHologram();
        if (hologramLines.isEmpty()) return;
        double height = 2.0;
        if (spawnedEntity != null) {
            height = spawnedEntity.getHeight() + 0.5;
        }
        hologram = plugin.getMineBlocks().getIntegrationManager().getHologramProvider().provide("minemob_" + id, location.clone().add(0, height, 0));
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

    private String replacePlaceholders(String line) {
        line = line.replace("%name%", id)
                   .replace("%health%", String.valueOf(health.getHealth()))
                   .replace("%max_health%", String.valueOf(health.getMaxHealth()));

        if (isCoolingDown) {
            line = line.replace("%cooldown%", String.valueOf(remainingCooldown));
        } else {
            line = line.replace("%cooldown%", "");
        }

        List<PlayerData> players = top.getPlayers();
        for (int i = 1; i <= 3; i++) {
            if (players.size() >= i) {
                PlayerData data = players.get(i - 1);
                line = line.replace("%player_name_" + i + "%", data.getDisplayName())
                           .replace("%playerhits_" + i + "%", String.valueOf(data.getBreaks()));
            } else {
                line = line.replace("%player_name_" + i + "%", "---")
                           .replace("%playerhits_" + i + "%", "0");
            }
        }
        return Colors.colorize(line);
    }
}
