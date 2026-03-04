package cz.miniomega.mobs.util.color;

import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

public class Colors {

	public static String colorize(String string) {
		if (string == null) return null;
		// Robustly handle ACF and legacy tags
		string = string.replace("<c1>", "&f")
                       .replace("<c2>", "&b")
                       .replace("<c3>", "&7")
                       .replace("<c4>", "&c")
                       .replace("<c5>", "&a")
                       .replace("{link}", "")
                       .replace("{/link}", "");
		return IridiumColorAPI.process(string);
	}

	public static List<String> colorize(List<String> list) {
		return list.stream().map(Colors::colorize).collect(Collectors.toList());
	}

	public static void send(CommandSender sender, String... message) {
		for (String s : message) {
			sender.sendMessage(colorize(s));
		}
	}

}
