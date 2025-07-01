package com.planetcraftn.djbiokinetix.utils;

import com.planetcraftn.djbiokinetix.Main;
import com.planetcraftn.djbiokinetix.manager.DBManager;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;

public class ReportManager {

    private static final Map<Long, Report> pending = new ConcurrentHashMap<>();
    private static final Map<Long, ScheduledFuture<?>> reminders = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final long REMINDER_PERIOD = 1;   // horas entre recordatorios
    private static final long EXPIRE_DELAY    = 24;  // horas para expirar
    public  static final Set<String> ADMINS = Set.of("MitchelMH", "DJBiokinetix");
    public  static final int    UNTRUSTED_THRESHOLD = 10; // umbral en 24h

    // Historial para detectar abuso
    private static final Map<UUID, List<Long>> reporterTimestamps = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, List<Long>>> reporterTargetTimestamps = new ConcurrentHashMap<>();

    private static long nextReportId = 1;

    // SQL statements
    private static final String CREATE_TABLE_SQL =
        "CREATE TABLE IF NOT EXISTS reports (" +
        "report_id BIGINT PRIMARY KEY, " +
        "reporter_uuid VARCHAR(36) NOT NULL, " +
        "target_name VARCHAR(16) NOT NULL, " +
        "target_uuid VARCHAR(36), " +
        "server_name VARCHAR(32), " +
        "reason TEXT, " +
        "created_at TIMESTAMP, " +
        "handled_by VARCHAR(16), " +
        "status VARCHAR(16), " +
        "handled_at TIMESTAMP" +
        ");";
    private static final String INSERT_SQL =
        "INSERT INTO reports (report_id, reporter_uuid, target_name, target_uuid, server_name, reason, created_at, handled_by, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
    private static final String UPDATE_SQL =
        "UPDATE reports SET handled_by = ?, status = ?, handled_at = ? WHERE report_id = ?;";
    private static final String DELETE_SQL =
        "DELETE FROM reports WHERE report_id = ?;";

    private static final DBManager db = Main.getPlugin().getDBManager();
    private static final ProxyServer proxy = Main.getPlugin().getProxyServer();

    static {
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement()) {
            // Crear tabla si no existe
            stmt.executeUpdate(CREATE_TABLE_SQL);
            Main.getPlugin().getLogger().info("[ReportManager] Tabla 'reports' verificada/creada");

            // Inicializar nextReportId basado en la BD
            try (ResultSet rs = stmt.executeQuery("SELECT MAX(report_id) FROM reports")) {
                if (rs.next()) {
                    long max = rs.getLong(1);
                    nextReportId = max + 1;
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static synchronized long createReport(
        UUID reporter, String targetName, UUID targetUuid, String server, String reason
    ) {
        long now = System.currentTimeMillis();
        reporterTimestamps.computeIfAbsent(reporter, k -> new ArrayList<>()).add(now);
        reporterTargetTimestamps
            .computeIfAbsent(reporter, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(targetName.toLowerCase(), k -> new ArrayList<>())
            .add(now);

        long id = nextReportId++;
        Report rpt = new Report(id, reporter, targetName, targetUuid, server, reason);
        pending.put(id, rpt);
        scheduleReminder(rpt);

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            ps.setLong(1, id);
            ps.setString(2, reporter.toString());
            ps.setString(3, targetName);
            ps.setString(4, targetUuid != null ? targetUuid.toString() : null);
            ps.setString(5, server);
            ps.setString(6, reason);
            ps.setTimestamp(7, new Timestamp(now));
            ps.setString(8, null);
            ps.setString(9, "OPEN");
            ps.executeUpdate();
            Main.getPlugin().getLogger().info("[ReportManager] Reporte creado.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    public static int getReportsLast24h(UUID reporter) {
        long cutoff = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24);
        return (int) reporterTimestamps
            .getOrDefault(reporter, List.of()).stream()
            .filter(ts -> ts >= cutoff)
            .count();
    }

    public static int getReportsToTargetLast24h(UUID reporter, String targetName) {
        long cutoff = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24);
        return (int) reporterTargetTimestamps
            .getOrDefault(reporter, Map.of())
            .getOrDefault(targetName.toLowerCase(), List.of()).stream()
            .filter(ts -> ts >= cutoff)
            .count();
    }

    public static Report getReport(long id) {
        return pending.get(id);
    }

    public static synchronized boolean confirmReport(long id, String handler) {
        Report rpt = pending.get(id);
        if (rpt == null) return false;
        String prev = rpt.getHandler();
        if (prev != null && !prev.equals(handler) && !ADMINS.contains(handler)) return false;
        rpt.setHandler(handler);
        try (
            Connection conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)
        ) {
            ps.setString(1, handler);
            ps.setString(2, "CONFIRMED");
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.setLong(4, id);
            ps.executeUpdate();
            Main.getPlugin().getLogger().info("[ReportManager] Reporte subido.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ScheduledFuture<?> rem = reminders.remove(id);
        if (rem != null) rem.cancel(false);
        pending.remove(id);
        return true;
    }

    public static synchronized boolean deleteReport(long id, String handler) {
        Report rpt = pending.get(id);
        if (rpt == null) return false;
        String prev = rpt.getHandler();
        if (prev != null && !prev.equals(handler) && !ADMINS.contains(handler)) return false;
        try (
            Connection conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(DELETE_SQL)
        ) {
            ps.setLong(1, id);
            ps.executeUpdate();
            Main.getPlugin().getLogger().info("[ReportManager] Reporte borrado.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ScheduledFuture<?> rem = reminders.remove(id);
        if (rem != null) rem.cancel(false);
        pending.remove(id);
        return true;
    }

    private static void scheduleReminder(Report rpt) {
        // Recordatorios periódicos
        ScheduledFuture<?> rem = scheduler.scheduleAtFixedRate(() -> {
            String handler = rpt.getHandler();
            if (handler != null) {
                proxy.getPlayer(handler).ifPresent(staff -> {
                    Component msg = LegacyComponentSerializer.legacyAmpersand()
                        .deserialize("&8[&6Code&8] &7Recuerda revisar el reporte &f#" + rpt.getId());
                    staff.sendMessage(msg);
                });
            }
        }, REMINDER_PERIOD, REMINDER_PERIOD, TimeUnit.HOURS);
        reminders.put(rpt.getId(), rem);

        // Expiración automática
        scheduler.schedule(() -> {
            pending.remove(rpt.getId());
            ScheduledFuture<?> r = reminders.remove(rpt.getId());
            if (r != null) r.cancel(false);
            for (Player pl : proxy.getAllPlayers()) {
                if (ADMINS.contains(pl.getUsername())) {
                    Component msg = LegacyComponentSerializer.legacyAmpersand()
                        .deserialize("&8[&6Code&8] &7El reporte &f#" + rpt.getId() + " &7fue borrado por falta de veredicto");
                    pl.sendMessage(msg);
                }
            }
        }, EXPIRE_DELAY, TimeUnit.HOURS);
    }
}