package com.planetcraftn.djbiokinetix.utils;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class PluginUtils {
	
    public static Component buildHoverMessage(String texto1, String texto2_click, String texto3, String comando, String lore_click) {
    	
        Component mensaje = setColor(
        	texto1
        ).append(
        		
        	setColor(
        		texto2_click
        	)
        	
        	.hoverEvent(	
        		HoverEvent.showText(
        			setColor(
        				lore_click
        			)
        		)
        	)
        	
        	.clickEvent(
        		ClickEvent.runCommand(
        			comando
        		)
        	)
        	
        ).append(
        		
    		setColor(
    			texto3
    		)
    		
        );
        
		return mensaje;
		
    }

    public static Component setColor(String color) {
    	return LegacyComponentSerializer.legacyAmpersand().deserialize(color.replace('&', '§'));
    }
    
    public static void connectToServer(Player player, RegisteredServer targetServer) {
        player.getCurrentServer().map(conn -> conn.getServer()).ifPresentOrElse(current -> {
            if (current.getServerInfo().equals(targetServer.getServerInfo())) {
                Component msg = LegacyComponentSerializer.legacyAmpersand().deserialize("&8[&6Code&8] &7Ya estás en &b" + targetServer.getServerInfo().getName());
                player.sendMessage(msg);
            } else {
                // Mensaje de conexión y solicitud
                Component msg = LegacyComponentSerializer.legacyAmpersand().deserialize("&8[&6Code&8] &7Conectando a &b" + targetServer.getServerInfo().getName() + "&7…");
                player.sendMessage(msg);
                player.createConnectionRequest(targetServer).fireAndForget();
            }
        }, () -> {
            Component msg = LegacyComponentSerializer.legacyAmpersand().deserialize("&8[&6Code&8] &7Conectando a &b" + targetServer.getServerInfo().getName() + "&7…");
            player.sendMessage(msg);
            player.createConnectionRequest(targetServer).fireAndForget();
        });
    }
    
}
