package cz.miniomega.mobs.gui.meta;

import cz.miniomega.mobs.gui.Gui;
import cz.miniomega.mobs.gui.filler.GuiFiller;
import cz.miniomega.mobs.gui.type.InventoryType;
import net.kyori.adventure.text.Component;

public class GuiMeta<F extends GuiFiller<F>> {

    private final F filler;
    private final Component title;
    private final InventoryType type;

    public GuiMeta(F filler, Component title, InventoryType type) {
        this.filler = filler;
        this.title = title;
        this.type = type;
    }

    public F getFiller() {
        return filler;
    }

    public Component getTitle() {
        return title;
    }

    public InventoryType getType() {
        return type;
    }

    public GuiMeta<F> withParent(Gui<F> parent) {
        return new GuiMeta<>(filler.withParent(parent, this), title, type);
    }

}
