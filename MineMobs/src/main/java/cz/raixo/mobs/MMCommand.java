package cz.raixo.mobs;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import cz.raixo.blocks.commands.MBCommand;
import cz.raixo.blocks.block.health.BlockHealth;
import cz.raixo.blocks.block.messages.BlockMessages;
import cz.raixo.blocks.block.rewards.BlockRewards;
import cz.raixo.blocks.util.color.Colors;
import org.bukkit.Location;
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

        mob.setRewards(new BlockRewards(plugin.getMineBlocks(), new LinkedList<>(), exampleRewards));
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

    @Subcommand("hologram show")
    @Syntax("<id>")
    @CommandCompletion("@mobids")
    public void showHologram(Player player, String id) {
        MineMob mob = plugin.getMobRegistry().getById(id);
        if (mob == null) {
            player.sendMessage(Colors.colorize("&cMob with that ID does not exist!"));
            return;
        }

        MBCommand.showHologram(plugin.getMineBlocks().getBukkitAudiences().player(player), "mm", mob.getId(), mob.getHologramLines());
    }

    @Subcommand("hologram setline")
    @Syntax("<id> <line number> <text>")
    @CommandCompletion("@mobids")
    public void setHologramLine(Player player, String id, int line, String text) {
        MineMob mob = plugin.getMobRegistry().getById(id);
        if (mob == null) {
            player.sendMessage(Colors.colorize("&cMob with that ID does not exist!"));
            return;
        }

        List<String> lines = mob.getHologramLines();
        if (line < 1 || line > lines.size()) {
            player.sendMessage(Colors.colorize("&cInvalid line number!"));
            return;
        }

        lines.set(line - 1, text);
        mob.updateHologram();
        plugin.getMobConfig().saveMobs();
        MBCommand.showHologram(plugin.getMineBlocks().getBukkitAudiences().player(player), "mm", mob.getId(), mob.getHologramLines());
    }

    @Subcommand("hologram removeline")
    @Syntax("<id> <line number>")
    @CommandCompletion("@mobids")
    public void removeHologramLine(Player player, String id, int line) {
        MineMob mob = plugin.getMobRegistry().getById(id);
        if (mob == null) {
            player.sendMessage(Colors.colorize("&cMob with that ID does not exist!"));
            return;
        }

        List<String> lines = mob.getHologramLines();
        if (line < 1 || line > lines.size()) {
            player.sendMessage(Colors.colorize("&cInvalid line number!"));
            return;
        }

        lines.remove(line - 1);
        mob.updateHologram();
        plugin.getMobConfig().saveMobs();
        MBCommand.showHologram(plugin.getMineBlocks().getBukkitAudiences().player(player), "mm", mob.getId(), mob.getHologramLines());
    }

    @Subcommand("hologram addline")
    @Syntax("<id> <text>")
    @CommandCompletion("@mobids")
    public void addHologramLine(Player player, String id, String text) {
        MineMob mob = plugin.getMobRegistry().getById(id);
        if (mob == null) {
            player.sendMessage(Colors.colorize("&cMob with that ID does not exist!"));
            return;
        }

        mob.getHologramLines().add(text);
        mob.updateHologram();
        plugin.getMobConfig().saveMobs();
        MBCommand.showHologram(plugin.getMineBlocks().getBukkitAudiences().player(player), "mm", mob.getId(), mob.getHologramLines());
    }

    @Subcommand("move")
    @Syntax("<id> [x y z]")
    @CommandCompletion("@mobids @nothing")
    public void move(Player player, String id, @Optional Double x, @Optional Double y, @Optional Double z) {
        MineMob mob = plugin.getMobRegistry().getById(id);
        if (mob == null) {
            player.sendMessage(Colors.colorize("&cMob with that ID does not exist!"));
            return;
        }

        Location newLoc;
        if (x != null && y != null && z != null) {
            newLoc = new Location(player.getWorld(), x, y, z);
        } else {
            newLoc = player.getLocation();
        }

        mob.setLocation(newLoc);
        mob.spawn(); // Re-spawns at new location
        plugin.getMobConfig().saveMobs();
        player.sendMessage(Colors.colorize("&aMineMob " + id + " moved!"));
    }
}
