package cz.miniomega.mobs.rewards.hits.condition;

import cz.miniomega.mobs.playerdata.PlayerData;
import cz.miniomega.mobs.rewards.context.RewardContext;
import cz.miniomega.mobs.util.range.NumberRange;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RangeCondition implements BreakCondition {

    private final NumberRange range;

    @Override
    public boolean test(PlayerData player, RewardContext context) {
        return range.test(player.hits);
    }

    @Override
    public boolean isLast() {
        return false;
    }

    @Override
    public String toString() {
        return range.toString();
    }
}
