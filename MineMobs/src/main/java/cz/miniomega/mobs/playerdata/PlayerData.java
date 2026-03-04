package cz.miniomega.mobs.playerdata;

import cz.miniomega.mobs.util.serializable.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class PlayerData implements Serializable {

    public static PlayerData deserialize(DataInput input) throws IOException {
        return new PlayerData(new UUID(input.readLong(), input.readLong()), input.readUTF(), input.readInt());
    }

    public final UUID uuid;
    public final String displayName;
    public int hits;

    public PlayerData(UUID uuid, String displayName) {
        this.uuid = uuid;
        this.displayName = displayName;
        this.hits = 0;
    }

    public void incrementHits() {
        hits++;
    }

    @Override
    public void serialize(DataOutput output) throws IOException {
        output.writeLong(uuid.getMostSignificantBits());
        output.writeLong(uuid.getLeastSignificantBits());
        output.writeUTF(displayName);
        output.writeInt(hits);
    }

    public boolean isOnline() {
        return Bukkit.getPlayer(uuid) != null;
    }

}
