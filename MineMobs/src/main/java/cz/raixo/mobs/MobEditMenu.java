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
                "    b    ",
                " r c t s ",
                " p x g f ",
                "         ",
                "    q    "
        );

        GuiMeta<MapGuiFiller> meta = new GuiMeta<>(filler, Component.text("Edit Mob: " + mob.getId()), InventoryType.CHEST_6);
        cz.raixo.blocks.gui.Gui<MapGuiFiller> gui = new cz.raixo.blocks.gui.Gui<>(meta);
        filler = gui.getFiller();

        // b: Bone Meal (Hologram Editor)
        filler.setItem('b', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.BONE_MEAL)
                .withName(MineDown.parse("&#205295&&lHologram Editor"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Click to edit lines in chat")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    MBCommand.showHologram(plugin.getMineBlocks().getBukkitAudiences().player(player), "mm", mob.getId(), mob.getHologramLines());
                }).build());

        // r: Red Dye (Health)
        filler.setItem('r', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.RED_DYE)
                .withName(MineDown.parse("&#205295&&lHealth"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Current: &#2C74B3&" + mob.getHealth().getMaxHealth()), Component.empty(), MineDown.parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    Colors.send(player, "&bEnter new health:");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player)
                            .thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> {
                                open(player);
                                if (s != null) NumberUtil.parseInt(s).ifPresent(i -> { mob.getHealth().setMaxHealth(i); mob.getHealth().setHealth(i); plugin.getMobConfig().saveMobs(); });
                            }));
                }).build());

        // c: Clock (Cooldown)
        filler.setItem('c', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.CLOCK)
                .withName(MineDown.parse("&#205295&&lCooldown Time"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Current: &#2C74B3&" + mob.getCooldownSeconds() + "s"), Component.empty(), MineDown.parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    Colors.send(player, "&bEnter cooldown in seconds:");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player)
                            .thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> {
                                open(player);
                                if (s != null) NumberUtil.parseInt(s).ifPresent(i -> { mob.setCooldownSeconds(i); plugin.getMobConfig().saveMobs(); });
                            }));
                }).build());

        // t: Spawner (Type)
        filler.setItem('t', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.SPAWNER)
                .withName(MineDown.parse("&#205295&&lMob Type"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Current: &#2C74B3&" + mob.getType().name()), Component.empty(), MineDown.parse("&7Click to change")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    Colors.send(player, "&bEnter EntityType name:");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player)
                            .thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> {
                                open(player);
                                if (s != null) try { mob.setType(org.bukkit.entity.EntityType.valueOf(s.toUpperCase())); mob.spawn(); plugin.getMobConfig().saveMobs(); } catch (Exception ignored) {}
                            }));
                }).build());

        // f: Fire Charge (Delete)
        filler.setItem('f', new GuiItemBuilder<>(filler, (Renderer<Boolean>) (slot, state) -> {
            if (Boolean.TRUE.equals(state)) return ItemStackBuilder.create(Material.RED_TERRACOTTA).withName(MineDown.parse("&c&lCONFIRM DELETE")).build();
            return ItemStackBuilder.create(Material.FIRE_CHARGE).withName(MineDown.parse("&4&lDELETE MOB")).build();
        })
                .withDefaultState(false)
                .withClickHandler(e -> {
                    if (Boolean.TRUE.equals(e.getGuiItem().getState())) { plugin.getMobRegistry().unregister(mob); player.closeInventory(); }
                    else e.getGuiItem().setState(true);
                }).build());

        // p: Regen Potion (Regeneration)
        filler.setItem('p', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.POTION)
                .withName(MineDown.parse("&#205295&&lRegeneration"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Idle: &#2C74B3&" + mob.getRegenerationIdleSeconds() + "s"), Component.empty(), MineDown.parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    Colors.send(player, "&bEnter regen idle seconds:");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player)
                            .thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> {
                                open(player);
                                if (s != null) NumberUtil.parseInt(s).ifPresent(i -> { mob.setRegenerationIdleSeconds(i); plugin.getMobConfig().saveMobs(); });
                            }));
                }).build());

        // x: XP Bottle (Particles)
        filler.setItem('x', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.EXPERIENCE_BOTTLE)
                .withName(MineDown.parse("&#205295&&lParticles & Effects"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Edit combat effects, knockback,"), MineDown.parse("&7and defeat particles.")))
                .build())
                .withClickHandler(e -> openParticlesPage1(player)).build());

        // g: Glow Ink Sac (Glowing)
        filler.setItem('g', new GuiItemBuilder<>(filler, (Renderer<Boolean>) (slot, state) -> ItemStackBuilder.create(Material.GLOW_INK_SAC)
                .withName(MineDown.parse("&#205295&&lGlowing Status"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Red Glow on defeat: " + (state ? "&2ON" : "&4OFF"))))
                .build())
                .withDefaultState(mob.isGlowingRed())
                .withClickHandler(e -> {
                    mob.setGlowingRed(!mob.isGlowingRed());
                    e.getGuiItem().setState(mob.isGlowingRed());
                    plugin.getMobConfig().saveMobs();
                }).build());

        // s: Amethyst Shard (Scale)
        filler.setItem('s', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.AMETHYST_SHARD)
                .withName(MineDown.parse("&#205295&&lMob Scale"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Current: &#2C74B3&" + mob.getMobScale()), Component.empty(), MineDown.parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory();
                    Colors.send(player, "&bEnter new scale (e.g. 1.0, 2.0, 0.5):");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player)
                            .thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> {
                                open(player);
                                if (s != null) try { mob.setMobScale(Double.parseDouble(s)); mob.spawn(); plugin.getMobConfig().saveMobs(); } catch (Exception ignored) {}
                            }));
                }).build());

        // q: Barrier (Close)
        filler.setItem('q', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.BARRIER).withName(MineDown.parse("&cClose")).build())
                .withClickHandler(e -> player.closeInventory()).build());

        gui.open(player);
    }

    private void openParticlesPage1(Player player) {
        MapGuiFiller filler = new MapGuiFiller(
                "         ",
                " r l c   ",
                " h m e f ",
                "         ",
                "    b n  "
        );
        GuiMeta<MapGuiFiller> meta = new GuiMeta<>(filler, Component.text("Effects Page 1: " + mob.getId()), InventoryType.CHEST_6);
        cz.raixo.blocks.gui.Gui<MapGuiFiller> gui = new cz.raixo.blocks.gui.Gui<>(meta);
        filler = gui.getFiller();

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
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player).thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> { openParticlesPage1(player); if (s != null) NumberUtil.parseInt(s).ifPresent(i -> { mob.setFireworkHeight(i); plugin.getMobConfig().saveMobs(); }); }));
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
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player).thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> { openParticlesPage1(player); if (s != null) NumberUtil.parseInt(s).ifPresent(i -> { mob.setLaunchChance(i); plugin.getMobConfig().saveMobs(); }); }));
                }).build());

        // e: Turtle Shell (Launch Range)
        filler.setItem('e', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.TURTLE_HELMET)
                .withName(MineDown.parse("&#205295&&lLaunch Range"))
                .withLore(List.of(Component.empty(), MineDown.parse("&7Current: &#2C74B3&" + mob.getLaunchRange()), Component.empty(), MineDown.parse("&7Click to edit")))
                .build())
                .withClickHandler(e -> {
                    player.closeInventory(); Colors.send(player, "&bEnter range:");
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player).thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> { openParticlesPage1(player); if (s != null) try { mob.setLaunchRange(Double.parseDouble(s)); plugin.getMobConfig().saveMobs(); } catch (Exception ignored) {} }));
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
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player).thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> { openParticlesPage1(player); if (s != null) try { mob.setChickenLauncherRange(Double.parseDouble(s)); plugin.getMobConfig().saveMobs(); } catch (Exception ignored) {} }));
                }).build());

        // n: Next Page
        filler.setItem('n', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.TIPPED_ARROW).withName(MineDown.parse("&6Next Page")).build()).withClickHandler(e -> openParticlesPage2(player)).build());

        // b: Back to Main
        filler.setItem('b', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.ARROW).withName(MineDown.parse("&7Back")).build()).withClickHandler(e -> open(player)).build());

        gui.open(player);
    }

    private void openParticlesPage2(Player player) {
        MapGuiFiller filler = new MapGuiFiller("         ", " t m     ", "         ", "    b    ");
        GuiMeta<MapGuiFiller> meta = new GuiMeta<>(filler, Component.text("Effects Page 2: " + mob.getId()), InventoryType.CHEST_6);
        cz.raixo.blocks.gui.Gui<MapGuiFiller> gui = new cz.raixo.blocks.gui.Gui<>(meta);
        filler = gui.getFiller();

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
                    plugin.getMineBlocks().getEditValuesListener().awaitChatInput(player).thenAccept(s -> cz.raixo.blocks.gui.Gui.runSync(() -> { openParticlesPage2(player); if (s != null) NumberUtil.parseInt(s).ifPresent(i -> { mob.setTntCannonCount(i); plugin.getMobConfig().saveMobs(); }); }));
                }).build());

        // b: Back
        filler.setItem('b', new GuiItemBuilder<>(filler, ItemStackBuilder.create(Material.ARROW).withName(MineDown.parse("&7Back")).build()).withClickHandler(e -> openParticlesPage1(player)).build());

        gui.open(player);
    }
}
