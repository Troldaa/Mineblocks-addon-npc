package cz.raixo.mobs;

import org.bukkit.entity.Entity;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MobRegistry {
    private final Map<String, MineMob> mobMap = new ConcurrentHashMap<>();
    private final Map<UUID, MineMob> mobByEntity = new ConcurrentHashMap<>();

    public void register(MineMob mob) {
        mobMap.put(mob.getId(), mob);
        mob.spawn();
        if (mob.getSpawnedEntity() != null) {
            mobByEntity.put(mob.getSpawnedEntity().getUniqueId(), mob);
        }
    }

    public void unregister(MineMob mob) {
        mobMap.remove(mob.getId());
        if (mob.getSpawnedEntity() != null) {
            mobByEntity.remove(mob.getSpawnedEntity().getUniqueId());
        }
        mob.remove();
        mob.setId(null); // Prevent re-spawning
        mob.getPlugin().getMobConfig().saveMobs();
    }

    public MineMob getById(String id) {
        return mobMap.get(id);
    }

    public MineMob getByEntity(Entity entity) {
        return mobByEntity.get(entity.getUniqueId());
    }

    public Collection<MineMob> getMobs() {
        return mobMap.values();
    }

    public void updateEntityMap(MineMob mob, Entity oldEntity, Entity newEntity) {
        if (oldEntity != null) {
            mobByEntity.remove(oldEntity.getUniqueId());
        }
        if (newEntity != null) {
            mobByEntity.put(newEntity.getUniqueId(), mob);
        }
    }
}
