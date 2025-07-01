package com.planetcraftn.djbiokinetix.manager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.planetcraftn.djbiokinetix.Main;

public class DBManager {
	
    private String url;
    private String urlsindb;
    private String user;
    private String pass;
    
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
	
    public DBManager(String url, String user, String pass) {
    	
        this.url = url;
        this.user = user;
        this.pass = pass;
        
        if (url.contains("/")) {
            this.urlsindb = url.substring(0, url.lastIndexOf("/")) + "/?serverTimezone=UTC";
        } else {
            this.urlsindb = url;
        }
        
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url + "?serverTimezone=UTC", user, pass);
    }
    
    public void desconectar() throws Exception {
    	DriverManager.getConnection(url, user, pass).close();
    }
    
    public void crearDatabaseSiNoExiste() {
        String nombreBD = url.substring(url.lastIndexOf("/") + 1);
    	try (Connection conn = DriverManager.getConnection(urlsindb, user, pass); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + nombreBD);
            LOGGER.log(Level.INFO, "[PlanetCraft Web Manager] Base de datos '" + nombreBD + "' creada/verificada.");
        } catch (SQLException e) {}
    }
    
    public void crearTablaSiNoExiste() {
        try (Connection conn = DriverManager.getConnection(url + "?serverTimezone=UTC", user, pass); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(x.y.z.X9A1L.getCreateStmt());
            LOGGER.log(Level.INFO, "[PlanetCraft Web Manager] Consulta: " + x.y.z.X9A1L.getCreateStmt());
            LOGGER.log(Level.INFO, "[PlanetCraft Web Manager] Tabla 'jugadores' creada/verificada.");
        } catch (SQLException e) {}
    }
    
    public void consultas(String nombre) {
        try (Connection conn = DriverManager.getConnection(url + "?serverTimezone=UTC", user, pass)) {
        	
            PreparedStatement stmt = conn.prepareStatement(x.y.z.X9A1L.getSelectStmt());
            stmt.setString(1, nombre);
            LOGGER.log(Level.INFO, "[PlanetCraft Web Manager] Consulta: " + x.y.z.X9A1L.getSelectStmt());
            LOGGER.log(Level.INFO, "[PlanetCraft Web Manager] SELECT realizada para " + nombre + "...");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            
            if (!(resultSet.getInt(1) == 0)) {
                PreparedStatement update = conn.prepareStatement(x.y.z.X9A1L.getUpdateStmt());
                update.setString(1, nombre);
                update.executeUpdate();
                LOGGER.log(Level.INFO, "[PlanetCraft Web Manager] Consulta: " + x.y.z.X9A1L.getUpdateStmt());
                LOGGER.log(Level.INFO, "[PlanetCraft Web Manager] UPDATE realizado para " + nombre + "...");
            } else {
            	PreparedStatement insert = conn.prepareStatement(x.y.z.X9A1L.getInsertStmt());
                insert.setString(1, nombre);
                insert.executeUpdate();
                LOGGER.log(Level.INFO, "[PlanetCraft Web Manager] Consulta: " + x.y.z.X9A1L.getInsertStmt());
                LOGGER.log(Level.INFO, "[PlanetCraft Web Manager] INSERT realizado para " + nombre + "...");
            }
            
        } catch (Exception e) { e.printStackTrace(); }
    }
    
}
