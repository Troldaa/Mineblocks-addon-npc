package cz.miniomega.mobs.rewards.commands.random;

import cz.miniomega.mobs.rewards.commands.RewardEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class RandomCommandEntry implements RewardEntry {

    private final String command;
    private int chance;

}
