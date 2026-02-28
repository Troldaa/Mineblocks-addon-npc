package cz.raixo.mobs;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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
        MineMob mob = plugin.getMobRegistry().getByEntity(e.getEntity());
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
            // Handle entity map update if it respawned
            plugin.getMobRegistry().updateEntityMap(mob, null, mob.getSpawnedEntity());
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (plugin.getMobRegistry().getByEntity(e.getEntity()) != null) {
            if (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        MineMob mob = plugin.getMobRegistry().getByEntity(e.getEntity());
        if (mob != null) {
            e.getDrops().clear();
            e.setDroppedExp(0);
            // Mob will respawn in reset() called by onDamage or should be handled here if killed by other means
        }
    }
}
