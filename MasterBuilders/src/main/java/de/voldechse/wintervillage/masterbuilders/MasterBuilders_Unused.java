package de.voldechse.wintervillage.masterbuilders;

import com.google.common.reflect.ClassPath;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.library.gradient.Gradient;
import de.voldechse.wintervillage.masterbuilders.commands.*;
import de.voldechse.wintervillage.masterbuilders.gamestate.GameState;
import de.voldechse.wintervillage.masterbuilders.gamestate.GameStateManager;
import de.voldechse.wintervillage.masterbuilders.teams.Team;
import de.voldechse.wintervillage.masterbuilders.teams.TeamManager;
import de.voldechse.wintervillage.masterbuilders.teams.position.Team_Corner;
import de.voldechse.wintervillage.masterbuilders.teams.position.Team_Entity;
import de.voldechse.wintervillage.masterbuilders.utils.GameManager;
import de.voldechse.wintervillage.masterbuilders.utils.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MasterBuilders_Unused extends JavaPlugin {

    private static MasterBuilders_Unused instance;
    public String serverPrefix;

    public TeamManager teamManager;
    public GameManager gameManager;
    public GameStateManager gameStateManager;
    public ScoreboardManager scoreboardManager;

    public Document teamDocument, configDocument;

    public int allowedHeightDifference,
            lobby_sleepDelay,
            lobbyCountdown,
            voteThemesCountdown,
            buildingCountdown,
            voteBuildingsCountdownPerPlayer,
            minPlayers,
            maxPlayersInTeam,
            teleportationTreshold,
            PLAYING;
    public double percentageToSkipBuildingPhase;
    public boolean SPAWN_VILLAGERS,
            isStarted,
            isPaused,
            THEME_ALREADY_SET;

    @Override
    public void onLoad() {
        Bukkit.getLogger().info("-----------------------------------");
        Bukkit.getLogger().info("MasterBuilders wird initialisiert!");
        instance = this;
        this.serverPrefix = " " + Gradient.color("Santas Workshop", java.awt.Color.GREEN, java.awt.Color.CYAN) + " §8| §f";

        initializeTeamConfig_notAvailable();
        initializeGameConfig_notavailable();
    }

    @Override
    public void onEnable() {
        Bukkit.getLogger().info("-----------------------------------");
        Bukkit.getLogger().info("teams.json | config.json werden initialisiert...");
        this.teamDocument = Document.loadDocument(Paths.get(getDataFolder().getAbsolutePath(), "teams.json"));
        this.configDocument = Document.loadDocument(Paths.get(getDataFolder().getAbsolutePath(), "config.json"));

        if (Bukkit.getWorld("world_playing_masterbuilders") == null) Bukkit.createWorld(WorldCreator.name("world_playing_masterbuilders"));

        Bukkit.getLogger().info("Events werden registriert");
        this.registerListener();

        Bukkit.getLogger().info("Befehle werden registriert");
        new CommandAdmin();
        new CommandBuild();
        new CommandFix();
        new CommandPause();
        new CommandSetTime();
        new CommandSkip();
        new CommandStart();

        this.teamManager = new TeamManager();
        this.teamManager.loadFromConfig();
        this.gameManager = new GameManager();
        this.gameStateManager = new GameStateManager();
        this.scoreboardManager = new ScoreboardManager();

        this.allowedHeightDifference = this.configDocument.getInt("allowedHeightDifference");
        this.lobby_sleepDelay = this.configDocument.getInt("lobby_sleepDelay");
        this.lobbyCountdown = this.configDocument.getInt("lobbyCountdown");
        this.voteThemesCountdown = this.configDocument.getInt("voteThemesCountdown");
        this.buildingCountdown = this.configDocument.getInt("buildingCountdown");
        this.voteBuildingsCountdownPerPlayer = this.configDocument.getInt("voteBuildingsCountdownPerPlayer");
        this.minPlayers = this.configDocument.getInt("minPlayers");
        this.maxPlayersInTeam = this.configDocument.getInt("maxPlayersInTeam");
        this.percentageToSkipBuildingPhase = this.configDocument.getDouble("percentageToSkipBuildingPhase");
        this.teleportationTreshold = this.configDocument.getInt("teleportationTreshold");

        this.SPAWN_VILLAGERS = this.configDocument.getBoolean("spawnVillagers");

        isPaused = false;
        isStarted = false;
        THEME_ALREADY_SET = false;

        Bukkit.getLogger().info("MasterBuilders wurde initialisiert!");
        Bukkit.getLogger().info("-----------------------------------");
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("-----------------------------------");
        Bukkit.getLogger().info("MasterBuilders wird deaktiviert");
        Bukkit.getLogger().info("Scheduler werden abgebrochen");
        Bukkit.getScheduler().cancelTasks(this);

        this.teamManager = null;
        this.gameManager = null;
        this.gameStateManager = null;
        this.scoreboardManager = null;

        Bukkit.getLogger().info("MasterBuilders wurde deaktiviert");
        Bukkit.getLogger().info("-----------------------------------");
    }

    private void registerListener() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        try {
            for (ClassPath.ClassInfo classInfo : ClassPath.from(getInstance().getClassLoader()).getTopLevelClasses("de.voldechse.wintervillage.masterbuilders.listener")) {
                Class clazz = Class.forName(classInfo.getName());
                if (Listener.class.isAssignableFrom(clazz))
                    pluginManager.registerEvents((Listener) clazz.newInstance(), getInstance());
            }
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException exception) {
            Logger.getLogger(MasterBuilders.class.getName()).log(Level.SEVERE, null, exception);
        }
    }

    private void initializeTeamConfig_notAvailable() {
        if (Files.exists(Paths.get(getDataFolder().getAbsolutePath(), "teams.json"))) return;

        Bukkit.getLogger().warning("teams.json wurde nicht gefunden, weshalb sie nun erstellt wird");

        List<Team> temporaryList = new ArrayList<Team>();

        Team_Corner testCorner = new Team_Corner(0.0, 0.0, 0.0, "world");
        Team_Entity entitySpawn = new Team_Entity(0.0, 0.0, 0.0, 0.0F, 0.0F, "world");
        Document cornerData = new Document("x", testCorner.getX())
                .append("y", testCorner.getY())
                .append("z", testCorner.getZ())
                .append("world", "world");
        Document entityData = new Document("x", entitySpawn.getX())
                .append("y", entitySpawn.getY())
                .append("z", entitySpawn.getZ())
                .append("yaw", 0.0)
                .append("pitch", 0.0)
                .append("world", "world");

        for (int i = 0; i <= 4; i++)
            temporaryList.add(new Team(i, testCorner, testCorner, entitySpawn, entitySpawn, new ArrayList<Player>(), 0));
        temporaryList.sort(Comparator.comparing(team -> team.teamId));

        JsonArray jsonElements = new JsonArray();
        temporaryList.forEach(team -> {
            Document plotData = new Document("teamId", team.teamId)
                    .append("cornerA", cornerData)
                    .append("cornerB", cornerData)
                    .append("playerSpawn", entityData)
                    .append("villagerSpawn", entityData);
            jsonElements.add(plotData.obj());
        });

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("teams", jsonElements);

        new Document(jsonObject).saveAsConfig(Paths.get(getDataFolder().getAbsolutePath(), "teams.json"));

        Bukkit.getLogger().info("teams.json wurde erstellt!");
    }

    private void initializeGameConfig_notavailable() {
        if (Files.exists(Paths.get(getDataFolder().getAbsolutePath(), "config.json"))) return;

        Bukkit.getLogger().warning("config.json wurde nicht gefunden, weshalb sie nun erstellt wird");

        Team_Entity lobbySpawn = new Team_Entity(0.0, 0.0, 0.0, 0.0F, 0.0F, "world");

        new Document("allowedHeightDifference", 40)
                .append("lobby_sleepDelay", 15)
                .append("lobbyCountdown", 60)
                .append("voteThemesCountdown", 15)
                .append("buildingCountdown", 8 * 60)
                .append("voteBuildingsCountdownPerPlayer", 15)
                .append("minPlayers", 2)
                .append("maxPlayersPerTeam", 1)
                .append("percentageToSkipBuildingPhase", 66.0D)
                .append("teleportationTreshold", 60)
                .append("spawnVillagers", true)
                .append("lobbySpawn", lobbySpawn)
                .appendList("themes", Arrays.asList("theme1", "theme2", "theme3", "theme4", "theme5", "theme6", "theme7", "theme8"))
                .saveAsConfig(Paths.get(getDataFolder().getAbsolutePath(),"config.json"));

        Bukkit.getLogger().info("config.json wurde erstellt!");
    }

    public void reloadConfigs() {
        reloadGameConfig();
        reloadTeams();
    }

    public void reloadTeams() {
        this.teamDocument = Document.loadDocument(Paths.get(getDataFolder().getAbsolutePath(), "teams.json"));
        this.teamManager.getTeamList().clear();
        this.teamManager.loadFromConfig();
    }

    public void reloadGameConfig() {
        this.configDocument = Document.loadDocument(Paths.get(getDataFolder().getAbsolutePath(), "config.json"));

        this.allowedHeightDifference = this.configDocument.getInt("allowedHeightDifference");
        this.lobby_sleepDelay = this.configDocument.getInt("lobby_sleepDelay");
        this.lobbyCountdown = this.configDocument.getInt("lobbyCountdown");
        this.voteThemesCountdown = this.configDocument.getInt("voteThemesCountdown");
        this.buildingCountdown = this.configDocument.getInt("buildingCountdown");
        this.voteBuildingsCountdownPerPlayer = this.configDocument.getInt("voteBuildingsCountdownPerPlayer");
        this.minPlayers = this.configDocument.getInt("minPlayers");
        this.maxPlayersInTeam = this.configDocument.getInt("maxPlayersInTeam");
        this.percentageToSkipBuildingPhase = this.configDocument.getDouble("percentageToSkipBuildingPhase");
        this.teleportationTreshold = this.configDocument.getInt("teleportationTreshold");

        this.SPAWN_VILLAGERS = this.configDocument.getBoolean("spawnVillagers");

        isPaused = false;
        isStarted = false;
        THEME_ALREADY_SET = false;
    }

    public GameStateManager getGameStateManager() {
        return gameStateManager;
    }

    public GameState getCurrentGameState() {
        return gameStateManager.currentGameState();
    }

    public void removeMetadata(Entity entity, String name) {
        if (entity.hasMetadata(name)) entity.removeMetadata(name, this);
    }

    public void setMetadata(Player entity, String name, Object object) {
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

    public static MasterBuilders_Unused getInstance() {
        return instance;
    }
}