package cz.miniomega.mobs.rewards.context;

import cz.miniomega.mobs.MineMobsPlugin;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class RewardContext {

    public final MineMobsPlugin plugin;
    private final Random random;
    @Getter(AccessLevel.NONE)
    private final Map<UUID, Integer> positions;
    private final UUID lastBreaker;

    public int getPosition(UUID id) {
        return positions.getOrDefault(id, -1);
    }

}
