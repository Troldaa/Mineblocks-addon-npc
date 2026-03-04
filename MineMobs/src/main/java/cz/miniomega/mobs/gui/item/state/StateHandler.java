package cz.miniomega.mobs.gui.item.state;

import cz.miniomega.mobs.gui.item.GuiItem;

public interface StateHandler {

    void onAdd(GuiItem<?> item);
    void onRemove(GuiItem<?> item);

}
