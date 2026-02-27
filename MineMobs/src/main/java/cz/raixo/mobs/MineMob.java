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
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

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

    private Entity spawnedEntity;

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
            // We can set custom name to show health if we want, or use holograms if we integrate deeper
            updateName();
        }
    }

    public void updateName() {
        if (spawnedEntity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) spawnedEntity;
            living.setCustomName(Colors.colorize("&b" + id + " &7[" + health.getHealth() + "/" + health.getMaxHealth() + "]"));
            living.setCustomNameVisible(true);
        }
    }

    public void remove() {
        if (spawnedEntity != null) {
            spawnedEntity.remove();
            spawnedEntity = null;
        }
    }

    public Runnable onDamage(Player player) {
        health.decrement();

        List<Runnable> runnables = new LinkedList<>();
        PlayerData playerData = playerDataMap.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerData(uuid, player.getName()));
        playerData.incrementBreaks();
        top.update(playerData);
        runnables.add(rewards.giveRewards(playerData));

        if (health.getHealth() <= 0) {
            runnables.add(onDeath(player));
        } else {
            updateName();
        }

        return () -> runnables.forEach(Runnable::run);
    }

    private Runnable onDeath(Player player) {
        Runnable runnable = rewards.giveLastRewards(player.getUniqueId());
        broadcast(messages.getBreakMessage(), player);
        reset();
        return runnable;
    }

    public void reset() {
        health.reset();
        playerDataMap.clear();
        top.clear();
        spawn();
    }

    public void broadcast(String message, Player attacker) {
        if (message == null || message.isEmpty()) return;
        String coloredMessage = Colors.colorize(message.replace("%player%", attacker.getName()));
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            p.sendMessage(coloredMessage);
        }
    }
}
