package cz.miniomega.mobs;

import cz.miniomega.mobs.gui.Gui;
import cz.miniomega.mobs.gui.filler.map.MapGuiFiller;
import cz.miniomega.mobs.gui.item.GuiItem;
import cz.miniomega.mobs.gui.item.GuiItemBuilder;
import cz.miniomega.mobs.gui.item.render.Renderer;
import cz.miniomega.mobs.gui.itemstack.ItemStackBuilder;
import cz.miniomega.mobs.gui.meta.GuiMeta;
import cz.miniomega.mobs.gui.type.InventoryType;
import cz.miniomega.mobs.util.NumberUtil;
import cz.miniomega.mobs.util.color.Colors;
import cz.miniomega.mobs.commands.MMHologramCommand;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public class MobEditMenu {
    private final MineMob mob;
    public final MineMobsPlugin plugin;

    public MobEditMenu(MineMob mob) {
        this.mob = mob;
        this.plugin = mob.getPlugin();
    }

    private ItemStackBuilder createItem(Material mat) {
        return ItemStackBuilder.create(mat).addItemFlags(ItemFlag.values());
    }

    private Component parse(String s) {
        if (s == null) return Component.empty();
        // Handle #RRGGBB format by converting to &#RRGGBB for MineDown
        String processed = s.replaceAll("(?<!&)#([A-Fa-f0-9]{6})", "&#$1");
        return MineDown.parse(processed);
    }

    private void applySides(MapGuiFiller filler, int skipSlot) {
        ItemStack designItem = createItem(Material.BLACK_STAINED_GLASS_PANE).withName(Component.empty()).build();
        GuiItem<Object> guiItem = new GuiItemBuilder<>(filler, designItem).build();
        for (int i = 0; i < 6; i++) {
            int left = i * 9;
            int right = i * 9 + 8;
            if (left != skipSlot) filler.setItem(left, guiItem);
            if (right != skipSlot) filler.setItem(right, guiItem);
        }
    }

    public void open(Player player) {
        MapGuiFiller filler = new MapGuiFiller(
                "         ",
                "         ",
                "         ",
                "         ",
                "         ",
                "         "
        );

        GuiMeta<MapGuiFiller> meta = new GuiMeta<>(filler, Component.text("Edit Mob: " + mob.getId()), InventoryType.CHEST_6);
        cz.miniomega.mobs.gui.Gui<MapGuiFiller> gui = new cz.miniomega.mobs.gui.Gui<>(meta);
        filler = gui.getFiller();

        applySides(filler, 53); // Delete button at 53

        // Slot 13: Bone Meal (Hologram Editor)
        List<Component> holoLore = new ArrayList<>();
        holoLore.add(Component.empty());
        holoLore.add(Component.text("Here will be visible hologram lines"));
        holoLore.add(Component.empty());
        for (String line : mob.getHologramLines()) {
            holoLore.add(parse("&8- " + line));
        }
        holoLore.add(Component.empty());
        holoLore.add(parse("&7Click to edit"));

        filler.setItem(13, new GuiItemBuilder<>(filler, createItem(Material.BONE_MEAL)
                .withName(parse("#FAEDCB&lHologram displaced"))
                .withLore(holoLore)
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    MMHologramCommand.showHologram(plugin.integrationManager.getBukkitAudiences().player(player), "mm", mob.getId(), mob.getHologramLines());
                }).build());

        // Slot 19: Red Dye (Health)
        filler.setItem(19, new GuiItemBuilder<>(filler, createItem(Material.RED_DYE)
                .withName(parse("#E53333&lHealth"))
                .withLore(List.of(Component.empty(), parse("&7Current: #E53333" + mob.health.maxHealth), Component.empty(), parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    Colors.send(player, "&bEnter new health:");
                    plugin.getEditValuesListener().awaitChatInput(player)
                            .thenAccept(s -> cz.miniomega.mobs.gui.Gui.runSync(() -> {
                                if (s != null) NumberUtil.parseInt(s).ifPresent(i -> { mob.health.setMaxHealth(i); mob.health.setHealth(i); plugin.getMobConfig().saveMobs(); });
                                open(player);
                            }));
                }).build());

        // Slot 20: Clock (Cooldown)
        filler.setItem(20, new GuiItemBuilder<>(filler, createItem(Material.CLOCK)
                .withName(parse("#FFFC08&lDefeated Cooldown"))
                .withLore(List.of(Component.empty(), parse("&7Current: #FFFC08" + mob.cooldownSeconds + "s"), Component.empty(), parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    Colors.send(player, "&bEnter cooldown in seconds:");
                    plugin.getEditValuesListener().awaitChatInput(player)
                            .thenAccept(s -> cz.miniomega.mobs.gui.Gui.runSync(() -> {
                                if (s != null) NumberUtil.parseInt(s).ifPresent(i -> { mob.setCooldownSeconds(i); plugin.getMobConfig().saveMobs(); });
                                open(player);
                            }));
                }).build());

        // Slot 23: Resin (Scale)
        Material resinMaterial;
        try {
            resinMaterial = Material.valueOf("RESIN_CLUMP");
        } catch (Exception e) {
            resinMaterial = Material.CLAY_BALL;
        }
        filler.setItem(23, new GuiItemBuilder<>(filler, createItem(resinMaterial)
                .withName(parse("#FFA44A&lSize of mob"))
                .withLore(List.of(Component.empty(), parse("&7Current size: #FFA44A" + mob.mobScale), Component.empty(), parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    Colors.send(player, "&bEnter new scale (e.g. 1.0, 2.0, 0.5):");
                    plugin.getEditValuesListener().awaitChatInput(player)
                            .thenAccept(s -> cz.miniomega.mobs.gui.Gui.runSync(() -> {
                                if (s != null) try { mob.setMobScale(Double.parseDouble(s)); mob.spawn(); plugin.getMobConfig().saveMobs(); } catch (Exception ignored) {}
                                open(player);
                            }));
                }).build());

        // Slot 24: Spawner (Type)
        filler.setItem(24, new GuiItemBuilder<>(filler, createItem(Material.SPAWNER)
                .withName(parse("#555555&lType of Mob"))
                .withLore(List.of(Component.empty(), parse("&7Current: &a" + mob.getType().name()), Component.empty(), parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    Colors.send(player, "&bEnter EntityType name:");
                    plugin.getEditValuesListener().awaitChatInput(player)
                            .thenAccept(s -> cz.miniomega.mobs.gui.Gui.runSync(() -> {
                                if (s != null) try { mob.setType(org.bukkit.entity.EntityType.valueOf(s.toUpperCase())); mob.spawn(); plugin.getMobConfig().saveMobs(); } catch (Exception ignored) {}
                                open(player);
                            }));
                }).build());

        // Slot 29: Regen Potion (Regeneration)
        ItemStack regenPotion = new ItemStack(Material.POTION);
        PotionMeta pm = (PotionMeta) regenPotion.getItemMeta();
        if (pm != null) {
            pm.setBasePotionType(PotionType.REGENERATION);
            regenPotion.setItemMeta(pm);
        }
        filler.setItem(29, new GuiItemBuilder<>(filler, new ItemStackBuilder(regenPotion).addItemFlags(ItemFlag.values())
                .withName(parse("#9955FE&lRegeneration cooldown"))
                .withLore(List.of(Component.empty(), parse("&7Time after mob starts"), parse("&7regenerating"), Component.empty(), parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    Colors.send(player, "&bEnter regen idle seconds:");
                    plugin.getEditValuesListener().awaitChatInput(player)
                            .thenAccept(s -> cz.miniomega.mobs.gui.Gui.runSync(() -> {
                                if (s != null) NumberUtil.parseInt(s).ifPresent(i -> { mob.setRegenerationIdleSeconds(i); plugin.getMobConfig().saveMobs(); });
                                open(player);
                            }));
                }).build());

        // Slot 32: XP Bottle (Particles)
        filler.setItem(32, new GuiItemBuilder<>(filler, createItem(Material.EXPERIENCE_BOTTLE)
                .withName(parse("#C3A0F7&lParticles & Effects"))
                .withLore(List.of(Component.empty(), Component.text("The effect/particles on defeat the mob"), Component.text("and more!"), Component.empty(), parse("&7Click to enter")))
                .build())
                .withClickHandler(e -> openParticlesPage1(player)).build());

        // Slot 33: Glow Ink Sac (Glowing)
        filler.setItem(33, new GuiItemBuilder<>(filler, (Renderer<Integer>) (slot, state) -> {
            String modeName = state == 0 ? "&cdenied" : (state == 1 ? "&awhite" : "&ared");
            return createItem(Material.GLOW_INK_SAC)
                .withName(parse("#89DBF7&lGlowing while defeated"))
                .withLore(List.of(Component.empty(), parse("&7Current: " + modeName), Component.empty(), parse("&7Click to cycle (None -> White -> Red)")))
                .build();
        })
                .withDefaultState(mob.glowMode)
                .withClickHandler(e -> {
                    int nextMode = (mob.glowMode + 1) % 3;
                    mob.setGlowMode(nextMode);
                    e.getGuiItem().setState(nextMode);
                    plugin.getMobConfig().saveMobs();
                }).build());

        // Slot 49: Barrier (Close)
        filler.setItem(49, new GuiItemBuilder<>(filler, createItem(Material.BARRIER).withName(parse("&cClose")).build())
                .withClickHandler(e -> player.closeInventory()).build());

        // Slot 53: Fireball (Delete)
        filler.setItem(53, new GuiItemBuilder<>(filler, (Renderer<Boolean>) (slot, state) -> {
            if (Boolean.TRUE.equals(state)) return createItem(Material.RED_TERRACOTTA).withName(parse("&c&lCONFIRM DELETE")).build();
            return createItem(Material.FIRE_CHARGE).withName(parse("&4&lDELETE MOB")).build();
        })
                .withDefaultState(false)
                .withClickHandler(e -> {
                    if (Boolean.TRUE.equals(e.getGuiItem().getState())) { plugin.mobRegistry.unregister(mob); player.closeInventory(); }
                    else e.getGuiItem().setState(true);
                }).build());

        gui.open(player);
    }

    private void openParticlesPage1(Player player) {
        MapGuiFiller filler = new MapGuiFiller(
                "         ",
                "         ",
                "         ",
                "         ",
                "         ",
                "         "
        );
        GuiMeta<MapGuiFiller> meta = new GuiMeta<>(filler, Component.text("Effects Page 1: " + mob.getId()), InventoryType.CHEST_6);
        cz.miniomega.mobs.gui.Gui<MapGuiFiller> gui = new cz.miniomega.mobs.gui.Gui<>(meta);
        filler = gui.getFiller();

        applySides(filler, -1);

        // Firework Rocket (Toggle) - Slot 19
        filler.setItem(19, new GuiItemBuilder<>(filler, (Renderer<Boolean>) (slot, state) -> createItem(Material.FIREWORK_ROCKET)
                .withName(parse("#205295&lFirework Effect"))
                .withLore(List.of(Component.empty(), parse("&7Status: " + (state ? "&2ON" : "&4OFF"))))
                .build())
                .withDefaultState(mob.fireworkEffect)
                .withClickHandler(e -> {
                    mob.setFireworkEffect(!mob.fireworkEffect);
                    e.getGuiItem().setState(mob.fireworkEffect);
                    plugin.getMobConfig().saveMobs();
                }).build());

        // Firework Height - Slot 28
        filler.setItem(28, new GuiItemBuilder<>(filler, createItem(Material.FIREWORK_STAR)
                .withName(parse("#205295&lFirework Height"))
                .withLore(List.of(Component.empty(), parse("&7Current: #2C74B3" + mob.fireworkHeight), Component.empty(), parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory(); Colors.send(player, "&bEnter height:");
                    plugin.getEditValuesListener().awaitChatInput(player).thenAccept(s -> cz.miniomega.mobs.gui.Gui.runSync(() -> {
                        if (s != null) NumberUtil.parseInt(s).ifPresent(i -> { mob.setFireworkHeight(i); plugin.getMobConfig().saveMobs(); });
                        openParticlesPage1(player);
                    }));
                }).build());

        // Fishing Rod (Launch Mode Toggle) - Slot 20
        filler.setItem(20, new GuiItemBuilder<>(filler, (Renderer<Boolean>) (slot, state) -> createItem(Material.FISHING_ROD)
                .withName(parse("#205295&lLaunch Mode"))
                .withLore(List.of(Component.empty(), parse("&7Status: " + (state ? "&2ON" : "&4OFF"))))
                .build())
                .withDefaultState(mob.launchMode)
                .withClickHandler(e -> { mob.setLaunchMode(!mob.launchMode); e.getGuiItem().setState(mob.launchMode); plugin.getMobConfig().saveMobs(); })
                .build());

        // Slimeball (Chance) - Slot 29
        filler.setItem(29, new GuiItemBuilder<>(filler, createItem(Material.SLIME_BALL)
                .withName(parse("#205295&lLaunch Chance"))
                .withLore(List.of(Component.empty(), parse("&7Current: #2C74B3" + mob.launchChance + "%"), Component.empty(), parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory(); Colors.send(player, "&bEnter chance (0-100):");
                    plugin.getEditValuesListener().awaitChatInput(player).thenAccept(s -> cz.miniomega.mobs.gui.Gui.runSync(() -> {
                        if (s != null) NumberUtil.parseInt(s).ifPresent(i -> { mob.setLaunchChance(i); plugin.getMobConfig().saveMobs(); });
                        openParticlesPage1(player);
                    }));
                }).build());

        // Turtle Shell (Launch Range) - Slot 40
        filler.setItem(40, new GuiItemBuilder<>(filler, createItem(Material.TURTLE_HELMET)
                .withName(parse("#205295&lLaunch Range"))
                .withLore(List.of(Component.empty(), parse("&7Current: #2C74B3" + mob.launchRange), Component.empty(), parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory(); Colors.send(player, "&bEnter range:");
                    plugin.getEditValuesListener().awaitChatInput(player).thenAccept(s -> cz.miniomega.mobs.gui.Gui.runSync(() -> {
                        if (s != null) try { mob.setLaunchRange(Double.parseDouble(s)); plugin.getMobConfig().saveMobs(); } catch (Exception ignored) {}
                        openParticlesPage1(player);
                    }));
                }).build());

        // Feather (Chicken Cannon Toggle) - Slot 21
        filler.setItem(21, new GuiItemBuilder<>(filler, (Renderer<Boolean>) (slot, state) -> createItem(Material.FEATHER)
                .withName(parse("#205295&lChicken Cannon"))
                .withLore(List.of(Component.empty(), parse("&7Status: " + (state ? "&2ON" : "&4OFF"))))
                .build())
                .withDefaultState(mob.chickenLauncherEffect)
                .withClickHandler(e -> { mob.setChickenLauncherEffect(!mob.chickenLauncherEffect); e.getGuiItem().setState(mob.chickenLauncherEffect); plugin.getMobConfig().saveMobs(); })
                .build());

        // Egg (Chicken Power) - Slot 33
        filler.setItem(33, new GuiItemBuilder<>(filler, createItem(Material.EGG)
                .withName(parse("#205295&lChicken Power"))
                .withLore(List.of(Component.empty(), parse("&7Current: #2C74B3" + mob.chickenLauncherRange), Component.empty(), parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory(); Colors.send(player, "&bEnter chicken power:");
                    plugin.getEditValuesListener().awaitChatInput(player).thenAccept(s -> cz.miniomega.mobs.gui.Gui.runSync(() -> {
                        if (s != null) try { mob.setChickenLauncherRange(Double.parseDouble(s)); plugin.getMobConfig().saveMobs(); } catch (Exception ignored) {}
                        openParticlesPage1(player);
                    }));
                }).build());

        // Spectral Arrow (Back) - Slot 48
        filler.setItem(48, new GuiItemBuilder<>(filler, createItem(Material.SPECTRAL_ARROW).withName(parse("&7Back")).build()).withClickHandler(e -> open(player)).build());

        // Barrier (Close) - Slot 49
        filler.setItem(49, new GuiItemBuilder<>(filler, createItem(Material.BARRIER).withName(parse("&cClose")).build()).withClickHandler(e -> player.closeInventory()).build());

        // Spectral Arrow (Next Page) - Slot 50
        filler.setItem(50, new GuiItemBuilder<>(filler, createItem(Material.SPECTRAL_ARROW).withName(parse("&6Next Page")).build()).withClickHandler(e -> openParticlesPage2(player)).build());

        gui.open(player);
    }

    private void openParticlesPage2(Player player) {
        MapGuiFiller filler = new MapGuiFiller(
                "         ",
                "         ",
                "         ",
                "         ",
                "         ",
                "         "
        );
        GuiMeta<MapGuiFiller> meta = new GuiMeta<>(filler, Component.text("Effects Page 2: " + mob.getId()), InventoryType.CHEST_6);
        cz.miniomega.mobs.gui.Gui<MapGuiFiller> gui = new cz.miniomega.mobs.gui.Gui<>(meta);
        filler = gui.getFiller();

        applySides(filler, -1);

        // TNT Cannon (Toggle) - Slot 19
        filler.setItem(19, new GuiItemBuilder<>(filler, (Renderer<Boolean>) (slot, state) -> createItem(Material.TNT)
                .withName(parse("#205295&lTNT Cannon"))
                .withLore(List.of(Component.empty(), parse("&7Status: " + (state ? "&2ON" : "&4OFF"))))
                .build())
                .withDefaultState(mob.tntCannonEffect)
                .withClickHandler(e -> { mob.setTntCannonEffect(!mob.tntCannonEffect); e.getGuiItem().setState(mob.tntCannonEffect); plugin.getMobConfig().saveMobs(); })
                .build());

        // TNT Minecart (Count) - Slot 29
        filler.setItem(29, new GuiItemBuilder<>(filler, createItem(Material.TNT_MINECART)
                .withName(parse("#205295&lTNT Count"))
                .withLore(List.of(Component.empty(), parse("&7Current: #2C74B3" + mob.tntCannonCount), Component.empty(), parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory(); Colors.send(player, "&bEnter TNT count:");
                    plugin.getEditValuesListener().awaitChatInput(player).thenAccept(s -> cz.miniomega.mobs.gui.Gui.runSync(() -> {
                        if (s != null) NumberUtil.parseInt(s).ifPresent(i -> { mob.setTntCannonCount(i); plugin.getMobConfig().saveMobs(); });
                        openParticlesPage2(player);
                    }));
                }).build());

        // Spectral Arrow (Back) - Slot 48
        filler.setItem(48, new GuiItemBuilder<>(filler, createItem(Material.SPECTRAL_ARROW).withName(parse("&7Back")).build()).withClickHandler(e -> openParticlesPage1(player)).build());

        gui.open(player);
    }
}
