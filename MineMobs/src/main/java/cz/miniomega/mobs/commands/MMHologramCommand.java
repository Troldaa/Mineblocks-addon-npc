package cz.miniomega.mobs.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import cz.miniomega.mobs.util.color.Colors;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;

@CommandAlias("mm|minemobs")
@CommandPermission("mm.admin")
public class MMHologramCommand extends BaseCommand {

    public static void showHologram(Audience audience, String command, String id, List<String> lines) {
        Component message = MineDown.parse("&#2C74B3&Hologram of " + id + ":");

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            message = message.append(Component.newline())
                    .append(
                            Component.text(" - ", NamedTextColor.DARK_GRAY)
                                    .clickEvent(ClickEvent.suggestCommand("/" + command + " hologram setline " + id + " " + (i + 1) + " " + line))
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to edit!", TextColor.color(32, 82, 149))))
                                    .append(Component.text(line, NamedTextColor.GRAY))
                                    .append(
                                            Component.text(" [Remove]", TextColor.color(223, 46, 56))
                                            .hoverEvent(HoverEvent.showText(Component.text("Click to remove!", TextColor.color(223, 46, 56))))
                                            .clickEvent(ClickEvent.suggestCommand("/" + command + " hologram removeline " + id + " " + (i + 1)))
                                    )
                    );
        }

        message = message.append(Component.newline())
                        .append(
                                Component.text(" Add new line", TextColor.color(44, 116, 179))
                                        .hoverEvent(HoverEvent.showText(Component.text("Click to add line!", TextColor.color(32, 82, 149))))
                                        .clickEvent(ClickEvent.suggestCommand("/" + command + " hologram addline " + id + " "))
                        );

        if (audience != null) {
            audience.sendMessage(message);
        }
    }
}
