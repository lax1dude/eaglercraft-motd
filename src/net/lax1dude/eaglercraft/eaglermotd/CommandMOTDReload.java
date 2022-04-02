package net.lax1dude.eaglercraft.eaglermotd;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandMOTDReload extends Command {

	public final EaglerMOTD plugin;
	
	public CommandMOTDReload(EaglerMOTD plugin) {
		super("motd-reload", "eaglermotd.command.reload");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender paramCommandSender, String[] paramArrayOfString) {
		try {
			plugin.loadConfiguration(paramCommandSender);
		} catch (Exception e) {
			paramCommandSender.sendMessage(ChatColor.RED + "[EaglerMOTD] Failed to reload! " + e.toString());
			e.printStackTrace();
		}
	}
	
}
