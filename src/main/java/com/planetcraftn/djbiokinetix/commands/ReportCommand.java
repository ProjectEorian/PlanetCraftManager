package com.planetcraftn.djbiokinetix.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.planetcraftn.djbiokinetix.Main;
import com.planetcraftn.djbiokinetix.utils.PluginUtils;
import com.planetcraftn.djbiokinetix.utils.ReportManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

public class ReportCommand implements SimpleCommand {

	private final Map<UUID, Long> cooldowns = new HashMap<>();
	private static final int COOLDOWN = 10; // segundos

	public ReportCommand() {
	}

	@Override
    public void execute(Invocation invocation) {
    	
    	CommandSource sender = invocation.source();
        String[] args = invocation.arguments();
    	
        if (!(sender instanceof Player)) {
            sender.sendMessage(PluginUtils.setColor("Solo jugadores pueden usar este comando"));
            return;
        }
        
        Player p = (Player) sender;
        
        if (args.length == 0) {
            p.sendMessage(PluginUtils.setColor("&8[&6Code&8] &7Uso: &2\"/reportar <jugador> <razón>\"&7."));
            return;
        }

        String sub = args[0].toLowerCase();
        // Subcomandos: push o eliminar
        if (sub.equals("push") || sub.equals("eliminar")) {
            handleSub(p, args);
            return;
        }

        // Validación de uso
        if (args.length < 2) {
            p.sendMessage(PluginUtils.setColor("&8[&6Code&8] &fUso: &a/reportar <jugador> <razón>"));
            return;
        }

        // Cooldown
        long now = System.currentTimeMillis() / 1000;
        UUID uid = p.getUniqueId();
        if (cooldowns.containsKey(uid)) {
            long last = cooldowns.get(uid) / 1000;
            long left = COOLDOWN - (now - last);
            if (left > 0) {
                p.sendMessage(PluginUtils.setColor("&8[&6Code&8] &7Espera &c" + left + "s &7antes de reportar de nuevo"));
                return;
            }
        }
        cooldowns.put(uid, System.currentTimeMillis());

        // Datos del target
        String targetName = args[0];
        UUID targetUuid = null;
        String serverName = null;
        
        Optional<Player> t = Main.getPlugin().getProxyServer().getPlayer(serverName);
        
        if (t.isPresent()) {
        	Player target = t.get();
            targetUuid = target.getUniqueId();
            serverName = target.getCurrentServer().map(conn -> conn.getServerInfo().getName()).orElse(null);
        }

        // Verificar abuso: mismo target >1 vez o totales > umbral en 24h
        int toSame = ReportManager.getReportsToTargetLast24h(uid, targetName);
        int tot24  = ReportManager.getReportsLast24h(uid);
        
        if (toSame > 1 || tot24 > ReportManager.UNTRUSTED_THRESHOLD) {
            String warn = String.format("&8[&6Code&8] &7Usuario &e%s &7poco confiable: &c%d reportes en 24h%s", p.getUsername(), tot24, (toSame > 1 ? String.format(", %d veces a %s", toSame, targetName) : ""));
            Main.getPlugin().getProxyServer().getAllPlayers().stream().filter(player -> player.hasPermission("planetcraft.command.reportes.recibir")).forEach(player -> player.sendMessage(PluginUtils.setColor(warn)));
        }

        // Construir razón y crear reporte
        String reason = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
        
        long reportId = ReportManager.createReport(uid, targetName, targetUuid, serverName, reason);

        // Notificar staff con hover
        String pre = "&8[&6Code&8] &e" + p.getUsername() + " &7ha reportado a &c" + targetName + (serverName != null ? " &fen &b" + serverName : " &f(&coffline&f)") + " &fpor &c" + reason + " &f- &8(&fclick &8» ";
        String mid = "&baquí";
        String post= " &8« &fpara revisar&8)";
        
        String cmd = "/intervenir " + p.getCurrentServer().map(conn -> conn.getServerInfo()).map(info -> info.getName()).orElse("Desconocido");
        String hov = "&7click para tomar caso #" + reportId;

        Main.getPlugin().getProxyServer().getAllPlayers().stream().filter(staff -> staff.hasPermission("planetcraft.command.reportes.recibir")).forEach(staff -> staff.sendMessage(PluginUtils.buildHoverMessage(pre, mid, post, cmd, hov)));

        p.sendMessage(PluginUtils.setColor("&8[&6Code&8] &7Reporte creado &fID#" + reportId));
    }

	private void handleSub(Player p, String[] args) {
		
		String sub = args[0].toLowerCase();
		
		if (args.length < 2) {
			p.sendMessage(PluginUtils.setColor("&8[&6Code&8] &7Uso: &2\"/reportar " + sub + " <id>\""));
			return;
		}
		
		long id;
		
		try {
			id = Long.parseLong(args[1]);
		} catch (NumberFormatException e) {
			p.sendMessage(PluginUtils.setColor("&8[&6Code&8] &7ID de reporte &cinválido&7."));
			return;
		}

		boolean success;
		
		if (sub.equals("push")) {
			success = ReportManager.confirmReport(id, p.getUsername());
			p.sendMessage(PluginUtils.setColor(success ? "&8[&6Code&8] &7Confirmado reporte &f#" + id : "&8[&6Code&8] &7No puedes confirmar este reporte"));
		} else {
			success = ReportManager.deleteReport(id, p.getUsername());
			p.sendMessage(PluginUtils.setColor(success ? "&8[&6Code&8] &7Eliminado reporte &f#" + id : "&8[&6Code&8] &7No puedes eliminar este reporte"));
		}
		
	}
}
