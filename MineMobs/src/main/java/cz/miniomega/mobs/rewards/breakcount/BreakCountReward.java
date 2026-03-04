package cz.miniomega.mobs.rewards.breakcount;

import cz.miniomega.mobs.playerdata.PlayerData;
import cz.miniomega.mobs.rewards.Reward;
import cz.miniomega.mobs.rewards.RewardType;
import cz.miniomega.mobs.rewards.commands.RewardCommands;
import cz.miniomega.mobs.rewards.commands.RewardEntry;
import cz.miniomega.mobs.rewards.context.RewardContext;
import cz.miniomega.mobs.util.range.NumberRange;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BreakCountReward implements Reward {

    private final String name;
    private final NumberRange range;
    private final RewardCommands<? extends RewardEntry> commands;

    @Override
    public RewardType getType() {
        return RewardType.BREAK_COUNT;
    }

    @Override
    public boolean canGet(PlayerData player, RewardContext context) {
        return range.test(player.hits);
    }

    @Override
    public boolean isLast() {
        return true;
    }

}
