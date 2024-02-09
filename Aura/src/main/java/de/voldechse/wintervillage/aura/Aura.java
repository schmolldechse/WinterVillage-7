package de.voldechse.wintervillage.aura;

import com.google.common.reflect.ClassPath;
import com.google.gson.JsonArray;
import de.voldechse.wintervillage.aura.commands.*;
import de.voldechse.wintervillage.aura.gamestate.GameStateManager;
import de.voldechse.wintervillage.aura.utils.GameManager;
import de.voldechse.wintervillage.aura.utils.ScoreboardManager;
import de.voldechse.wintervillage.aura.utils.border.WorldBorderController;
import de.voldechse.wintervillage.aura.utils.position.Position;
import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.library.gradient.Gradient;
import dev.derklaro.aerogel.Inject;
import dev.derklaro.aerogel.Singleton;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionManagement;
import eu.cloudnetservice.ext.platforminject.api.PlatformEntrypoint;
import eu.cloudnetservice.ext.platforminject.api.stereotype.Command;
import eu.cloudnetservice.ext.platforminject.api.stereotype.PlatformPlugin;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
@PlatformPlugin(platform = "bukkit",
        pluginFileNames = "plugin.yml",
        name = "Aura",
        version = "1.1-RELEASE",
        authors = "Voldechse",
        commands = {
              @Command(name = "admin",
                      permission = "wintervillage.event.aura.command.admin"),
                @Command(name = "build",
                        permission = "wintervillage.event.aura.command.build"),
                @Command(name = "pause",
                        permission = "wintervillage.event.aura.command.pause"),
                @Command(name = "settime",
                        permission = "wintervillage.event.aura.command.settime"),
                @Command(name = "start",
                        permission = "wintervillage.event.aura.command.start")
        }
)
public class Aura implements PlatformEntrypoint {

    private final JavaPlugin instance;
    public String serverPrefix;

    public GameManager gameManager;
    public GameStateManager gameStateManager;
    public ScoreboardManager scoreboardManager;
    public WorldBorderController worldBorderController;

    public PermissionManagement permissionManagement;

    public List<UUID> SPECTATORS;

    public Document configDocument, worldBorderDocument;

    public int minPlayers,
            lobby_sleepDelay,
            lobbyCountdown,
            preparingStartCountdown,
            ingameCountdown,
            pvpEnabledAfter,
            worldBorderAfter,
            compassAfter,
            PLAYING;

    public boolean STARTED, PAUSED, PVP_ENABLED, USE_WORLDBORDER;

    public int potencyAdmin,
            potencyDeveloper,
            potencySupporter,
            potencyContentCreator,
            potencyTeilnehmer,
            potencySpectator;

    @Inject
    public Aura(JavaPlugin plugin) {
        this.instance = plugin;

        this.instance.getLogger().info("-----------------------------------");
        this.instance.getLogger().info("Aura wird initialisiert!");

        this.serverPrefix = " " + Gradient.color("Aura", Color.WHITE, Color.RED) + " §8| §f";

        if (!getInstance().getDataFolder().exists()) getInstance().getDataFolder().mkdirs();
        initializeGameConfig_notavailable();
        initializeWorldBorderConfig_notAvailable();

        this.permissionManagement = InjectionLayer.ext().instance(PermissionManagement.class);

        this.potencyAdmin = 100;
        this.potencyDeveloper = 80;
        this.potencySupporter = 60;
        this.potencyContentCreator = 40;
        this.potencyTeilnehmer = 20;
        this.potencySpectator = 0;
    }

