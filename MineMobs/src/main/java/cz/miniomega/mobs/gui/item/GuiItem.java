package cz.miniomega.mobs.gui.item;

import cz.miniomega.mobs.gui.item.click.ClickHandler;
import cz.miniomega.mobs.gui.item.state.updater.StateUpdater;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public interface GuiItem<T> extends ClickHandler<T> {

    T getState();
    void setState(T state);
    void stateUpdated();
    ItemStack render(int slot);
    boolean requiresPerSlotRendering();
    Optional<StateUpdater<T>> getStateUpdater();
    void setStateUpdater(StateUpdater<T> updater);
    boolean hasState();

}
