package com.planetcraftn.djbiokinetix.commands;

import com.planetcraftn.djbiokinetix.Main;
import com.planetcraftn.djbiokinetix.utils.PluginUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

public class StaffChat implements SimpleCommand {
	
	public StaffChat() {}

	@Override
	public void execute(Invocation invocation) {
    	
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();
        
        if (!(sender instanceof Player)) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < args.length; i++) sb.append(args[i]).append(" ");
			Main.getPlugin().getProxyServer().getAllPlayers().stream().filter(player -> player.hasPermission("planetcraft.command.staffchat")).forEach(player -> player.sendMessage(PluginUtils.setColor("&8[&6Code&8] (&4SC&8) &7&o@Console &8» &f" + sb.toString())));
			sender.sendMessage(PluginUtils.setColor("&8[&6Code&8] (&4SC&8) &7&o@Console &8» &f" + sb.toString()));
            return;
        }
        
		Player p = (Player) sender;
		
		if (!p.hasPermission("planetcraft.command.staffchat")) {
			p.sendMessage(PluginUtils.setColor("&8[&6Code&8] &7No tienes permiso."));
			return;
		}
		
		if (args.length == 0) {
			p.sendMessage(PluginUtils.setColor("&8[&6Code&8] (&4SC&8) &7Uso correcto: &c\"/sc <mensaje>\"&7."));
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < args.length; i++) sb.append(args[i]).append(" ");
		Main.getPlugin().getProxyServer().getAllPlayers().stream().filter(player -> player.hasPermission("planetcraft.command.staffchat")).forEach(player -> player.sendMessage(PluginUtils.setColor("&8[&6Code&8] (&4SC&8) &7(&b" + p.getCurrentServer().map(conn -> conn.getServerInfo().getName()).orElse(null) + "&7) &e" + p.getUsername() + " &8» &f" + sb.toString())));
		
	}
}
