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
        MapGuiFiller filler = new MapGuiFiller(
                "         ",
                " a b c d ",
                "  e f g  ",
                " h i j k ",
                "         ",
                "l   x   r"
        );

        GuiMeta<MapGuiFiller> meta = new GuiMeta<>(filler, Component.text("Edit Mob: " + mob.getId()), InventoryType.CHEST_6);
        cz.raixo.blocks.gui.Gui<MapGuiFiller> gui = new cz.raixo.blocks.gui.Gui<>(meta);
        filler = gui.getFiller();

        // Basic Settings
        filler.setItem('a', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.APPLE)
                .withName(MineDown.parse("&#205295&&lHealth"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Current: &#2C74B3&" + mob.getHealth().getMaxHealth()), Component.empty(), MineDown.parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    Colors.send(player, "&bEnter new health in chat:");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player)
                            .thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> {
                                open(player);
                                if (s == null) return;
                                NumberUtil.parseInt(s).ifPresent(i -> {
                                    mob.getHealth().setMaxHealth(i);
                                    plugin.getMobConfig().saveMobs();
                                });
                            }));
                }).build());

        filler.setItem('b', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.CLOCK)
                .withName(MineDown.parse("&#205295&&lCooldown"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Current: &#2C74B3&" + mob.getCooldownSeconds() + "s"), Component.empty(), MineDown.parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    Colors.send(player, "&bEnter new cooldown in seconds:");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player)
                            .thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> {
                                open(player);
                                if (s == null) return;
                                NumberUtil.parseInt(s).ifPresent(i -> {
                                    mob.setCooldownSeconds(i);
                                    plugin.getMobConfig().saveMobs();
                                });
                            }));
                }).build());

        filler.setItem('c', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.POTION)
                .withName(MineDown.parse("&#205295&&lRegen Idle"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Current: &#2C74B3&" + mob.getRegenerationIdleSeconds() + "s"), Component.empty(), MineDown.parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    Colors.send(player, "&bEnter new regen idle seconds:");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player)
                            .thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> {
                                open(player);
                                if (s == null) return;
                                NumberUtil.parseInt(s).ifPresent(i -> {
                                    mob.setRegenerationIdleSeconds(i);
                                    plugin.getMobConfig().saveMobs();
                                });
                            }));
                }).build());

        filler.setItem('d', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.PLAYER_HEAD)
                .withName(MineDown.parse("&#205295&&lScale"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Current: &#2C74B3&" + mob.getMobScale()), Component.empty(), MineDown.parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    Colors.send(player, "&bEnter new scale (e.g. 1.5):");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player)
                            .thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> {
                                open(player);
                                if (s == null) return;
                                try {
                                    mob.setMobScale(Double.parseDouble(s));
                                    mob.spawn();
                                    plugin.getMobConfig().saveMobs();
                                } catch (Exception ignored) {}
                            }));
                }).build());

        // Combat Settings
        filler.setItem('e', new GuiItemBuilder<>(filler, (Renderer<Boolean>) (slot, state) -> ItemStackBuilder.create(Material.SLIME_BALL)
                .withName(MineDown.parse("&#205295&&lLaunch Mode"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Status: " + (state ? "&2ON" : "&4OFF")), MineDown.parse("&7Chance: &#2C74B3&" + mob.getLaunchChance() + "%"), MineDown.parse("&7Range: &#2C74B3&" + mob.getLaunchRange()), Component.empty(), MineDown.parse("&7L-Click to toggle"), MineDown.parse("&7R-Click to edit chance")))
                .build())
                .withDefaultState(mob.isLaunchMode())
                .withClickHandler(e -> {
                    if (e.getType().isLeftClick()) {
                        mob.setLaunchMode(!mob.isLaunchMode());
                        e.getGuiItem().setState(mob.isLaunchMode());
                    } else if (e.getType().isRightClick()) {
                        player.closeInventory();
                        Colors.send(player, "&bEnter new launch chance (0-100):");
                        plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player)
                                .thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> {
                                    open(player);
                                    if (s == null) return;
                                    NumberUtil.parseInt(s).ifPresent(i -> mob.setLaunchChance(i));
                                    plugin.getMobConfig().saveMobs();
                                }));
                    }
                    plugin.getMobConfig().saveMobs();
                }).build());

        filler.setItem('f', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.FISHING_ROD)
                .withName(MineDown.parse("&#205295&&lLaunch Range"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Current: &#2C74B3&" + mob.getLaunchRange()), Component.empty(), MineDown.parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    Colors.send(player, "&bEnter new range:");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player)
                            .thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> {
                                open(player);
                                if (s == null) return;
                                try { mob.setLaunchRange(Double.parseDouble(s)); plugin.getMobConfig().saveMobs(); } catch (Exception ignored) {}
                            }));
                }).build());

        // Effects Category
        filler.setItem('g', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.NETHER_STAR)
                .withName(MineDown.parse("&#205295&&lEffects & Particles"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7TNT: " + (mob.isTntCannonEffect() ? "&2ON" : "&4OFF")), MineDown.parse("&7Chicken: " + (mob.isChickenLauncherEffect() ? "&2ON" : "&4OFF")), MineDown.parse("&7Glow: " + (mob.isGlowingRed() ? "&2ON" : "&4OFF")), Component.empty(), MineDown.parse("&7Click to edit effects")))
                .build())
                .withClickHandler(e -> openEffectsMenu(player)).build());

        // Misc
        filler.setItem('i', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.SPAWNER)
                .withName(MineDown.parse("&#205295&&lEntity Type"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Current: &#2C74B3&" + mob.getType().name()), Component.empty(), MineDown.parse("&7Click to change")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    Colors.send(player, "&bEnter new EntityType:");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player)
                            .thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> {
                                open(player);
                                if (s == null) return;
                                try { mob.setType(org.bukkit.entity.EntityType.valueOf(s.toUpperCase())); mob.spawn(); plugin.getMobConfig().saveMobs(); } catch (Exception ignored) {}
                            }));
                }).build());

        // Navigation
        filler.setItem('x', new GuiItemBuilder<>(filler, (Renderer<Boolean>) (slot, state) -> {
            if (Boolean.TRUE.equals(state)) return ItemStackBuilder.create(Material.RED_TERRACOTTA).withName(MineDown.parse("&c&lCONFIRM DELETE")).build();
            return ItemStackBuilder.create(Material.LAVA_BUCKET).withName(MineDown.parse("&4&lDELETE MOB")).build();
        })
                .withDefaultState(false)
                .withClickHandler(e -> {
                    if (Boolean.TRUE.equals(e.getGuiItem().getState())) { plugin.getMobRegistry().unregister(mob); player.closeInventory(); }
                    else e.getGuiItem().setState(true);
                }).build());

        filler.setItem('r', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.BARRIER).withName(MineDown.parse("&cClose")).build())
                .withClickHandler(e -> player.closeInventory()).build());

        gui.open(player);
    }

    private void openEffectsMenu(Player player) {
        MapGuiFiller filler = new MapGuiFiller("         ", " T C G H ", "         ", "    B    ");
        GuiMeta<MapGuiFiller> meta = new GuiMeta<>(filler, Component.text("Effects: " + mob.getId()), InventoryType.CHEST_3);
        cz.raixo.blocks.gui.Gui<MapGuiFiller> gui = new cz.raixo.blocks.gui.Gui<>(meta);
        filler = gui.getFiller();

        filler.setItem('T', new GuiItemBuilder<>(filler, (Renderer<Boolean>) (slot, state) -> ItemStackBuilder.create(Material.TNT).withName(MineDown.parse("&cTNT Cannon: " + (state ? "&2ON" : "&4OFF"))).build())
                .withDefaultState(mob.isTntCannonEffect()).withClickHandler(e -> { mob.setTntCannonEffect(!mob.isTntCannonEffect()); e.getGuiItem().setState(mob.isTntCannonEffect()); plugin.getMobConfig().saveMobs(); }).build());

        filler.setItem('C', new GuiItemBuilder<>(filler, (Renderer<Boolean>) (slot, state) -> ItemStackBuilder.create(Material.FEATHER).withName(MineDown.parse("&eChicken Launcher: " + (state ? "&2ON" : "&4OFF"))).build())
                .withDefaultState(mob.isChickenLauncherEffect()).withClickHandler(e -> { mob.setChickenLauncherEffect(!mob.isChickenLauncherEffect()); e.getGuiItem().setState(mob.isChickenLauncherEffect()); plugin.getMobConfig().saveMobs(); }).build());

        filler.setItem('G', new GuiItemBuilder<>(filler, (Renderer<Boolean>) (slot, state) -> ItemStackBuilder.create(Material.RED_DYE).withName(MineDown.parse("&cGlowing Red: " + (state ? "&2ON" : "&4OFF"))).build())
                .withDefaultState(mob.isGlowingRed()).withClickHandler(e -> { mob.setGlowingRed(!mob.isGlowingRed()); e.getGuiItem().setState(mob.isGlowingRed()); plugin.getMobConfig().saveMobs(); }).build());

        filler.setItem('H', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.FIREWORK_ROCKET).withName(MineDown.parse("&bFirework Height: " + mob.getFireworkHeight())).build())
                .withClickHandler(e -> {
                    player.closeInventory(); Colors.send(player, "&bEnter height:");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player).thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> { openEffectsMenu(player); if (s != null) NumberUtil.parseInt(s).ifPresent(i -> { mob.setFireworkHeight(i); plugin.getMobConfig().saveMobs(); }); }));
                }).build());

        filler.setItem('B', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.ARROW).withName(MineDown.parse("&7Back")).build()).withClickHandler(e -> open(player)).build());

        gui.open(player);
    }
}
