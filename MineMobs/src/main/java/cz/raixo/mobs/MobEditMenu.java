package cz.raixo.mobs;

import cz.raixo.blocks.gui.filler.map.MapGuiFiller;
import cz.raixo.blocks.gui.item.GuiItemBuilder;
import cz.raixo.blocks.gui.item.render.Renderer;
import cz.raixo.blocks.gui.itemstack.ItemStackBuilder;
import cz.raixo.blocks.gui.meta.GuiMeta;
import cz.raixo.blocks.gui.type.InventoryType;
import cz.raixo.blocks.util.NumberUtil;
import cz.raixo.blocks.util.color.Colors;
import cz.raixo.blocks.commands.MBCommand;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.List;

public class MobEditMenu {
    private final MineMob mob;
    private final MineMobsPlugin plugin;

    public MobEditMenu(MineMob mob) {
        this.mob = mob;
        this.plugin = mob.getPlugin();
    }

    public void open(Player player) {
        MapGuiFiller filler = new MapGuiFiller(
                "vvvvvvvvv",
                "vvvvhvvvv",
                "vrcvvstvv",
                "vvpvvxgvv",
                "vvvvvvvvv",
                "vvvvqvvvv"
        );

        GuiMeta<MapGuiFiller> meta = new GuiMeta<>(filler, Component.text("Edit Mob: " + mob.getId()), InventoryType.CHEST_6);
        cz.raixo.blocks.gui.Gui<MapGuiFiller> gui = new cz.raixo.blocks.gui.Gui<>(meta);
        filler = gui.getFiller();

        // v: Black Stained Glass Pane (Design)
        filler.setItem('v', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.BLACK_STAINED_GLASS_PANE).withName(Component.empty()).build()).build());

        // h: Bone Meal (Hologram Editor) - Slot 13
        filler.setItem('h', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.BONE_MEAL)
                .withName(MineDown.parse("&#FAEDCB&lHologram displaced"))
                .withLore(List.of(Component.empty(), Component.text("Here will be visible hologram lines"), Component.empty(), MineDown.parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    MBCommand.showHologram(plugin.getMineBlocks().getBukkitAudiences().player(player), "mm", mob.getId(), mob.getHologramLines());
                }).build());

        // r: Red Dye (Health) - Slot 19
        filler.setItem('r', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.RED_DYE)
                .withName(MineDown.parse("&#E53333&lHealth"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Current: &#E53333" + mob.getHealth().getMaxHealth()), Component.empty(), MineDown.parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    Colors.send(player, "&bEnter new health:");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player)
                            .thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> {
                                if (s != null) NumberUtil.parseInt(s).ifPresent(i -> { mob.getHealth().setMaxHealth(i); mob.getHealth().setHealth(i); plugin.getMobConfig().saveMobs(); });
                                open(player);
                            }));
                }).build());

        // c: Clock (Cooldown) - Slot 20
        filler.setItem('c', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.CLOCK)
                .withName(MineDown.parse("&#FFFC08&lDefeated Cooldown"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Current: &#FFFC08" + mob.getCooldownSeconds() + "s"), Component.empty(), MineDown.parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    Colors.send(player, "&bEnter cooldown in seconds:");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player)
                            .thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> {
                                if (s != null) NumberUtil.parseInt(s).ifPresent(i -> { mob.setCooldownSeconds(i); plugin.getMobConfig().saveMobs(); });
                                open(player);
                            }));
                }).build());

        // t: Spawner (Type) - Slot 24
        filler.setItem('t', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.SPAWNER)
                .withName(MineDown.parse("&#555555&lType of Mob"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Current: &a" + mob.getType().name()), Component.empty(), MineDown.parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    Colors.send(player, "&bEnter EntityType name:");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player)
                            .thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> {
                                if (s != null) try { mob.setType(org.bukkit.entity.EntityType.valueOf(s.toUpperCase())); mob.spawn(); plugin.getMobConfig().saveMobs(); } catch (Exception ignored) {}
                                open(player);
                            }));
                }).build());

        // p: Regen Potion (Regeneration) - Slot 29
        ItemStack regenPotion = new ItemStack(Material.POTION);
        PotionMeta pm = (PotionMeta) regenPotion.getItemMeta();
        if (pm != null) {
            pm.setBasePotionType(PotionType.REGENERATION);
            regenPotion.setItemMeta(pm);
        }
        filler.setItem('p', new GuiItemBuilder<>(filler, new ItemStackBuilder(regenPotion)
                .withName(MineDown.parse("&#9955FE&lRegeneration cooldown"))
                .withLore(List.of(Component.empty(), Component.text("Time after mob starts"), Component.text("regenerating"), Component.empty(), MineDown.parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    Colors.send(player, "&bEnter regen idle seconds:");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player)
                            .thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> {
                                if (s != null) NumberUtil.parseInt(s).ifPresent(i -> { mob.setRegenerationIdleSeconds(i); plugin.getMobConfig().saveMobs(); });
                                open(player);
                            }));
                }).build());

        // x: XP Bottle (Particles) - Slot 32
        filler.setItem('x', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.EXPERIENCE_BOTTLE)
                .withName(MineDown.parse("&#C3A0F7&lParticles & Effects"))
                .withLore(List.of(Component.empty(), Component.text("The effect/particles on defeat the mob"), Component.text("and more!"), Component.empty(), MineDown.parse("&7Click to enter")))
                .build())
                .withClickHandler(e -> openParticlesPage1(player)).build());

        // g: Glow Ink Sac (Glowing) - Slot 33
        filler.setItem('g', new GuiItemBuilder<>(filler, (Renderer<Integer>) (slot, state) -> {
            String modeName = state == 0 ? "&cdenied" : (state == 1 ? "&awhite" : "&ared");
            return ItemStackBuilder.create(Material.GLOW_INK_SAC)
                .withName(MineDown.parse("&#89DBF7&lGlowing while defeated"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Current: " + modeName), Component.empty(), MineDown.parse("&7Click to cycle (None -> White -> Red)")))
                .build();
        })
                .withDefaultState(mob.getGlowMode())
                .withClickHandler(e -> {
                    int nextMode = (mob.getGlowMode() + 1) % 3;
                    mob.setGlowMode(nextMode);
                    e.getGuiItem().setState(nextMode);
                    plugin.getMobConfig().saveMobs();
                }).build());

        // s: Resin (Scale) - Slot 23
        Material resinMaterial;
        try {
            resinMaterial = Material.valueOf("RESIN_CLUMP");
        } catch (Exception e) {
            resinMaterial = Material.CLAY_BALL;
        }
        filler.setItem('s', new GuiItemBuilder<>(filler, ItemStackBuilder.create(resinMaterial)
                .withName(MineDown.parse("&#FFA44A&lSize of mob"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Current size: &#FFA44A" + mob.getMobScale()), Component.empty(), MineDown.parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    Colors.send(player, "&bEnter new scale (e.g. 1.0, 2.0, 0.5):");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player)
                            .thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> {
                                if (s != null) try { mob.setMobScale(Double.parseDouble(s)); mob.spawn(); plugin.getMobConfig().saveMobs(); } catch (Exception ignored) {}
                                open(player);
                            }));
                }).build());

        // q: Barrier (Close) - Slot 49
        filler.setItem('q', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.BARRIER).withName(MineDown.parse("&cClose")).build())
                .withClickHandler(e -> player.closeInventory()).build());

        gui.open(player);
    }

    private void openParticlesPage1(Player player) {
        MapGuiFiller filler = new MapGuiFiller(
                "vvvvvvvvv",
                "vvvvvvvvv",
                "v r l c v",
                "v h m e f",
                "vvvvvvvvv",
                "vvvvb nvv"
        );
        GuiMeta<MapGuiFiller> meta = new GuiMeta<>(filler, Component.text("Effects Page 1: " + mob.getId()), InventoryType.CHEST_6);
        cz.raixo.blocks.gui.Gui<MapGuiFiller> gui = new cz.raixo.blocks.gui.Gui<>(meta);
        filler = gui.getFiller();

        // v: Black Stained Glass Pane (Design)
        filler.setItem('v', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.BLACK_STAINED_GLASS_PANE).withName(Component.empty()).build()).build());

        // r: Firework Rocket (Toggle)
        filler.setItem('r', new GuiItemBuilder<>(filler, (Renderer<Boolean>) (slot, state) -> ItemStackBuilder.create(Material.FIREWORK_ROCKET)
                .withName(MineDown.parse("&#205295&&lFirework Effect"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Status: " + (state ? "&2ON" : "&4OFF"))))
                .build())
                .withDefaultState(true) // Always on for now as requested
                .build());

        // h: Firework Ball (Height)
        filler.setItem('h', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.FIREWORK_STAR)
                .withName(MineDown.parse("&#205295&&lFirework Height"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Current: &#2C74B3&" + mob.getFireworkHeight()), Component.empty(), MineDown.parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory(); Colors.send(player, "&bEnter height:");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player).thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> {
                        if (s != null) NumberUtil.parseInt(s).ifPresent(i -> { mob.setFireworkHeight(i); plugin.getMobConfig().saveMobs(); });
                        openParticlesPage1(player);
                    }));
                }).build());

        // l: Fishing Rod (Launch Mode Toggle)
        filler.setItem('l', new GuiItemBuilder<>(filler, (Renderer<Boolean>) (slot, state) -> ItemStackBuilder.create(Material.FISHING_ROD)
                .withName(MineDown.parse("&#205295&&lLaunch Mode"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Status: " + (state ? "&2ON" : "&4OFF"))))
                .build())
                .withDefaultState(mob.isLaunchMode())
                .withClickHandler(e -> { mob.setLaunchMode(!mob.isLaunchMode()); e.getGuiItem().setState(mob.isLaunchMode()); plugin.getMobConfig().saveMobs(); })
                .build());

        // m: Slimeball (Chance)
        filler.setItem('m', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.SLIME_BALL)
                .withName(MineDown.parse("&#205295&&lLaunch Chance"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Current: &#2C74B3&" + mob.getLaunchChance() + "%"), Component.empty(), MineDown.parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory(); Colors.send(player, "&bEnter chance (0-100):");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player).thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> {
                        if (s != null) NumberUtil.parseInt(s).ifPresent(i -> { mob.setLaunchChance(i); plugin.getMobConfig().saveMobs(); });
                        openParticlesPage1(player);
                    }));
                }).build());

        // e: Turtle Shell (Launch Range)
        filler.setItem('e', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.TURTLE_HELMET)
                .withName(MineDown.parse("&#205295&&lLaunch Range"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Current: &#2C74B3&" + mob.getLaunchRange()), Component.empty(), MineDown.parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory(); Colors.send(player, "&bEnter range:");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player).thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> {
                        if (s != null) try { mob.setLaunchRange(Double.parseDouble(s)); plugin.getMobConfig().saveMobs(); } catch (Exception ignored) {}
                        openParticlesPage1(player);
                    }));
                }).build());

        // c: Feather (Chicken Toggle)
        filler.setItem('c', new GuiItemBuilder<>(filler, (Renderer<Boolean>) (slot, state) -> ItemStackBuilder.create(Material.FEATHER)
                .withName(MineDown.parse("&#205295&&lChicken Cannon"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Status: " + (state ? "&2ON" : "&4OFF"))))
                .build())
                .withDefaultState(mob.isChickenLauncherEffect())
                .withClickHandler(e -> { mob.setChickenLauncherEffect(!mob.isChickenLauncherEffect()); e.getGuiItem().setState(mob.isChickenLauncherEffect()); plugin.getMobConfig().saveMobs(); })
                .build());

        // next slot/below feather: Egg (Chicken Power)
        filler.setItem('f', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.EGG)
                .withName(MineDown.parse("&#205295&&lChicken Power"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Current: &#2C74B3&" + mob.getChickenLauncherRange()), Component.empty(), MineDown.parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory(); Colors.send(player, "&bEnter chicken power:");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player).thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> {
                        if (s != null) try { mob.setChickenLauncherRange(Double.parseDouble(s)); plugin.getMobConfig().saveMobs(); } catch (Exception ignored) {}
                        openParticlesPage1(player);
                    }));
                }).build());

        // n: Next Page
        filler.setItem('n', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.TIPPED_ARROW).withName(MineDown.parse("&6Next Page")).build()).withClickHandler(e -> openParticlesPage2(player)).build());

        // b: Back to Main
        filler.setItem('b', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.ARROW).withName(MineDown.parse("&7Back")).build()).withClickHandler(e -> open(player)).build());

        gui.open(player);
    }

    private void openParticlesPage2(Player player) {
        MapGuiFiller filler = new MapGuiFiller(
                "vvvvvvvvv",
                "vvvvvvvvv",
                "v t m vvv",
                "vvvvvvvvv",
                "vvvvvvvvv",
                "vvvvb vvv"
        );
        GuiMeta<MapGuiFiller> meta = new GuiMeta<>(filler, Component.text("Effects Page 2: " + mob.getId()), InventoryType.CHEST_6);
        cz.raixo.blocks.gui.Gui<MapGuiFiller> gui = new cz.raixo.blocks.gui.Gui<>(meta);
        filler = gui.getFiller();

        // v: Black Stained Glass Pane (Design)
        filler.setItem('v', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.BLACK_STAINED_GLASS_PANE).withName(Component.empty()).build()).build());

        // t: TNT (Toggle)
        filler.setItem('t', new GuiItemBuilder<>(filler, (Renderer<Boolean>) (slot, state) -> ItemStackBuilder.create(Material.TNT)
                .withName(MineDown.parse("&#205295&&lTNT Cannon"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Status: " + (state ? "&2ON" : "&4OFF"))))
                .build())
                .withDefaultState(mob.isTntCannonEffect())
                .withClickHandler(e -> { mob.setTntCannonEffect(!mob.isTntCannonEffect()); e.getGuiItem().setState(mob.isTntCannonEffect()); plugin.getMobConfig().saveMobs(); })
                .build());

        // m: TNT Minecart (Count)
        filler.setItem('m', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.TNT_MINECART)
                .withName(MineDown.parse("&#205295&&lTNT Count"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Current: &#2C74B3&" + mob.getTntCannonCount()), Component.empty(), MineDown.parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory(); Colors.send(player, "&bEnter TNT count:");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player).thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> {
                        if (s != null) NumberUtil.parseInt(s).ifPresent(i -> { mob.setTntCannonCount(i); plugin.getMobConfig().saveMobs(); });
                        openParticlesPage2(player);
                    }));
                }).build());

        // b: Back
        filler.setItem('b', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.ARROW).withName(MineDown.parse("&7Back")).build()).withClickHandler(e -> openParticlesPage1(player)).build());

        gui.open(player);
    }
}
