package cz.miniomega.mobs.rewards.commands;

import cz.miniomega.mobs.playerdata.PlayerData;
import cz.miniomega.mobs.rewards.commands.batch.BatchRewardCommands;
import cz.miniomega.mobs.rewards.commands.random.RandomRewardCommands;
import cz.miniomega.mobs.rewards.context.RewardContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public interface RewardCommands<T extends RewardEntry> {

    static RewardCommands<? extends RewardEntry> parse(@Nullable String mode, List<String> commands) {
        switch (Optional.ofNullable(mode).map(String::toLowerCase).orElse("")) {
            case "all":
                return new BatchRewardCommands(commands);
            default:
                return new RandomRewardCommands(commands);
        }
    }

    List<T> asList();

    List<String> saveToList();

    void addCommand(T command);

    void removeCommand(RewardEntry command);

    List<String> rewardPlayer(PlayerData player, RewardContext random);

    String getModeName();

}