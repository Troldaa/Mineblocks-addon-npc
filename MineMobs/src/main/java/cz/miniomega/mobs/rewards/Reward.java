package cz.miniomega.mobs.rewards;

import cz.miniomega.mobs.playerdata.PlayerData;
import cz.miniomega.mobs.rewards.commands.RewardCommands;
import cz.miniomega.mobs.rewards.commands.RewardEntry;
import cz.miniomega.mobs.rewards.context.RewardContext;
import lombok.SneakyThrows;
import org.bukkit.configuration.ConfigurationSection;

import java.util.function.Supplier;

public interface Reward {

    @SneakyThrows
    static Reward parse(ConfigurationSection section) {
        RewardType type = RewardType.getByName(section.getString("type", "null"))
                .orElseThrow((Supplier<Throwable>) () -> new IllegalArgumentException("Invalid reward type for reward named " + section.getName()));
        return type.parse(section.getName(), section);
    }

    static void save(ConfigurationSection section, Reward reward) {
        RewardType type = reward.getType();
        section.set("type", type.name().toLowerCase());
        section.set("mode", reward.getCommands().getModeName());
        type.set(section, reward);
    }

    String getName();
    RewardType getType();
    /**
     * @param context If the mob is not fully defeated, partial reward context is supplied
     * */
    boolean canGet(PlayerData player, RewardContext context);
    boolean isLast();
    RewardCommands<? extends RewardEntry> getCommands();

}
