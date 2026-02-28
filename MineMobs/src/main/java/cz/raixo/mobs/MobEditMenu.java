package cz.raixo.mobs;

import cz.raixo.blocks.gui.filler.map.MapGuiFiller;
import cz.raixo.blocks.gui.item.GuiItemBuilder;
import cz.raixo.blocks.gui.item.render.Renderer;
import cz.raixo.blocks.gui.itemstack.ItemStackBuilder;
import cz.raixo.blocks.gui.meta.GuiMeta;
import cz.raixo.blocks.gui.type.InventoryType;
import cz.raixo.blocks.util.NumberUtil;
import cz.raixo.blocks.util.color.Colors;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.TimeoutException;

public class MobEditMenu {
    private final MineMob mob;
    private final MineMobsPlugin plugin;

    public MobEditMenu(MineMob mob) {
        this.mob = mob;
        this.plugin = mob.getPlugin();
    }

    public void open(Player player) {
        // Since I cannot easily extend BlockMenu due to generic constraints and project structure,
        // I will implement a simpler version or try to use MineBlocks' Gui system.

        // Actually, let's try to use the raw Gui class from MineBlocks if possible.
        // But for now, a simple chest menu for speed.

        // REVISION: I'll use the existing MineBlocks GUI framework.
        MapGuiFiller filler = new MapGuiFiller(
                "1 2 3 4 5",
                "         ",
                " L R S F ",
                " T G C   ",
                "         ",
                "    X    "
        );

        GuiMeta<MapGuiFiller> meta = new GuiMeta<>(filler, Component.text("Edit Mob: " + mob.getId()), InventoryType.CHEST_6);
        cz.raixo.blocks.gui.Gui<MapGuiFiller> gui = new cz.raixo.blocks.gui.Gui<>(meta);

        // Health
        filler.setItem('1', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.APPLE)
                .withName(MineDown.parse("&cHealth: " + mob.getHealth().getMaxHealth()))
                .withLore(List.of(Component.text("Click to edit health")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    Colors.send(player, "&bEnter new health in chat:");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player)
                            .exceptionally(t -> {
                                if (t instanceof TimeoutException) Colors.send(player, "&cTimeout!");
                                return null;
                            })
                            .thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> {
                                open(player);
                                if (s == null) return;
                                NumberUtil.parseInt(s).ifPresent(i -> {
                                    mob.getHealth().setMaxHealth(i);
                                    plugin.getMobConfig().saveMobs();
                                });
                            }));
                }).build());

        // Glowing Red
        filler.setItem('G', new GuiItemBuilder<>(filler, (Renderer<Boolean>) (slot, state) -> ItemStackBuilder.create(Material.RED_DYE)
                .withName(MineDown.parse("&cGlowing Red: " + (state ? "&2ON" : "&4OFF")))
                .build())
                .withDefaultState(mob.isGlowingRed())
                .withClickHandler(e -> {
                    mob.setGlowingRed(!mob.isGlowingRed());
                    e.getGuiItem().setState(mob.isGlowingRed());
                    plugin.getMobConfig().saveMobs();
                }).build());

        // Cooldown
        filler.setItem('2', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.CLOCK)
                .withName(MineDown.parse("&eCooldown: " + mob.getCooldownSeconds() + "s"))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    Colors.send(player, "&bEnter new cooldown in seconds:");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player)
                            .exceptionally(t -> null)
                            .thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> {
                                open(player);
                                if (s == null) return;
                                NumberUtil.parseInt(s).ifPresent(i -> {
                                    mob.setCooldownSeconds(i);
                                    plugin.getMobConfig().saveMobs();
                                });
                            }));
                }).build());

        // Launch Mode
        filler.setItem('L', new GuiItemBuilder<>(filler, (Renderer<Boolean>) (slot, state) -> ItemStackBuilder.create(Material.SLIME_BALL)
                .withName(MineDown.parse("&aLaunch Mode: " + (state ? "&2ON" : "&4OFF")))
                .build())
                .withDefaultState(mob.isLaunchMode())
                .withClickHandler(e -> {
                    mob.setLaunchMode(!mob.isLaunchMode());
                    e.getGuiItem().setState(mob.isLaunchMode());
                    plugin.getMobConfig().saveMobs();
                }).build());

        // TNT Cannon
        filler.setItem('T', new GuiItemBuilder<>(filler, (Renderer<Boolean>) (slot, state) -> ItemStackBuilder.create(Material.TNT)
                .withName(MineDown.parse("&cTNT Effect: " + (state ? "&2ON" : "&4OFF")))
                .build())
                .withDefaultState(mob.isTntCannonEffect())
                .withClickHandler(e -> {
                    mob.setTntCannonEffect(!mob.isTntCannonEffect());
                    e.getGuiItem().setState(mob.isTntCannonEffect());
                    plugin.getMobConfig().saveMobs();
                }).build());

        // Launch Range
        filler.setItem('R', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.FISHING_ROD)
                .withName(MineDown.parse("&bLaunch Range: " + mob.getLaunchRange()))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    Colors.send(player, "&bEnter new launch range:");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player)
                            .thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> {
                                open(player);
                                if (s == null) return;
                                try { mob.setLaunchRange(Double.parseDouble(s)); plugin.getMobConfig().saveMobs(); } catch (Exception ignored) {}
                            }));
                }).build());

        // Firework Height
        filler.setItem('F', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.FIREWORK_ROCKET)
                .withName(MineDown.parse("&bFirework Height: " + mob.getFireworkHeight()))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    Colors.send(player, "&bEnter firework height:");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player)
                            .thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> {
                                open(player);
                                if (s == null) return;
                                NumberUtil.parseInt(s).ifPresent(i -> {
                                    mob.setFireworkHeight(i);
                                    plugin.getMobConfig().saveMobs();
                                });
                            }));
                }).build());

        // Close
        filler.setItem('X', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.BARRIER)
                .withName(MineDown.parse("&cClose"))
                .build())
                .withClickHandler(e -> player.closeInventory()).build());

        gui.open(player);
    }
}
