package cz.miniomega.mobs.rewards.hits.condition;

import cz.miniomega.mobs.playerdata.PlayerData;
import cz.miniomega.mobs.rewards.context.RewardContext;
import cz.miniomega.mobs.util.NumberUtil;

public class IntervalCondition implements BreakCondition {

    private int value;

    public IntervalCondition(String value) {
        this.value = NumberUtil.parseInt(value)
                .orElseThrow(() -> new IllegalArgumentException("Invalid break interval: " + value));
    }

    public IntervalCondition(int value) {
        this.value = value;
    }

    @Override
    public boolean test(PlayerData player, RewardContext context) {
        int count = player.hits;
        return count % value == 0 && count >= value;
    }

    @Override
    public boolean isLast() {
        return false;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
