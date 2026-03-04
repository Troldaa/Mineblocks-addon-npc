package cz.miniomega.mobs.integration;

public interface Integration {

    String getPluginName();
    int getPriority();
    default void disable() {};

}
