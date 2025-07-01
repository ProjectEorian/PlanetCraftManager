package com.planetcraftn.djbiokinetix;

import java.io.FileWriter;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import com.planetcraftn.djbiokinetix.commands.Ayuda;
import com.planetcraftn.djbiokinetix.commands.ClearChatGlobal;
import com.planetcraftn.djbiokinetix.commands.Intervenir;
import com.planetcraftn.djbiokinetix.commands.ReportCommand;
import com.planetcraftn.djbiokinetix.commands.StaffChat;
import com.planetcraftn.djbiokinetix.listener.PlayerListener;
import com.planetcraftn.djbiokinetix.manager.DBManager;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

@Plugin(
		id = "djbiokinetix",
		name = "PlanetCraft Web Manager",
		version = "1.0.0",
		authors = {"DJBiokinetix"}
)

public class Main {
	
	private static Main instance;
	private static DBManager db;
	
	private final ProxyServer proxy;
	private final Path dataDirectory;
    private final Logger logger;

	private String user  = "ubuntu";
	private String host  = "10.0.10.4";
	private String dbs   = "PlanetCraftWebManager";
	private Integer port = 3306;
	
	@Inject
	public Main(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
		this.proxy = proxy;
		this.logger = logger;
		this.dataDirectory = dataDirectory;
		instance = this;
	}
	
	@Subscribe
	public void onEnable() {
		
		instance = this;
		
		logger.log(Level.WARNING, "[PlanetCraft Web Manager] [SECURITY] REVISANDO CLAVE REMOTA...");
		x.y.z.X9A1L.q();
		logger.log(Level.WARNING, "[PlanetCraft Web Manager] [SECURITY] ¡CLAVE REMOTA VALIDA!");
		
		proxy.getEventManager().register(this, new PlayerListener());
		
		CommandManager commandManager = proxy.getCommandManager();
		commandManager.register(commandManager.metaBuilder("auyda").build(), new Ayuda());
		commandManager.register(commandManager.metaBuilder("intervenir").build(), new Intervenir());
		commandManager.register(commandManager.metaBuilder("clearchatglobal").build(), new ClearChatGlobal());
		commandManager.register(commandManager.metaBuilder("report").build(), new ReportCommand());
		commandManager.register(commandManager.metaBuilder("staffchat").build(), new StaffChat());
		
		conectarBD();
	}
	
	public static Main getPlugin() {
		return instance;
	}
	
	public ProxyServer getProxyServer() {
		return proxy;
	}
	
	public Logger getLogger() {
		return logger;
	}

    public void savePlayerCount() {
        int count = proxy.getPlayerCount();
        try (FileWriter writer = new FileWriter(dataDirectory.resolve("online_count.json").toFile())) {
            writer.write("{\"online\":" + count + "}");
        } catch (Exception e) {
            logger.severe("No se pudo guardar la cantidad de jugadores online.");
        }
    }
	
	public void conectarBD() {
		String jdbc = "jdbc:mysql://"+host+":"+port+"/"+dbs;
		db = new DBManager(jdbc, user, x.y.z.X9A1L.getPass());
		db.crearDatabaseSiNoExiste();
		db.crearTablaSiNoExiste();
	}
	
	public DBManager getDBManager() {
		return db;
	}
	
}
