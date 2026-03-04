package cz.miniomega.mobs.integration.papi;

import cz.miniomega.mobs.MineMobsPlugin;
import cz.miniomega.mobs.MineMob;
import cz.miniomega.mobs.playerdata.PlayerData;
import cz.miniomega.mobs.util.NumberUtil;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class MineMobsPlaceholders extends PlaceholderExpansion {

    public final MineMobsPlugin plugin;

    @Override
    public @NotNull String getIdentifier() {
        return "mm";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    private Optional<MineMob> getMob(List<String> params) {
        MineMob mob = plugin.mobRegistry.get(String.join("_", params));
        if (mob != null) return Optional.of(mob);
        if (params.size() <= 1) return Optional.empty();
        return getMob(params.subList(0, params.size() - 1));
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        Optional<MineMob> mobOpt = getMob(new ArrayList<>(List.of(params.split("_"))));
        if (mobOpt.isEmpty()) return "mob_not_found";
        MineMob mob = mobOpt.get();
        String value = params.substring(mob.getId().length());
        if (!value.isEmpty()) value = value.substring(1);
        value = value.toLowerCase();
        if (value.startsWith("top_")) {
            boolean hits = value.startsWith("top_hits_");
            String pos = value.substring(hits ? 11 : 4);
            Optional<PlayerData> playerData = NumberUtil.parseInt(pos)
                    .map(i -> i - 1)
                    .flatMap(p -> mob.top.getPlayer(p));
            return hits ?
                    String.valueOf(playerData.map(PlayerData::getHits).orElse(0)) :
                    playerData.map(PlayerData::getDisplayName).orElse("");
        } else switch (value) {
            case "hp": return String.valueOf(mob.health.health);
            case "max_hp": return String.valueOf(mob.health.maxHealth);
            case "hits": return String.valueOf(
                    Optional.ofNullable(
                                    mob.playerDataMap.get(player.getUniqueId()))
                            .map(PlayerData::getHits)
                            .orElse(0)
            );
            case "cooldown":
                return String.valueOf(mob.getRemainingCooldown());
            default: return null;
        }
    }

}
