package cz.miniomega.mobs.gui.item.click;

public interface ClickHandler<T> {

    void onClick(ItemClickEvent<T> itemClickEvent);

}
