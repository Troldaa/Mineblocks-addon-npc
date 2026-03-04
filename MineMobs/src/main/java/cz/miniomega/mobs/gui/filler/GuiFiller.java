package cz.miniomega.mobs.gui.filler;

import cz.miniomega.mobs.gui.Gui;
import cz.miniomega.mobs.gui.item.GuiItem;
import cz.miniomega.mobs.gui.meta.GuiMeta;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface GuiFiller<S extends GuiFiller<S>> {

    Gui<S> getParent();
    GuiItem<?> getItem(int slot);
    Collection<GuiItem<?>> getItems();
    Collection<Integer> getSlots(GuiItem<?> item);
    CompletableFuture<Void> stateUpdated(GuiItem<?> item);
    S withParent(Gui<S> parent, GuiMeta<S> meta);
    CompletableFuture<Void> renderAll();
    void setItem(int slot, GuiItem<?> item);

}
