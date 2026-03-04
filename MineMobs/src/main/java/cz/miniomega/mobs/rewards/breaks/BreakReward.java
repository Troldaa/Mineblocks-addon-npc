package cz.miniomega.mobs.rewards.hits;

import cz.miniomega.mobs.playerdata.PlayerData;
import cz.miniomega.mobs.rewards.Reward;
import cz.miniomega.mobs.rewards.RewardType;
import cz.miniomega.mobs.rewards.hits.condition.BreakCondition;
import cz.miniomega.mobs.rewards.commands.RewardCommands;
import cz.miniomega.mobs.rewards.commands.RewardEntry;
import cz.miniomega.mobs.rewards.context.RewardContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BreakReward implements Reward {

    private final String name;
    private final BreakCondition condition;
    private final RewardCommands<? extends RewardEntry> commands;

    @Override
    public RewardType getType() {
        return RewardType.BREAK;
    }

    @Override
    public boolean canGet(PlayerData player, RewardContext context) {
        if (condition == null) return true;
        return condition.test(player, context);
    }

    @Override
    public boolean isLast() {
        if (condition == null) return false;
        return condition.isLast();
    }

}
