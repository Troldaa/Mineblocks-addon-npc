package cz.miniomega.mobs.rewards;

import cz.miniomega.mobs.MineMobsPlugin;
import cz.miniomega.mobs.playerdata.PlayerData;
import cz.miniomega.mobs.playerdata.placeholder.PlayerDataPlaceholderSet;
import cz.miniomega.mobs.rewards.context.RewardContext;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Getter
public class MobRewards {

    public final MineMobsPlugin plugin;
    public final List<Reward> rewards;
    public final List<Reward> lastRewards;

    public MobRewards(MineMobsPlugin plugin, List<Reward> rewards, List<Reward> lastRewards) {
        this.plugin = plugin;
        this.rewards = rewards;
        this.lastRewards = lastRewards;
    }

    private void dispatchCommand(String command) {
        if (command == null) return;
        CommandSender sender = plugin.getServer().getConsoleSender();
        plugin.getServer().dispatchCommand(sender, command);
    }

    public Runnable giveLastRewards(UUID lastBreaker, Map<UUID, PlayerData> playerDataMap) {
        List<UUID> sortedPlayers = playerDataMap.values().stream()
                .sorted(Comparator.comparingInt(PlayerData::getHits).reversed())
                .map(PlayerData::getUuid)
                .collect(Collectors.toList());
        Map<UUID, Integer> positions = new HashMap<>();
        for (int i = 0; i < sortedPlayers.size(); i++) {
            positions.put(sortedPlayers.get(i), i + 1);
        }
        RewardContext context = new RewardContext(
                plugin,
                ThreadLocalRandom.current(),
                positions,
                lastBreaker
        );
        List<Runnable> toExecute = new LinkedList<>();
        for (PlayerData player : playerDataMap.values()) {
            OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(player.uuid);
            for (Reward lastReward : lastRewards) {
                if (lastReward.canGet(player, context)) {
                    for (String rewardCmd : lastReward.getCommands().rewardPlayer(player, context)) {
                        String cmd = parsePlaceholders(offlinePlayer, player, rewardCmd);
                        toExecute.add(() -> dispatchCommand(cmd));
                    }
                }
            }
        }
        return () -> toExecute.forEach(Runnable::run);
    }

    public Runnable giveRewards(PlayerData player) {
        OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(player.uuid);
        RewardContext context = new RewardContext(
                plugin,
                ThreadLocalRandom.current(),
                null,
                null
        );
        List<Runnable> toExecute = new LinkedList<>();
        for (Reward reward : rewards) {
            if (reward.canGet(player, context)) {
                for (String rewardCmd : reward.getCommands().rewardPlayer(player, context)) {
                    toExecute.add(() -> dispatchCommand(parsePlaceholders(
                            offlinePlayer, player, rewardCmd
                    )));
                }
            }
        }
        return () -> toExecute.forEach(Runnable::run);
    }

    private String parsePlaceholders(OfflinePlayer player, PlayerData playerData, String cmd) {
        PlayerDataPlaceholderSet placeholderSet = new PlayerDataPlaceholderSet(playerData);
        return plugin.integrationManager.setPlaceholders(player, placeholderSet.parse(cmd));
    }

}
