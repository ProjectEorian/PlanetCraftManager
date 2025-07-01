package com.planetcraftn.djbiokinetix.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.planetcraftn.djbiokinetix.Main;
import com.planetcraftn.djbiokinetix.utils.PluginUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import net.kyori.adventure.text.Component;

public class Ayuda implements SimpleCommand {

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public Ayuda() {}

    @Override
    public void execute(Invocation invocation) {
    	
        CommandSource source = invocation.source();
        
        // Solo jugadores pueden ejecutar el comando
        if (!(source instanceof Player)) {
            source.sendMessage(PluginUtils.setColor("&8[&6Code&8] &7Solo los jugadores pueden usar este comando."));
            return;
        }

        Player p = (Player) source;
        String[] args = invocation.arguments();
        if (args.length != 0) {
            p.sendMessage(PluginUtils.setColor("&8[&6Code&8] &7Uso correcto: &2\"/ayuda\"&7."));
            return;
        }

        int cdSeconds = 3;
        UUID id = p.getUniqueId();

        if (cooldowns.containsKey(id)) {
            long last = cooldowns.get(id) / 1000L;
            long now = System.currentTimeMillis() / 1000L;
            long left = cdSeconds - (now - last);
            if (left > 0) {
                p.sendMessage(PluginUtils.setColor("&8[&6Code&8] &7Espera &c" + left + " &7segundos antes de pedir o volver a pedir ayuda."));
                return;
            }
        }

        cooldowns.put(id, System.currentTimeMillis());

        for (Player staff : Main.getPlugin().getProxyServer().getAllPlayers()) {
        	
            if (!staff.hasPermission("planetcraft.command.reportes.recibir")) continue;
            
            String serverName = p.getCurrentServer().map(
            	conn -> conn.getServerInfo()
            ).map(
            	info -> info.getName()
            ).orElse(
            	"Desconocido"
            );
            String cmd = "/intervenir " + serverName;
            Component message = PluginUtils.buildHoverMessage("&8[&6Code&8] &7El jugador &e"+p.getUsername()+" &7está solicitando ayuda en &b"+serverName+" &8» ", "&bclic aquí", " &8« &7para ir al servidor.", cmd, "Haz clic aqui para conectarte al servidor de " + p.getUsername());
            staff.sendMessage(message);
            
        }
        
    }
}