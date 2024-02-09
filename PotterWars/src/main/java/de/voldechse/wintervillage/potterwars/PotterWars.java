package de.voldechse.wintervillage.potterwars;

import com.google.common.reflect.ClassPath;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.library.gradient.Gradient;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.potterwars.chest.ChestItem;
import de.voldechse.wintervillage.potterwars.chest.ChestManager;
import de.voldechse.wintervillage.potterwars.commands.*;
import de.voldechse.wintervillage.potterwars.gamestate.GameStateManager;
import de.voldechse.wintervillage.potterwars.kit.KitManager;
import de.voldechse.wintervillage.potterwars.spell.SpellManager;
import de.voldechse.wintervillage.potterwars.team.Team;
import de.voldechse.wintervillage.potterwars.team.TeamManager;
import de.voldechse.wintervillage.potterwars.team.position.TeamPosition;
import de.voldechse.wintervillage.potterwars.utils.GameManager;
import de.voldechse.wintervillage.potterwars.utils.ScoreboardManager;
import de.voldechse.wintervillage.potterwars.utils.border.WorldBorderController;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldCreator;
import org.bukkit.enchantments.Enchantment;
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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PotterWars extends JavaPlugin {

    private static PotterWars instance;
    public String serverPrefix;

    public GameStateManager gameStateManager;
    public ChestManager chestManager;
    public SpellManager spellManager;
    public TeamManager teamManager;
    public KitManager kitManager;
    public GameData gameData;
    public ScoreboardManager scoreboardManager;
    public GameManager gameManager;
    public WorldBorderController worldBorderController;

    public Document configDocument, /** chestsDocument, */ teamsDocument, worldBorderDocument;
    public List<Location> chestList;
    public List<UUID> SPECTATORS;

    public int maxPlayersInTeam,
            levelMultiplier,
            minPlayers,
            lobby_sleepDelay,
            lobbyCountdown,
            preparingStartCountdown,
            ingameCountdown,
            pvpEnabledAfter,
            worldBorderAfter,
            overtimeCountdown,
            PLAYING;

    public boolean OVERTIME, PVP_ENABLED, PAUSED, STARTED, USE_WORLDBORDER;

    @Override
    public void onLoad() {
        Bukkit.getLogger().info("-----------------------------------");
        Bukkit.getLogger().info("PotterWars wird initialisiert!");
        instance = this;

        this.serverPrefix = " " + Gradient.color("ElfWars", Color.WHITE, Color.CYAN) + " §8| §f";

        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        initializeTeamConfig_notAvailable();
        initializeGameConfig_notavailable();
        initializeWorldBorderConfig_notAvailable();
        //TODO: initializeChestsConfig_notAvailable();
    }

    @Override
    public void onEnable() {
        Bukkit.getLogger().info("-----------------------------------");
        Bukkit.getLogger().info("config.json | teams.json | worldborder.json werden initialisiert...");
        //this.chestsDocument = Document.loadDocument(new File(getDataFolder().getAbsolutePath(), "chests.json"));
        this.configDocument = Document.loadDocument(Paths.get(getDataFolder().getAbsolutePath(), "config.json"));
        this.teamsDocument = Document.loadDocument(Paths.get(getDataFolder().getAbsolutePath(), "teams.json"));
        this.worldBorderDocument = Document.loadDocument(Paths.get(getDataFolder().getAbsolutePath(), "worldborder.json"));

        if (Bukkit.getWorld("world_playing_potterwars") == null) Bukkit.createWorld(WorldCreator.name("world_playing_potterwars"));

        Bukkit.getLogger().info("Events werden registriert");
        this.registerListener();

        Bukkit.getLogger().info("Befehle werden registriert");
        new CommandAdmin();
        new CommandBuild();
        new CommandPause();
        new CommandRevive();
        new CommandSetTime();
        new CommandStart();

        Bukkit.getLogger().info("Initialisiere Manager");
        this.teamManager = new TeamManager();
        this.teamManager.loadFromConfig();
        this.gameManager = new GameManager();
        this.gameStateManager = new GameStateManager();
        this.spellManager = new SpellManager();
        this.kitManager = new KitManager();
        this.scoreboardManager = new ScoreboardManager();
        this.registerChestItems();

        Document map = this.configDocument.getDocument("gameData");
        this.setGameData(new GameData(map.getString("mapName"), map.getString("mapBuilder")));

        this.lobby_sleepDelay = this.configDocument.getInt("lobby_sleepDelay");
        this.lobbyCountdown = this.configDocument.getInt("lobbyCountdown");
        this.preparingStartCountdown = this.configDocument.getInt("preparingStartCountdown");
        this.ingameCountdown = this.configDocument.getInt("ingameCountdown");
        this.pvpEnabledAfter = this.configDocument.getInt("pvpEnabledAfter");
        this.worldBorderAfter = this.configDocument.getInt("worldBorderAfter");
        this.overtimeCountdown = this.configDocument.getInt("overtimeCountdown");
        this.minPlayers = this.configDocument.getInt("minPlayers");
        this.maxPlayersInTeam = this.configDocument.getInt("maxPlayersInTeam");
        this.levelMultiplier = this.configDocument.getInt("levelMultiplier");

        this.OVERTIME = false;
        this.PVP_ENABLED = false;
        this.PAUSED = false;
        this.STARTED = false;
        this.USE_WORLDBORDER = this.worldBorderDocument.getBoolean("enabled");
        if (this.USE_WORLDBORDER) this.worldBorderController = new WorldBorderController();

        this.SPECTATORS = new ArrayList<>();

        Bukkit.getLogger().info("PotterWars wurde initialisiert");
        Bukkit.getLogger().info("-----------------------------------");
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("-----------------------------------");
        Bukkit.getLogger().info("PotterWars wird deaktiviert");
        Bukkit.getLogger().info("Scheduler werden abgebrochen");
        Bukkit.getScheduler().cancelTasks(this);

        this.teamManager = null;
        this.gameManager = null;
        this.gameStateManager = null;
        this.spellManager = null;
        this.kitManager = null;
        this.scoreboardManager = null;
        this.chestManager = null;
        this.worldBorderController = null;

        if (!this.SPECTATORS.isEmpty()) this.SPECTATORS.clear();

        Bukkit.getLogger().info("PotterWars wurde deaktiviert");
        Bukkit.getLogger().info("-----------------------------------");
    }

    //TODO: rework
    /**
    private void registerWholeGame() {
        System.out.println("POTTERWARS: Kistenconfig initialisieren..");
        this.chestConfig = Document.loadDocument(Paths.get("plugins/GameConfig/chests.json"));
        this.chestList = new ArrayList<Location>();
        this.initializeChests();
    }

    public void initializeChests() {
        System.out.println("POTTERWARS: Initialisiere die Kisten auf der Karte " + this.getGameData().getMapName() + "...");
        if (this.chestConfig.keys().isEmpty()) {
            System.out.println("POTTERWARS - FEHLER: Es wurde kein Kisteneintrag gefunden!");
            return;
        }
        System.out.println("POTTERWARS: Kisteneinträge gefunden! Sollten Kisten nicht existieren (bzw. zerstört sein), werden sie bei Rundenstart platziert.");

        for (String chestsInRound : this.chestConfig.keys()) {
            if (chestsInRound.startsWith("chest")) {
                Document chest = this.chestConfig.getDocument(chestsInRound);

                int chestPositionX = chest.getInt("x");
                int chestPositionY = chest.getInt("y");
                int chestPositionZ = chest.getInt("z");
                String worldName = chest.getString("worldName");

                if (Bukkit.getWorld(worldName) == null) {
                    System.out.println("POTTERWARS - FEHLER: Die Welt " + worldName + " existiert nicht. Kiste: " + chestsInRound + " bei X:" + chestPositionX + ";Y:" + chestPositionY + ";Z:" + chestPositionZ);
                    return;
                }

                final World world = Bukkit.getWorld(worldName);
                Location location = new Location(world, chestPositionX, chestPositionY, chestPositionZ);
                if (!chestList.contains(location)) chestList.add(location);

                if (world.getBlockAt(chestPositionX, chestPositionY, chestPositionZ).getType() == Material.CHEST) {
                    continue;
                }
                world.getBlockAt(chestPositionX, chestPositionY, chestPositionZ).setType(Material.CHEST);

                System.out.println("POTTERWARS: Die Kiste " + chestsInRound + " (X:" + chestPositionX + ";Y:" + chestPositionY + ";Z:" + chestPositionZ + ") auf der Karte " + this.getGameData().getMapName() + " wurde platziert");
            } else System.out.println("POTTERWARS - FEHLER: Es wurde kein Kisteneintrag gefunden!");
        }

        this.registerChestItems();
    }

    public boolean isPotterWarsChest(Location locationToCheck) {
        return this.chestList.contains(locationToCheck);
    }
     **/

    private void registerChestItems() {
        Bukkit.getLogger().info("ChestItems werden initialisiert");

        List<ChestItem> chestItems = new ArrayList<ChestItem>();
        chestItems.add(new ChestItem(new ItemBuilder(Material.WOODEN_SWORD, 1).enchant(Enchantment.DAMAGE_ALL, 7).build(), 13, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.STONE_SWORD, 1).enchant(Enchantment.DAMAGE_ALL, 7).build(), 13, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.IRON_SWORD, 1).enchant(Enchantment.DAMAGE_ALL, 4).build(), 9, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.ARROW, 1).build(), 37, 4, 39));


        //Non-Enchanted -> Netherite
        chestItems.add(new ChestItem(new ItemBuilder(Material.NETHERITE_HELMET, 1).build(), 12, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.NETHERITE_CHESTPLATE, 1).build(), 11, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.NETHERITE_LEGGINGS, 1).build(), 11, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.NETHERITE_BOOTS, 1).build(), 10, 1, 1));
        //Enchanted Prot 1 -> Netherite
        chestItems.add(new ChestItem(new ItemBuilder(Material.NETHERITE_HELMET, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build(),6, 1,1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.NETHERITE_CHESTPLATE, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build(), 9,1,1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.NETHERITE_LEGGINGS, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build(), 6,1,1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.NETHERITE_BOOTS, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build(), 7,1,1));
        //Enchanted Prot 2 -> Netherite
        chestItems.add(new ChestItem(new ItemBuilder(Material.NETHERITE_CHESTPLATE, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build(), 5,1,1));


        //Non-Enchanted -> Diamond
        chestItems.add(new ChestItem(new ItemBuilder(Material.DIAMOND_HELMET, 1).build(), 17, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.DIAMOND_CHESTPLATE, 1).build(), 18, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.DIAMOND_LEGGINGS, 1).build(), 16, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.DIAMOND_BOOTS, 1).build(), 17, 1, 1));
        //Enchanted -> Diamond
        chestItems.add(new ChestItem(new ItemBuilder(Material.DIAMOND_HELMET, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build(),14, 1,1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.DIAMOND_CHESTPLATE, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build(), 13,1,1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.DIAMOND_LEGGINGS, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build(), 15,1,1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.DIAMOND_BOOTS, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build(), 13,1,1));


        //Non-Enchanted -> Iron
        chestItems.add(new ChestItem(new ItemBuilder(Material.IRON_CHESTPLATE, 1).build(), 21, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.IRON_BOOTS, 1).build(), 22, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.IRON_LEGGINGS, 1).build(), 23, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.IRON_HELMET, 1).build(), 22, 1, 1));
        //Enchanted -> Iron
        chestItems.add(new ChestItem(new ItemBuilder(Material.IRON_CHESTPLATE, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build(), 19, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.IRON_BOOTS, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build(), 20, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.IRON_LEGGINGS, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build(), 21, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.IRON_HELMET, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build(), 19, 1, 1));


        //Non-Enchanted -> Gold
        chestItems.add(new ChestItem(new ItemBuilder(Material.GOLDEN_HELMET, 1).build(), 31, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.GOLDEN_CHESTPLATE, 1).build(), 32, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.GOLDEN_LEGGINGS, 1).build(), 30, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.GOLDEN_BOOTS, 1).build(), 33, 1, 1));
        //Emchanted -> Gold
        chestItems.add(new ChestItem(new ItemBuilder(Material.GOLDEN_HELMET, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build(), 25, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.GOLDEN_CHESTPLATE, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build(), 27, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.GOLDEN_LEGGINGS, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build(), 25, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.GOLDEN_BOOTS, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build(), 27, 1, 1));


        //Non-Enchanted -> Chainmail
        chestItems.add(new ChestItem(new ItemBuilder(Material.CHAINMAIL_LEGGINGS, 1).build(), 38, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.CHAINMAIL_CHESTPLATE, 1).build(), 39, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.CHAINMAIL_BOOTS, 1).build(), 37, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.CHAINMAIL_HELMET, 1).build(), 38, 1, 1));
        //Enchanted -> Chainmail
        chestItems.add(new ChestItem(new ItemBuilder(Material.CHAINMAIL_LEGGINGS, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build(), 34, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.CHAINMAIL_CHESTPLATE, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build(), 33, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.CHAINMAIL_BOOTS, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build(), 35, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.CHAINMAIL_HELMET, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build(), 33, 1, 1));


        //Enchanted -> Leather
        chestItems.add(new ChestItem(new ItemBuilder(Material.LEATHER_HELMET, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build(), 40, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.LEATHER_CHESTPLATE, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3).build(), 41, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.LEATHER_LEGGINGS, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3).build(), 41, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.LEATHER_BOOTS, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build(), 38, 1, 1));


        chestItems.add(new ChestItem(new ItemBuilder(Material.BAKED_POTATO, 1, "§1B§de§br§at§2i§3e §1B§4o§ct§9t§9s §8B§7o§1h§cn§ee").build(), 49, 3, 11));
        chestItems.add(new ChestItem(new ItemBuilder(Material.COOKED_BEEF, 1).build(), 41, 7, 14));
        chestItems.add(new ChestItem(new ItemBuilder(Material.GOLDEN_CARROT, 1).build(), 35, 8, 18));
        chestItems.add(new ChestItem(new ItemBuilder(Material.GOLDEN_APPLE, 1).build(), 29, 3, 7));

        chestItems.add(new ChestItem(new ItemBuilder(Material.LAPIS_LAZULI, 1).build(), 31, 19, 27));
        chestItems.add(new ChestItem(new ItemBuilder(Material.SHIELD, 1).build(), 30, 1, 1));
        chestItems.add(new ChestItem(new ItemBuilder(Material.TOTEM_OF_UNDYING, 1).build(), 39, 1, 1));
        this.chestManager = new ChestManager("§cTruhe", chestItems, 14, 4, 27);

        Bukkit.getLogger().info("Es wurden " + chestItems.size() + " Items in Kisten gefunden");
    }

    private void registerListener() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        try {
            for (ClassPath.ClassInfo classInfo : ClassPath.from(getInstance().getClassLoader()).getTopLevelClasses("de.voldechse.wintervillage.potterwars.listener")) {
                Class clazz = Class.forName(classInfo.getName());
                if (Listener.class.isAssignableFrom(clazz))
                    pluginManager.registerEvents((Listener) clazz.newInstance(), getInstance());
            }
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException exception) {
            Logger.getLogger(PotterWars.class.getName()).log(Level.SEVERE, null, exception);
        }
    }

    private void initializeTeamConfig_notAvailable() {
        if (Files.exists(Paths.get(getDataFolder().getAbsolutePath(), "teams.json"))) return;

        Bukkit.getLogger().log(Level.WARNING, "teams.json wird erstellt, da sie nicht gefunden wurde");
        List<Team> temporaryList = new ArrayList<Team>();

        TeamPosition playerSpawn = new TeamPosition(0.0, 0.0, 0.0, 0.0f, 0.0f, "world");

        for (int i = 0; i <= 4; i++) temporaryList.add(new Team("WHITE_WOOL_BLOCK", i, "TEAM_" + i, "§f", new ArrayList<Player>(), playerSpawn));
        temporaryList.sort(Comparator.comparing(Team::getTeamId));

        JsonArray jsonElements = new JsonArray();
        temporaryList.forEach(team -> {
            Document teamData = new Document("teamBlock", team.teamBlock)
                    .append("teamId", team.teamId)
                    .append("teamName", team.teamName)
                    .append("teamPrefix", team.teamPrefix)
                    .append("playerSpawn", playerSpawn);
            jsonElements.add(teamData.obj());
        });

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("teams", jsonElements);

        //new Document(jsonObject).saveAsConfig(new File(getDataFolder().getAbsolutePath(), "teams.json"));
        new Document(jsonObject).saveAsConfig(Paths.get(getDataFolder().getAbsolutePath(), "teams.json"));

        Bukkit.getLogger().log(Level.INFO, "teams.json wurde erstellt!");
    }

    private void initializeGameConfig_notavailable() {
        if (Files.exists(Paths.get(getDataFolder().getAbsolutePath(), "config.json"))) return;

        Bukkit.getLogger().log(Level.WARNING, "config.json wird erstellt, da sie nicht gefunden wurde");

        GameData gameData = new GameData("NAME_OF_MAP", "MAP_BUILDER");
        TeamPosition otherLocation = new TeamPosition(0.0, 0.0, 0.0, 0.0f, 0.0f, "world");

        new Document("gameData", gameData)
                .append("lobby_sleepDelay", 15)
                .append("lobbyCountdown", 60)
                .append("preparingStartCountdown", 5)
                .append("ingameCountdown", 12 * 60)
                .append("pvpEnabledAfter", 45)
                .append("worldBorderAfter", 0)
                .append("overtimeCountdown", 5 * 60)
                .append("minPlayers", 2)
                .append("maxPlayersInTeam", 2)
                .append("levelMultiplier", 1)
                .append("lobbySpawn", otherLocation)
                //.saveAsConfig(new File(getDataFolder().getAbsolutePath(), "config.json"));
                        .saveAsConfig(Paths.get(getDataFolder().getAbsolutePath(), "config.json"));

        Bukkit.getLogger().log(Level.INFO, "config.json wurde erstellt!");
    }

    private void initializeWorldBorderConfig_notAvailable() {
        if (Files.exists(Paths.get(getDataFolder().getAbsolutePath(), "worldborder.json"))) return;

        Bukkit.getLogger().log(Level.WARNING, "worldborder.json wird erstellt, da sie nicht gefunden wurde");

        WorldBorderController.BorderPhase phase1 = new WorldBorderController.BorderPhase(0, new WorldBorderController.Center(false, 0.0, 0.0), 1000, 300, 180, 60);
        WorldBorderController.BorderPhase phase2 = new WorldBorderController.BorderPhase(1, new WorldBorderController.Center(false, 0.0, 0.0), 300, 200, 120, 45);
        WorldBorderController.BorderPhase phase3 = new WorldBorderController.BorderPhase(2, new WorldBorderController.Center(false, 0.0, 0.0), 200, 120, 90, 45);
        WorldBorderController.BorderPhase phase4 = new WorldBorderController.BorderPhase(3, new WorldBorderController.Center(true, 0.0, 0.0), 120, 90, 60, 30);
        WorldBorderController.BorderPhase phase5 = new WorldBorderController.BorderPhase(4, new WorldBorderController.Center(true, 0.0, 0.0), 90, 25, 45, 0);

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
                .append("world", "world_playing_potterwars")
                .append("controller", jsonElements)
                .saveAsConfig(Paths.get(getDataFolder().getAbsolutePath(), "worldborder.json"));

        Bukkit.getLogger().log(Level.INFO, "worldborder.json wurde erstellt!");
    }

    public void reloadConfigs() {
        this.configDocument = Document.loadDocument(Paths.get(getDataFolder().getAbsolutePath(), "config.json"));
        this.teamsDocument = Document.loadDocument(Paths.get(getDataFolder().getAbsolutePath(), "teams.json"));
        this.worldBorderDocument = Document.loadDocument(Paths.get(getDataFolder().getAbsolutePath(), "worldborder.json"));
        this.teamManager.getTeamList().clear();
        this.teamManager.loadFromConfig();

        this.lobby_sleepDelay = this.configDocument.getInt("lobby_sleepDelay");
        this.lobbyCountdown = this.configDocument.getInt("lobbyCountdown");
        this.preparingStartCountdown = this.configDocument.getInt("preparingStartCountdown");
        this.ingameCountdown = this.configDocument.getInt("ingameCountdown");
        this.pvpEnabledAfter = this.configDocument.getInt("pvpEnabledAfter");
        this.worldBorderAfter = this.configDocument.getInt("worldBorderAfter");
        this.overtimeCountdown = this.configDocument.getInt("overtimeCountdown");
        this.minPlayers = this.configDocument.getInt("minPlayers");
        this.maxPlayersInTeam = this.configDocument.getInt("maxPlayersInTeam");
        this.levelMultiplier = this.configDocument.getInt("levelMultiplier");

        this.OVERTIME = false;
        this.PVP_ENABLED = false;
        this.PAUSED = false;
        this.STARTED = false;
        this.USE_WORLDBORDER = this.worldBorderDocument.getBoolean("enabled");
        if (this.USE_WORLDBORDER) this.worldBorderController = new WorldBorderController();
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

    public static PotterWars getInstance() {
        return instance;
    }

    public void setGameData(GameData gameData) {
        this.gameData = gameData;
    }
}