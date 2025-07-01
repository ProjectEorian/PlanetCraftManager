package com.planetcraftn.djbiokinetix.commands;

import java.util.Optional;

import com.planetcraftn.djbiokinetix.Main;
import com.planetcraftn.djbiokinetix.utils.PluginUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

public class Intervenir implements SimpleCommand {

    public Intervenir() {}

    @Override
    public void execute(Invocation invocation) {

        CommandSource source = invocation.source();

        if (!(source instanceof Player)) {
            source.sendMessage(PluginUtils.setColor("Este comando no se puede usar aquí."));
            return;
        }

        Player p = (Player) source;
        String[] args = invocation.arguments();

        if (!p.hasPermission("planetcraft.command.intervenir")) {
            p.sendMessage(PluginUtils.setColor("&8[&6Code&8] &cNo tienes permiso."));
            return;
        }

        String serverName = p.getCurrentServer().map(conn -> conn.getServerInfo().getName()).orElse("Desconocido");

        if (serverName.equalsIgnoreCase("Login")) {
            p.sendMessage(PluginUtils.setColor("&8[&6Code&8] &7No puedes usar este comando &caquí&7."));
            return;
        }

        if (args.length == 0) {
            p.sendMessage(PluginUtils.setColor("&8[&6Code&8] &7Uso correcto: /intervenir <jugador>"));
            return;
        }

        String targetName = args[0];
        Optional<Player> optionalTarget = Main.getPlugin().getProxyServer().getPlayer(targetName);

        if (!optionalTarget.isPresent()) {
            p.sendMessage(PluginUtils.setColor("&8[&6Code&8] &cEl jugador no está conectado."));
            return;
        }

        Player target = optionalTarget.get();
        String targetServerName = target.getCurrentServer().map(conn -> conn.getServerInfo().getName()).orElse(null);

        if (targetServerName == null) {
            p.sendMessage(PluginUtils.setColor("&8[&6Code&8] &cNo se pudo obtener el servidor del jugador."));
            return;
        }

        int onlinePlayers = target.getCurrentServer().map(conn -> conn.getServer().getPlayersConnected().size()).orElse(0);

        String hoverText = onlinePlayers + (onlinePlayers == 1 ? " jugador" : " jugadores");
        String cmd = "/intervenir " + targetServerName;

        p.sendMessage(PluginUtils.buildHoverMessage("&8[&6Code&8] &7- &b" + targetServerName + " &8(&fclick &8» ", "&baqui", " &8« &fpara investigar&8)", cmd, hoverText + "\n&7click para conectarte"));
        
        RegisteredServer targetServer = Main.getPlugin().getProxyServer().getServer(targetServerName).orElse(null);

        if (targetServer == null) {
            p.sendMessage(PluginUtils.setColor("&8[&6Code&8] &cNo se pudo encontrar el servidor."));
            return;
        }

        PluginUtils.connectToServer(p, targetServer);

    }
}
