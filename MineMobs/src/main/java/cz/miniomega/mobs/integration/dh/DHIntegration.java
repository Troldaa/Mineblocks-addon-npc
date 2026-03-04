package cz.miniomega.mobs.integration.dh;

import cz.miniomega.mobs.MineMobsPlugin;
import cz.miniomega.mobs.integration.Integration;
import cz.miniomega.mobs.integration.models.hologram.Hologram;
import cz.miniomega.mobs.integration.models.hologram.HologramProvider;
import org.bukkit.Location;

public class DHIntegration implements Integration, HologramProvider {

    public static final String PLUGIN_NAME = "DecentHolograms";

    public DHIntegration(MineMobsPlugin plugin) {
        try {
            Class.forName("eu.decentsoftware.holograms.api.DHAPI");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("DecentHolograms API (DHAPI) not found. Is your DecentHolograms version at least 2.0.12?");
        }
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public Hologram provide(String name, Location location) {
        return new HologramDH(name, location);
    }

}
