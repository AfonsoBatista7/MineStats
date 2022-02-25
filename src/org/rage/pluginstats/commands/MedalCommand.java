package org.rage.pluginstats.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.rage.pluginstats.medals.MLevel;
import org.rage.pluginstats.medals.Medals;
import org.rage.pluginstats.utils.Util;

/**
 * Prints all information about a specified medal
 * @author Afonso Batista
 * 2021 - 2022
 */
public class MedalCommand implements CommandExecutor {

	public MedalCommand() {}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		Medals medal; 
		try {
			medal = Medals.valueOf(args[0].toUpperCase());
		} catch(ArrayIndexOutOfBoundsException e) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - You need to specify a medal."));
			return false;
		} catch(IllegalArgumentException e) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - This Medal doesn't exist yet."));
			return false;
		} 
		
		if(medal.getTransition()!=0)
			sender.sendMessage(
				Util.chat("&b[MineStats]&7 - &c<medalName>&7:\n &6<level1>&7 -> &a<stat1>&7\n &6<level2>&7 -> &a<stat2>&7\n &6<level3>&7 -> &a<stat3>&7\n &6<level4>&7 -> &a<stat4>&7 <statName>\n &7Tag: <tag>\n &7How: &3<how>"
						.replace("<medalName>", medal.toString())
						.replace("<level1>", MLevel.I.toString())
						.replace("<stat1>", String.valueOf(calculateTransition(medal, 0)))
						.replace("<level2>", MLevel.II.toString())
						.replace("<stat2>", String.valueOf(calculateTransition(medal, 1)))
						.replace("<level3>", MLevel.III.toString())
						.replace("<stat3>", String.valueOf(calculateTransition(medal, 2)))
						.replace("<level4>", MLevel.GOD.toString())
						.replace("<stat4>", String.valueOf(calculateTransition(medal, 3)))
						.replace("<statName>", medal.getStatName())
						.replace("<tag>", Util.rainbowText("["+medal.getTag().getTag().toUpperCase()+"]"))
						.replace("<how>", medal.getHowToGet())));
		else
			sender.sendMessage(
					Util.chat("&b[MineStats]&7 - &c<medalName>&7: &3<how>"
							.replace("<medalName>", medal.toString())
							.replace("<how>", medal.getHowToGet())));
		
		return true;
	}
	private long calculateTransition(Medals medal, int toMultiply) {
		return (long) (medal.getTransition()*Math.pow(medal.getMultiplyer(), toMultiply));
	}
}
