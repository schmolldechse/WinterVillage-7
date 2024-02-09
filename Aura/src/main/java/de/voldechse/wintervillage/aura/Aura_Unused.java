package de.voldechse.wintervillage.aura;

import com.google.common.reflect.ClassPath;
import com.google.gson.JsonArray;
import de.voldechse.wintervillage.aura.gamestate.GameStateManager;
import de.voldechse.wintervillage.aura.utils.GameManager;
import de.voldechse.wintervillage.aura.utils.ScoreboardManager;
import de.voldechse.wintervillage.aura.utils.border.WorldBorderController;
import de.voldechse.wintervillage.aura.utils.position.Position;
import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.library.gradient.Gradient;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Entity;
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

public class Aura_Unused extends JavaPlugin {

    private static Aura_Unused instance;
    public String serverPrefix;

    public GameManager gameManager;
    public GameStateManager gameStateManager;
    public ScoreboardManager scoreboardManager;
    public WorldBorderController worldBorderController;

    public List<UUID> SPECTATORS;

    public Document configDocument, worldBorderDocument;

    public int minPlayers, lobby_sleepDelay, lobbyCountdown, preparingStartCountdown, ingameCountdown, pvpEnabledAfter, PLAYING;

    public boolean STARTED, PAUSED, PVP_ENABLED;

    @Override
    public void onLoad() {
        Bukkit.getLogger().info("-----------------------------------");
        Bukkit.getLogger().info("Aura wird initialisiert!");
        instance = this;

        this.serverPrefix = " " + Gradient.color("Aura", Color.WHITE, Color.RED) + " §8| §f";

        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        initializeGameConfig_notavailable();
        initializeWorldBorderConfig_notAvailable();
    }

    @Override
    public void onEnable() {
        Bukkit.getLogger().info("-----------------------------------");
        Bukkit.getLogger().info("config.json | worldborder.json werden initialisiert...");
        this.configDocument = Document.loadDocument(Paths.get(getDataFolder().getAbsolutePath(), "config.json"));
        this.worldBorderDocument = Document.loadDocument(Paths.get(getDataFolder().getAbsolutePath(), "worldborder.json"));

        if (Bukkit.getWorld("world_playing_aura") == null) Bukkit.createWorld(WorldCreator.name("world_playing_aura"));

        System.out.println("Events werden registriert");
        registerListener();

        System.out.println("Befehle werden registriert");
        //new CommandAdmin();
        //new CommandBuild();
        //new CommandPause();
        //new CommandSetTime();
        //new CommandStart();

        this.lobby_sleepDelay = this.configDocument.getInt("lobby_sleepDelay");
        this.lobbyCountdown = this.configDocument.getInt("lobbyCountdown");
        this.preparingStartCountdown = this.configDocument.getInt("preparingStartCountdown");
        this.ingameCountdown = this.configDocument.getInt("ingameCountdown");
        this.pvpEnabledAfter = this.configDocument.getInt("pvpEnabledAfter");
        this.minPlayers = this.configDocument.getInt("minPlayers");

        this.gameManager = new GameManager();
        this.gameStateManager = new GameStateManager();
        this.scoreboardManager = new ScoreboardManager();
        this.worldBorderController = new WorldBorderController();

        this.SPECTATORS = new ArrayList<>();

        this.PAUSED = false;
        this.STARTED = false;

        Bukkit.getLogger().info("TTT wurde initialisiert!");
        Bukkit.getLogger().info("-----------------------------------");
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("-----------------------------------");
        Bukkit.getLogger().info("Aura wird deaktiviert");
        Bukkit.getLogger().info("Scheduler werden abgebrochen");
        Bukkit.getScheduler().cancelTasks(this);

        this.gameManager = null;
        this.gameStateManager = null;
        this.scoreboardManager = null;
        this.worldBorderController = null;

        if (!this.SPECTATORS.isEmpty()) this.SPECTATORS.clear();

        Bukkit.getLogger().info("Aura wurde deaktiviert");
        Bukkit.getLogger().info("-----------------------------------");
    }

