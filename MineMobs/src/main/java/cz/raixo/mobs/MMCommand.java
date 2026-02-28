package cz.raixo.mobs;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import cz.raixo.blocks.block.health.BlockHealth;
import cz.raixo.blocks.block.messages.BlockMessages;
import cz.raixo.blocks.block.rewards.BlockRewards;
import cz.raixo.blocks.util.color.Colors;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@CommandAlias("mm|minemobs")
@CommandPermission("mm.admin")
public class MMCommand extends BaseCommand {
    private final MineMobsPlugin plugin;

    public MMCommand(MineMobsPlugin plugin) {
        this.plugin = plugin;
        plugin.getCommandManager().getCommandCompletions().registerCompletion("mobids", c -> {
            List<String> ids = new LinkedList<>();
            for (MineMob mob : plugin.getMobRegistry().getMobs()) {
                ids.add(mob.getId());
            }
            return ids;
        });

        plugin.getCommandManager().getCommandCompletions().registerCompletion("usablemobs", c -> {
            List<String> types = new ArrayList<>();
            for (EntityType type : EntityType.values()) {
                if (type.isAlive() && type.isSpawnable()) {
                    types.add(type.name());
                }
            }
            return types;
        });
    }

    @Subcommand("create")
    @Syntax("<id> <type>")
    @CommandCompletion("@nothing @usablemobs")
    public void create(Player player, String id, EntityType type) {
        if (plugin.getMobRegistry().getById(id) != null) {
            player.sendMessage(Colors.colorize("&cMob with that ID already exists!"));
            return;
        }

        MineMob mob = new MineMob(plugin);
        mob.setId(id);
        mob.setType(type);
        mob.setLocation(player.getLocation());
        mob.setHealth(new BlockHealth(null, 10)); // Default 10 health
        // Default example rewards
        List<cz.raixo.blocks.block.rewards.Reward> exampleRewards = new LinkedList<>();
        exampleRewards.add(new cz.raixo.blocks.block.rewards.top.TopReward(
                "reward_1",
                cz.raixo.blocks.util.range.NumberRange.parse("1").get(),
                cz.raixo.blocks.block.rewards.commands.RewardCommands.parse("RANDOM", List.of("100;say %player% is first!"))
        ));
        exampleRewards.add(new cz.raixo.blocks.block.rewards.top.TopReward(
                "reward_2",
                cz.raixo.blocks.util.range.NumberRange.parse("2").get(),
                cz.raixo.blocks.block.rewards.commands.RewardCommands.parse("RANDOM", List.of("100;say %player% is second!"))
        ));
        exampleRewards.add(new cz.raixo.blocks.block.rewards.top.TopReward(
                "reward_3",
                cz.raixo.blocks.util.range.NumberRange.parse("3").get(),
                cz.raixo.blocks.block.rewards.commands.RewardCommands.parse("RANDOM", List.of("100;say %player% is third!"))
        ));

        mob.setRewards(new BlockRewards(plugin.getMineBlocks(), null, new LinkedList<>(), exampleRewards));
        mob.setMessages(new BlockMessages("&aMob %player% was defeated!"));
        mob.setHologramLines(new LinkedList<>(List.of(
                "&b&l%name%",
                "&7Health: &c%health%/%max_health%",
                "&b&lTOP HITS",
                "&71. %player_name_1% &8- &b%playerhits_1%",
                "&72. %player_name_2% &8- &b%playerhits_2%",
                "&73. %player_name_3% &8- &b%playerhits_3%",
                "&cRespawn in %cooldown%s"
        )));

        plugin.getMobRegistry().register(mob);
        plugin.getMobConfig().saveMobs();
        player.sendMessage(Colors.colorize("&aMineMob " + id + " created!"));
    }

    @Subcommand("list")
    public void list(Player player) {
        player.sendMessage(Colors.colorize("&bList of MineMobs:"));
        for (MineMob mob : plugin.getMobRegistry().getMobs()) {
            player.sendMessage(Colors.colorize("&7 - &b" + mob.getId() + " &7(" + mob.getType().name() + ")"));
        }
    }

    @Subcommand("delete")
    @Syntax("<id>")
    @CommandCompletion("@mobids")
    public void delete(Player player, String id) {
        MineMob mob = plugin.getMobRegistry().getById(id);
        if (mob == null) {
            player.sendMessage(Colors.colorize("&cMob with that ID does not exist!"));
            return;
        }

        plugin.getMobRegistry().unregister(mob);
        player.sendMessage(Colors.colorize("&aMineMob " + id + " deleted!"));
    }

    @Subcommand("reload")
    public void reload(Player player) {
        plugin.reload();
        player.sendMessage(Colors.colorize("&aMineMobs reloaded!"));
    }

    @Subcommand("edit")
    @Syntax("<id>")
    @CommandCompletion("@mobids")
    public void edit(Player player, String id) {
        MineMob mob = plugin.getMobRegistry().getById(id);
        if (mob == null) {
            player.sendMessage(Colors.colorize("&cMob with that ID does not exist!"));
            return;
        }

        new MobEditMenu(mob).open(player);
    }
}
