package cz.miniomega.mobs.playerdata.placeholder;

import cz.miniomega.mobs.playerdata.PlayerData;
import cz.miniomega.mobs.util.placeholders.PlaceholderSet;

public class PlayerDataPlaceholderSet extends PlaceholderSet {

    public PlayerDataPlaceholderSet(PlayerData playerData) {
        addPlaceholder("player", playerData::getDisplayName);
        addPlaceholder("uuid", () -> playerData.uuid.toString());
        addPlaceholder("hits", () -> String.valueOf(playerData.hits));
    }

}