    private void registerListener() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        try {
            for (ClassPath.ClassInfo classInfo : ClassPath.from(getInstance().getClassLoader()).getTopLevelClasses("de.voldechse.wintervillage.aura.listener")) {
                Class clazz = Class.forName(classInfo.getName());
                if (Listener.class.isAssignableFrom(clazz))
                    pluginManager.registerEvents((Listener) clazz.newInstance(), getInstance());
            }
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException exception) {
            Logger.getLogger(Aura_Unused.class.getName()).log(Level.SEVERE, null, exception);
        }
    }

    private void initializeGameConfig_notavailable() {
        if (Files.exists(Paths.get(getDataFolder().getAbsolutePath(), "config.json"))) return;

        Bukkit.getLogger().log(Level.WARNING, "config.json wird erstellt, da sie nicht gefunden wurde");

        Position location = new Position(0.0, 0.0, 0.0, 0.0f, 0.0f, "world");

        new Document("lobby_sleepDelay", 15)
                .append("lobbyCountdown", 60)
                .append("preparingStartCountdown", 5)
                .append("ingameCountdown", 20 * 60)
                .append("pvpEnabledAfter", 60)
                .append("minPlayers", 2)
                .append("lobbySpawn", location)
                .append("playerSpawn", location)
                //.saveAsConfig(new File(getDataFolder().getAbsolutePath(), "config.json"));
                .saveAsConfig(Paths.get(getDataFolder().getAbsolutePath(), "config.json"));

        Bukkit.getLogger().log(Level.INFO, "config.json wurde erstellt!");
    }

    private void initializeWorldBorderConfig_notAvailable() {
        if (Files.exists(Paths.get(getDataFolder().getAbsolutePath(), "worldborder.json"))) return;

        Bukkit.getLogger().log(Level.WARNING, "worldborder.json wird erstellt, da sie nicht gefunden wurde");

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

        new Document("world", "world")
                .append("controller", jsonElements)
                .saveAsConfig(Paths.get(getDataFolder().getAbsolutePath(), "worldborder.json"));

        Bukkit.getLogger().log(Level.INFO, "worldborder.json wurde erstellt!");
    }

    public void reloadConfigs() {
        this.configDocument = Document.loadDocument(Paths.get(getDataFolder().getAbsolutePath(), "config.json"));
        this.worldBorderDocument = Document.loadDocument(Paths.get(getDataFolder().getAbsolutePath(), "worldborder.json"));

        this.worldBorderController = new WorldBorderController();

        this.lobby_sleepDelay = this.configDocument.getInt("lobby_sleepDelay");
        this.lobbyCountdown = this.configDocument.getInt("lobbyCountdown");
        this.preparingStartCountdown = this.configDocument.getInt("preparingStartCountdown");
        this.ingameCountdown = this.configDocument.getInt("ingameCountdown");
        this.pvpEnabledAfter = this.configDocument.getInt("pvpEnabledAfter");
        this.minPlayers = this.configDocument.getInt("minPlayers");

        this.PVP_ENABLED = false;
        this.PAUSED = false;
        this.STARTED = false;
    }

    public void removeMetadata(Entity entity, String name) {
        if (entity.hasMetadata(name)) entity.removeMetadata(name, this);
    }

    public void setMetadata(Entity entity, String name, Object object) {
        this.removeMetadata(entity, name);
        entity.setMetadata(name, new FixedMetadataValue(this, object));
    }

    public Object getMetadata(Entity entity, String name) {
        if (entity.hasMetadata(name)) return entity.getMetadata(name);
        return null;
    }

    public boolean hasMetadata(Entity entity, String name) {
        return entity.hasMetadata(name);
    }

    public static Aura_Unused getInstance() {
        return instance;
    }
}
