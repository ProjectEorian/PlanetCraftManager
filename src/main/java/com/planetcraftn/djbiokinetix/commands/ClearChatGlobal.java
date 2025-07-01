package com.planetcraftn.djbiokinetix.commands;

import java.util.Collection;
import java.util.Optional;

import com.planetcraftn.djbiokinetix.Main;
import com.planetcraftn.djbiokinetix.utils.PluginUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import net.kyori.adventure.text.Component;

public class ClearChatGlobal implements SimpleCommand {

	private static final int BLANK_LINES = 180;

	public ClearChatGlobal() {
	}

	@Override
	public void execute(Invocation invocation) {
		
		CommandSource sender = invocation.source();
		String[] args = invocation.arguments();

		if (!sender.hasPermission("planetcraft.command.clearchat")) {
			sender.sendMessage(PluginUtils.setColor("&8[&6Code&8] &8(&4GCC&8) &cNo tienes permiso."));
			return;
		}

		if (args.length == 0) {
			clearCollection(Main.getPlugin().getProxyServer().getAllPlayers());
			broadcastMessage(Main.getPlugin().getProxyServer().getAllPlayers(), "&8[&6Code&8] &8(&4GCC&8) &e⚠️ &7El chat ha sido limpiado por un &coperador &7en toda la network.");
			sender.sendMessage(PluginUtils.setColor("&8[&6Code&8] &8(&4GCC&8) &e⚠️ &7Has limpiado el chat en &ctodos &7los servidores."));
			return;
		}

		Optional<RegisteredServer> targetOpt = Main.getPlugin().getProxyServer().getServer(args[0]);
		if (!targetOpt.isPresent()) {
			sender.sendMessage(PluginUtils.setColor("&8[&6Code&8] &8(&4GCC&8) &7El servidor &b" + args[0] + " &7no existe."));
			return;
		}
		
		RegisteredServer target = targetOpt.get();
		Collection<Player> playersOnServer = Main.getPlugin().getProxyServer().getAllPlayers().stream().filter(p -> p.getCurrentServer().map(conn -> conn.getServerInfo()).filter(info -> info.equals(target.getServerInfo())).isPresent()).toList();

		clearCollection(playersOnServer);
		broadcastMessage(playersOnServer, "&8[&6Code&8] &8(&4CC&8) &7El chat ha sido limpiado por un &coperador&7.");
		sender.sendMessage(PluginUtils.setColor("&8[&6Code&8] &8(&4CC&8) &7Has limpiado el chat del servidor &c" + target.getServerInfo().getName() + "&7."));
		
	}

	private void clearCollection(Collection<Player> players) {
		Component blank = Component.text(" ");
		for (Player pl : players) {
			for (int i = 0; i < BLANK_LINES; i++) {
				pl.sendMessage(blank);
			}
		}
	}

	private void broadcastMessage(Collection<Player> players, String message) {
		Component msg = PluginUtils.setColor(message);
		for (Player pl : players) {
			pl.sendMessage(msg);
		}
	}
}
