package cz.miniomega.mobs.rewards.top;

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
public class TopReward implements Reward {

    private final String name;
    private final NumberRange range;
    private final RewardCommands<? extends RewardEntry> commands;

    @Override
    public RewardType getType() {
        return RewardType.TOP;
    }

    @Override
    public boolean canGet(PlayerData player, RewardContext context) {
        return range.test(context.getPosition(player.uuid));
    }

    @Override
    public boolean isLast() {
        return true;
    }

}