    @Override
    public void onLoad() {
        this.instance.getLogger().info("-----------------------------------");
        this.instance.getLogger().info("config.json | worldborder.json werden initialisiert...");
        this.configDocument = Document.loadDocument(Paths.get(getInstance().getDataFolder().getAbsolutePath(), "config.json"));
        this.worldBorderDocument = Document.loadDocument(Paths.get(getInstance().getDataFolder().getAbsolutePath(), "worldborder.json"));

        if (Bukkit.getWorld("world_playing_aura") == null) Bukkit.createWorld(WorldCreator.name("world_playing_aura"));

        this.instance.getLogger().info("Events werden registriert");
        this.registerListener();

        this.instance.getLogger().info("Befehle werden registriert");
        new CommandAdmin();
        new CommandBuild();
        new CommandPause();
        new CommandSetTime();
        new CommandStart();

        this.gameManager = new GameManager();
        this.gameStateManager = new GameStateManager();
        this.scoreboardManager = new ScoreboardManager();

        this.minPlayers = this.configDocument.getInt("minPlayers");
        this.lobby_sleepDelay = this.configDocument.getInt("lobby_sleepDelay");
        this.lobbyCountdown = this.configDocument.getInt("lobbyCountdown");
        this.preparingStartCountdown = this.configDocument.getInt("preparingStartCountdown");
        this.ingameCountdown = this.configDocument.getInt("ingameCountdown");
        this.pvpEnabledAfter = this.configDocument.getInt("pvpEnabledAfter");
        this.worldBorderAfter = this.configDocument.getInt("worldBorderAfter");
        this.compassAfter = this.configDocument.getInt("compassAfter");

        this.PAUSED = false;
        this.STARTED = false;

        this.USE_WORLDBORDER = this.worldBorderDocument.getBoolean("enabled");
        if (this.USE_WORLDBORDER) this.worldBorderController = new WorldBorderController();

        this.SPECTATORS = new ArrayList<>();

        this.instance.getLogger().info("Aura wurde initialisiert!");
        this.instance.getLogger().info("-----------------------------------");
    }

    @Override
    public void onDisable() {
        this.instance.getLogger().info("-----------------------------------");
        this.instance.getLogger().info("Aura wird deaktiviert");
        this.instance.getLogger().info("Scheduler werden abgebrochen");
        Bukkit.getScheduler().cancelTasks(this.instance);

        this.gameManager = null;
        this.gameStateManager = null;
        this.scoreboardManager = null;
        this.worldBorderController = null;

        if (!this.SPECTATORS.isEmpty()) this.SPECTATORS.clear();

        this.instance.getLogger().info("Aura wurde deaktiviert");
        this.instance.getLogger().info("-----------------------------------");
    }

