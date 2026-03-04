package cz.miniomega.mobs.listener;
import cz.miniomega.mobs.MineMobsPlugin;
import cz.miniomega.mobs.MineMob;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class MobListener implements Listener {
    private final MineMobsPlugin plugin;

    public MobListener(MineMobsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        MineMob mob = plugin.mobRegistry.getByEntity(e.getEntity());
        if (mob == null) return;

        e.setCancelled(true);
        if (mob.isCoolingDown()) return;

        if (e.getDamager() instanceof Player) {
            Player player = (Player) e.getDamager();

            if (e.getEntity() instanceof LivingEntity) {
                if (((LivingEntity) e.getEntity()).getNoDamageTicks() > 10) return;
            }

            if (mob.getPermission() != null && !player.hasPermission(mob.getPermission())) {
                return;
            }

            mob.onDamage(player).run();
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (plugin.mobRegistry.getByEntity(e.getEntity()) != null) {
            if (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        MineMob mob = plugin.mobRegistry.getByEntity(e.getEntity());
        if (mob != null) {
            e.getDrops().clear();
            e.setDroppedExp(0);
        }
    }

    @EventHandler
    public void onCombust(EntityCombustEvent e) {
        if (plugin.mobRegistry.getByEntity(e.getEntity()) != null) {
            e.setCancelled(true);
        }
    }
}
