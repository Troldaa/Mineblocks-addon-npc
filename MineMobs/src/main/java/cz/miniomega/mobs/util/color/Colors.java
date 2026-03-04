package cz.miniomega.mobs.util.color;

import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

public class Colors {

	public static String colorize(String string) {
		if (string == null) return null;
		// Handle legacy <c2> if it somehow appears, but Iridium usually handles it
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
