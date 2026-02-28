package cz.raixo.mobs;

import cz.raixo.blocks.gui.filler.map.MapGuiFiller;
import cz.raixo.blocks.gui.item.GuiItem;
import cz.raixo.blocks.gui.item.GuiItemBuilder;
import cz.raixo.blocks.gui.item.render.Renderer;
import cz.raixo.blocks.gui.itemstack.ItemStackBuilder;
import cz.raixo.blocks.gui.meta.GuiMeta;
import cz.raixo.blocks.gui.type.InventoryType;
import cz.raixo.blocks.util.color.Colors;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

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
                .build()).build());

        // Cooldown
        filler.setItem('2', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.CLOCK)
                .withName(MineDown.parse("&eCooldown: " + mob.getCooldownSeconds() + "s"))
                .build()).build());

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

        // Close
        filler.setItem('X', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.BARRIER)
                .withName(MineDown.parse("&cClose"))
                .build())
                .withClickHandler(e -> player.closeInventory()).build());

        gui.open(player);
    }
}
