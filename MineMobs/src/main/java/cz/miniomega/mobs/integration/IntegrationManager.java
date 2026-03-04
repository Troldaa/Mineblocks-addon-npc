package cz.miniomega.mobs.integration;

import cz.miniomega.mobs.MineMobsPlugin;
import cz.miniomega.mobs.integration.dh.DHIntegration;
import cz.miniomega.mobs.integration.models.hologram.HologramProvider;
import cz.miniomega.mobs.integration.models.placeholder.PlaceholderProvider;
import cz.miniomega.mobs.integration.papi.PAPIIntegration;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.PluginManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class IntegrationManager implements PlaceholderProvider {

    private static final Map<String, Function<MineMobsPlugin, Integration>> INTEGRATION_REGISTRY = new HashMap<>();

    static {
        INTEGRATION_REGISTRY.put(DHIntegration.PLUGIN_NAME, DHIntegration::new);
        INTEGRATION_REGISTRY.put(PAPIIntegration.PLUGIN_NAME, PAPIIntegration::new);
    }

    private final List<Integration> integrations;
    private final HologramProvider hologramProvider;
    private final List<PlaceholderProvider> placeholderProviders;
    private final BukkitAudiences bukkitAudiences;

    @SneakyThrows
    public IntegrationManager(MineMobsPlugin plugin) {
        this.bukkitAudiences = BukkitAudiences.create(plugin);
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        integrations = new LinkedList<>();
        for (Map.Entry<String, Function<MineMobsPlugin, Integration>> entry : INTEGRATION_REGISTRY.entrySet()) {
            if (pluginManager.isPluginEnabled(entry.getKey())) {
                try {
                    integrations.add(entry.getValue().apply(plugin));
                    plugin.getLogger().info("Integration with plugin " + entry.getKey() + " successfully enabled!");
                } catch (Exception e) {
                    // Ignore failures of specific integrations
                }
            }
        }
        hologramProvider = integrations.stream()
                .filter(HologramProvider.class::isInstance)
                .max(Comparator.comparingInt(Integration::getPriority))
                .map(i -> (HologramProvider) i)
                .orElse(null);

        if (hologramProvider != null)
            plugin.getLogger().info("Using " + ((Integration) hologramProvider).getPluginName() + " as hologram provider");

        placeholderProviders = integrations.stream()
                .filter(PlaceholderProvider.class::isInstance)
                .map(i -> (PlaceholderProvider) i)
                .collect(Collectors.toList());
    }

    public void disable() {
        integrations.forEach(Integration::disable);
        if (bukkitAudiences != null) bukkitAudiences.close();
    }

    @Override
    public String setPlaceholders(OfflinePlayer player, String text) {
        String result = text;
        for (PlaceholderProvider placeholderProvider : placeholderProviders) {
            result = placeholderProvider.setPlaceholders(player, result);
        }
        return result;
    }

}