    private void registerListener() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        try {
            for (ClassPath.ClassInfo classInfo : ClassPath.from(getClass().getClassLoader()).getTopLevelClasses("de.voldechse.wintervillage.aura.listener")) {
                Class clazz = Class.forName(classInfo.getName());
                if (Listener.class.isAssignableFrom(clazz))
                    pluginManager.registerEvents((Listener) clazz.newInstance(), getInstance());
            }
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException exception) {
            Logger.getLogger(Aura.class.getName()).log(Level.SEVERE, null, exception);
        }
    }

    private void initializeGameConfig_notavailable() {
        if (Files.exists(Paths.get(getInstance().getDataFolder().getAbsolutePath(), "config.json"))) return;
        this.instance.getLogger().severe("config.json wird erstellt, da sie nicht gefunden wurde");

        Position location = new Position(0.0, 0.0, 0.0, 0.0f, 0.0f, "world");

        new Document("lobby_sleepDelay", 15)
                .append("lobbyCountdown", 60)
                .append("preparingStartCountdown", 5)
                .append("ingameCountdown", 20 * 60)
                .append("pvpEnabledAfter", 30)
                .append("worldBorderAfter", 0)
                .append("compassAfter", 10 * 60)
                .append("minPlayers", 2)
                .append("lobbySpawn", location)
                .append("playerSpawn", location)
                .saveAsConfig(Paths.get(getInstance().getDataFolder().getAbsolutePath(), "config.json"));

        this.instance.getLogger().info("config.json wurde erstellt");
    }

    private void initializeWorldBorderConfig_notAvailable() {
        if (Files.exists(Paths.get(getInstance().getDataFolder().getAbsolutePath(), "worldborder.json"))) return;
        this.instance.getLogger().severe("worldborder.json wird erstellt, da sie nicht gefunden wurde");

        WorldBorderController.BorderPhase phase1 = new WorldBorderController.BorderPhase(0, new WorldBorderController.Center(false, 0.0, 0.0), 1000, 700, 180, 60);
        WorldBorderController.BorderPhase phase2 = new WorldBorderController.BorderPhase(1, new WorldBorderController.Center(false, 0.0, 0.0), 700, 500, 60, 30);
        WorldBorderController.BorderPhase phase3 = new WorldBorderController.BorderPhase(2, new WorldBorderController.Center(false, 0.0, 0.0), 500, 300, 60, 30);
        WorldBorderController.BorderPhase phase4 = new WorldBorderController.BorderPhase(3, new WorldBorderController.Center(true, 0.0, 0.0), 300, 150, 60, 30);
        WorldBorderController.BorderPhase phase5 = new WorldBorderController.BorderPhase(4, new WorldBorderController.Center(true, 0.0, 0.0), 150, 75, 45, 0);

        List<WorldBorderController.BorderPhase> temporaryList = new ArrayList<>();
        temporaryList.add(phase1);
        temporaryList.add(phase2);
        temporaryList.add(phase3);
        temporaryList.add(phase4);
        temporaryList.add(phase5);

        JsonArray jsonElements = new JsonArray();
        temporaryList.forEach(borderPhase -> {
            Document phase = new Document("id", borderPhase.getId())
                    .append("center", borderPhase.getCenter())
                    .append("oldSize", borderPhase.getOldSize())
                    .append("newSize", borderPhase.getNewSize())
                    .append("timeShrinking", borderPhase.getTimeShrinking())
                    .append("pufferForNextBorder", borderPhase.getPufferForNextBorder());
            jsonElements.add(phase.obj());
        });

        new Document("enabled", true)
                .append("world", "world_playing_aura")
                .append("controller", jsonElements)
                .saveAsConfig(Paths.get(getInstance().getDataFolder().getAbsolutePath(), "worldborder.json"));

        this.instance.getLogger().severe("worldborder.json wurde erstellt");
    }

    public void reloadConfigs() {
        this.configDocument = Document.loadDocument(Paths.get(getInstance().getDataFolder().getAbsolutePath(), "config.json"));
        this.worldBorderDocument = Document.loadDocument(Paths.get(getInstance().getDataFolder().getAbsolutePath(), "worldborder.json"));

        this.USE_WORLDBORDER = this.worldBorderDocument.getBoolean("enabled");
        if (this.USE_WORLDBORDER) this.worldBorderController = new WorldBorderController();

        this.minPlayers = this.configDocument.getInt("minPlayers");
        this.lobby_sleepDelay = this.configDocument.getInt("lobby_sleepDelay");
        this.lobbyCountdown = this.configDocument.getInt("lobbyCountdown");
        this.preparingStartCountdown = this.configDocument.getInt("preparingStartCountdown");
        this.ingameCountdown = this.configDocument.getInt("ingameCountdown");
        this.pvpEnabledAfter = this.configDocument.getInt("pvpEnabledAfter");
        this.worldBorderAfter = this.configDocument.getInt("worldBorderAfter");
        this.compassAfter = this.configDocument.getInt("compassAfter");

        this.PVP_ENABLED = false;
        this.PAUSED = false;
        this.STARTED = false;
    }

    public void removeMetadata(Entity entity, String name) {
        if (entity.hasMetadata(name)) entity.removeMetadata(name, this.instance);
    }

    public void setMetadata(Player entity, String name, Object object) {
        this.removeMetadata(entity, name);
        entity.setMetadata(name, new FixedMetadataValue(this.instance, object));
    }

    public Object getMetadata(Entity entity, String name) {
        if (entity.hasMetadata(name)) return entity.getMetadata(name);
        return null;
    }

    public boolean hasMetadata(Entity entity, String name) {
        return entity.hasMetadata(name);
    }

    public JavaPlugin getInstance() {
        return instance;
    }
}
