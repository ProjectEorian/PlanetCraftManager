package com.planetcraftn.djbiokinetix.listener;

import com.planetcraftn.djbiokinetix.Main;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;

public class PlayerListener {

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Main.getPlugin().getProxyServer().getScheduler().buildTask(Main.getPlugin(), () -> {
        	Main.getPlugin().getDBManager().consultas(event.getPlayer().getUsername());
        }).schedule();
    }

    @Subscribe
    public void onPlayerJoin(ServerConnectedEvent event) {
        Main.getPlugin().getProxyServer().getScheduler().buildTask(Main.getPlugin(), () -> {
        	Main.getPlugin().savePlayerCount();
        }).schedule();
    }

    @Subscribe
    public void onPlayerQuit(DisconnectEvent event) {
    	Main.getPlugin().getProxyServer().getScheduler().buildTask(Main.getPlugin(), () -> {
    		Main.getPlugin().savePlayerCount();
        }).schedule();
    }
}
