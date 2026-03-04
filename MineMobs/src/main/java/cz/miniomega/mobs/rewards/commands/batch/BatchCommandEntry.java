package cz.miniomega.mobs.rewards.commands.batch;

import cz.miniomega.mobs.rewards.commands.RewardEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BatchCommandEntry implements RewardEntry {

    private final String command;

}