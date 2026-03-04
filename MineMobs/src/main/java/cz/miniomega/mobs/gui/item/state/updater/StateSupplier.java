package cz.miniomega.mobs.gui.item.state.updater;

import cz.miniomega.mobs.gui.item.GuiItem;

public interface StateSupplier<T> {

    T updateState(GuiItem<T> item);

}
