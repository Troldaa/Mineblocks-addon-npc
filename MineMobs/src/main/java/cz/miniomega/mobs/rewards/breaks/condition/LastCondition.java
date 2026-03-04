package cz.miniomega.mobs.rewards.hits.condition;

import cz.miniomega.mobs.playerdata.PlayerData;
import cz.miniomega.mobs.rewards.context.RewardContext;

public class LastCondition implements BreakCondition {

    @Override
    public boolean test(PlayerData player, RewardContext context) {
        return player.uuid.equals(context.getLastBreaker());
    }

    @Override
    public boolean isLast() {
        return true;
    }

    @Override
    public String toString() {
        return "last";
    }

}
