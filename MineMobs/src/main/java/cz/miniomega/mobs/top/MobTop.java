package cz.miniomega.mobs.top;

import cz.miniomega.mobs.playerdata.PlayerData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MobTop {

    public static final int MAX_TOP_SIZE = 10;

    private final List<PlayerData> players = new ArrayList<>();

    public void update(PlayerData player) {
        players.removeIf(p -> p.uuid.equals(player.uuid));
        PlayerData last = getLast();
        if (players.size() < MAX_TOP_SIZE && (last == null || player.hits <= last.hits)) {
            players.add(player);
        } else for (int i = 0; i < players.size(); i++) {
            if (i + 1 >= players.size() || players.get(i).hits < player.hits) {
                players.add(i, player);
                break;
            }
        }
        if (players.size() > MAX_TOP_SIZE)
            players.remove(players.size() - 1);
    }

    private PlayerData getLast() {
        if (players.isEmpty()) return null;
        return players.get(players.size() - 1);
    }

    public Optional<PlayerData> getPlayer(int pos) {
        if (pos < players.size()) return Optional.ofNullable(players.get(pos));
        return Optional.empty();
    }

    public void clear() {
        players.clear();
    }

    public List<PlayerData> getPlayers() {
        return Collections.unmodifiableList(players);
    }

}
