package de.voldechse.wintervillage.ttt;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.google.common.reflect.ClassPath;
import com.mojang.authlib.properties.Property;
import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.library.gradient.Gradient;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.commands.*;
import de.voldechse.wintervillage.ttt.game.corpse.CorpseEntity;
import de.voldechse.wintervillage.ttt.game.corpse.player.SkinData;
import de.voldechse.wintervillage.ttt.game.tester.Tester;
import de.voldechse.wintervillage.ttt.game.tester.TesterSetup;
import de.voldechse.wintervillage.ttt.gamestate.GameStateManager;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import de.voldechse.wintervillage.ttt.roles.Role;
import de.voldechse.wintervillage.ttt.roles.RoleManager;
import de.voldechse.wintervillage.ttt.roles.items.RoleItemManager;
import de.voldechse.wintervillage.ttt.utils.GameManager;
import de.voldechse.wintervillage.ttt.utils.ScoreboardManager;
import de.voldechse.wintervillage.ttt.utils.position.PositionBlock;
import de.voldechse.wintervillage.ttt.utils.position.PositionEntity;
import de.voldechse.wintervillage.ttt.utils.position.PositionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldCreator;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TTT_Unused extends JavaPlugin {

    private static TTT_Unused instance;
    public String serverPrefix;

    public Map<UUID, SkinData> playerSkinData;
    public Map<Integer, CorpseEntity> CORPSES_MAP;
    public Map<UUID, ArmorStand> IDENTIFICATION_MAP_TIMER, IDENTIFICATION_MAP_KILLED_BY;
    public Map<Integer, ArmorStand> SNEAK_ARMORSTANDS_USELESS;
    public List<ArmorStand> HOLOGRAM_ARMORSTANDS;
    public List<Material> ALLOWED_ITEMS_TO_DROP;
    public List<UUID> SPECTATORS;
    public Property deadSkinTexture;

    public RoleManager roleManager;
    public RoleItemManager roleItemManager;
    public GameManager gameManager;
    public GameStateManager gameStateManager;
    public ScoreboardManager scoreboardManager;
    public TesterSetup testerSetup;
    public PositionManager positionManager;

    public Document configDocument, testerDocument, spawnsDocument, chestsDocument;

    public int lobby_sleepDelay, lobbyCountdown, protectionCountdown, ingameCountdown, minPlayers, PLAYING;

    public double maxPercent_innocents, maxPercent_detectives, maxPercent_traitors;

    public boolean PAUSED, STARTED;

    public ProtocolManager protocolManager;

    public ItemStack innocentChestplate, detectiveChestplate, traitorChestplate, shop;


    @Override
    public void onLoad() {
        Bukkit.getLogger().info("-----------------------------------");
        Bukkit.getLogger().info("TTT wird initialisiert!");
        instance = this;

        this.serverPrefix = " " + Gradient.color("Krampus Hunt", Color.RED, Color.YELLOW) + " §8| §f";

        initializeGameConfig_notAvailable();
        initializeTesterConfig_notAvailable();
        initializeSpawnsConfig_notAvailable();
        initializeChestsConfig_notAvailable();
    }

    @Override
    public void onEnable() {
        Bukkit.getLogger().info("-----------------------------------");
        Bukkit.getLogger().info("config.json | tester.json | spawns.json | chests.json werden initialisiert...");
        this.configDocument = Document.loadDocument(Paths.get(getDataFolder().getAbsolutePath(), "config.json"));
        this.testerDocument = Document.loadDocument(Paths.get(getDataFolder().getAbsolutePath(), "tester.json"));
        this.spawnsDocument = Document.loadDocument(Paths.get(getDataFolder().getAbsolutePath(), "spawns.json"));
        this.chestsDocument = Document.loadDocument(Paths.get(getDataFolder().getAbsolutePath(), "chests.json"));

        if (Bukkit.getWorld("world_playing_ttt") == null) Bukkit.createWorld(WorldCreator.name("world_playing_ttt"));

        Bukkit.getLogger().info("Events werden registriert");
        registerListener();

        Bukkit.getLogger().info("Befehle werden registriert");
        new CommandAdmin();
        new CommandBuild();
        new CommandMrFrost();
        new CommandElf();
        new CommandPause();
        new CommandSetTime();
        new CommandShop();
        new CommandStart();
        new CommandKrampus();

        this.maxPercent_innocents = this.configDocument.getDouble("maxPercent_INNOCENT");
        this.maxPercent_detectives = this.configDocument.getDouble("maxPercent_DETECTIVE");
        this.maxPercent_traitors = this.configDocument.getDouble("maxPercent_TRAITOR");
        this.lobby_sleepDelay = this.configDocument.getInt("lobby_sleepDelay");
        this.lobbyCountdown = this.configDocument.getInt("lobbyCountdown");
        this.protectionCountdown = this.configDocument.getInt("protectionCountdown");
        this.ingameCountdown = this.configDocument.getInt("ingameCountdown");
        this.minPlayers = this.configDocument.getInt("minPlayers");

        this.roleManager = new RoleManager();
        this.roleItemManager = new RoleItemManager();
        //this.roleManager.roleList.add(new Role(0, "Innocent", "§a", "§7Unterstütze die Detectives dabei die Traitor zu enttarnen", maxPercent_innocents, new ArrayList<Player>()));
        //this.roleManager.roleList.add(new Role(1, "Detective", "§9", "§7Identifiziere Leichen und enttarne die Traitor", maxPercent_detectives, new ArrayList<Player>()));
        //this.roleManager.roleList.add(new Role(2, "Traitor", "§4", "§fTöte unauffällig alle Innocents und Detectives", maxPercent_traitors, new ArrayList<Player>()));

        this.roleManager.roleList.add(new Role(0, "Elf", "§a", "§7Unterstütze die Frosts dabei die Krampusse zu enttarnen", maxPercent_innocents, new ArrayList<Player>()));
        this.roleManager.roleList.add(new Role(1, "Mr. Frost", "§9", "§7Identifiziere Leichen und enttarne die Krampusse", maxPercent_detectives, new ArrayList<Player>()));
        this.roleManager.roleList.add(new Role(2, "Krampus", "§4", "§fTöte unaufällig alle Elfen und Frosts", maxPercent_traitors, new ArrayList<Player>()));

        Bukkit.getLogger().info("Es wurden folgende Teams erstellt");
        for (Role role : this.roleManager.roleList) Bukkit.getLogger().info(role.toString());

        this.gameManager = new GameManager();
        this.gameStateManager = new GameStateManager();
        this.scoreboardManager = new ScoreboardManager();
        this.setupTester();
        Arrays.stream(this.testerSetup.testerLamps).forEach(location -> location.getBlock().setType(this.testerSetup.idling));
        Arrays.stream(this.testerSetup.barrier).forEach(location -> location.getBlock().setType(Material.AIR));
        Tester.fillRectangle(this.testerSetup.cornerA, this.testerSetup.cornerB, this.testerSetup.floorMaterial);

        this.positionManager = new PositionManager();
        this.positionManager.readSpawns();
        this.positionManager.readChests();

        this.deadSkinTexture = new Property("textures",
                "ewogICJ0aW1lc3RhbXAiIDogMTY0OTA4MTM1MzEwMywKICAicHJvZmlsZUlkIiA6ICI4N2RiMmNjNWY4Y2I0MjI4YTU0OGRiMzJlM2Y0NmFmNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJZVG1hdGlhczEzbG9sIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzM4YzVlYzZmNGMzMDlkNmQxZjIyNWQzNzA3MzEwZGE5ZGM0ZjZjNjU1NTVlYmFiYWViNjc4ZDc4Mjg5OWNiNGMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                "g6RBsGyST1Ccd0jThycT+YQLqbLXBDISHjDLQmJ+2edyr0zkYhTo31ikm00e6gTsS9S76LKKrHDc5iO3A8MprNUTWlXTNd/XyWqWQ5Epo81WYECUsbDmulH0dooRixvWKz/zrWD+4SSZiRF7OSRYPM2slFZfTe9IwjxKhNduxU2dH6hChKyDHX1ZQ+vmhN4pPru/6TDuFkig+kN6TMlcNFy/xR94i+GoQKWppgDQgGhJyJBdRNuZ65GR0xd433itZ7xa3EkG/BK7FAZ48zbOV6p4qUODKYH5cRFLYrKDIE2O8JDWFEfh7JDqNnKRIwBH95EsToEdwnLcNrS18Mv1iySXJnanvYnXSfIJ9zAAn5UsorOcbkXncEjJ7dutqk5Q+m3Nr9wLre+D+YbXgwCxtivKecBTpp67wu5B5WQoZYpqimtZk6ThsN/gUVc1O2gY62bDwy/dBYBqtd7+vD2SgSPhhH++pZK4CJIGiuRNgo+ZnntMTfTs9zMnaMAMGcD0vabnPX1JsxKkQaou55YjLV4M4MtTJKWuGcftAO+c7a489apLhZjwkf3bzdUT5VHqNWBKwlMZS14DV1uUl8ymciByU61Ws/WB5VDj8wBXV3T13z193Piopzi3nVD3KT5gBVD1/yXRRBv0bZCPXoHw6spqej5xnvTr9VwoQL5eAHI=");
        this.playerSkinData = new HashMap<>();
        this.CORPSES_MAP = new HashMap<>();

        this.SNEAK_ARMORSTANDS_USELESS = new HashMap<>();

        this.IDENTIFICATION_MAP_TIMER = new HashMap<>();
        this.IDENTIFICATION_MAP_KILLED_BY = new HashMap<>();

        this.HOLOGRAM_ARMORSTANDS = new ArrayList<>();
        this.ALLOWED_ITEMS_TO_DROP = new ArrayList<>(Arrays.asList(
                Material.WOODEN_SWORD,
                Material.STONE_SWORD,
                Material.IRON_SWORD,
                Material.BOW,
                Material.ARROW
        ));

        this.SPECTATORS = new ArrayList<>();

        PAUSED = false;
        STARTED = false;

        Bukkit.getLogger().info("Items für das Spiel werden deklariert");
        innocentChestplate = new ItemBuilder(Material.LEATHER_CHESTPLATE, 1).build();
        LeatherArmorMeta innocentMeta = (LeatherArmorMeta) innocentChestplate.getItemMeta();
        innocentMeta.setColor(org.bukkit.Color.GREEN);
        innocentMeta.setUnbreakable(true);
        innocentChestplate.setItemMeta(innocentMeta);

        detectiveChestplate = new ItemBuilder(Material.LEATHER_CHESTPLATE, 1).build();
        LeatherArmorMeta detectiveMeta = (LeatherArmorMeta) detectiveChestplate.getItemMeta();
        detectiveMeta.setColor(org.bukkit.Color.BLUE);
        detectiveMeta.setUnbreakable(true);
        detectiveChestplate.setItemMeta(detectiveMeta);

        traitorChestplate = new ItemBuilder(Material.LEATHER_CHESTPLATE, 1).build();
        LeatherArmorMeta traitorMeta = (LeatherArmorMeta) traitorChestplate.getItemMeta();
        traitorMeta.setColor(org.bukkit.Color.RED);
        traitorMeta.setUnbreakable(true);
        traitorChestplate.setItemMeta(traitorMeta);

        shop = new ItemBuilder(Material.EMERALD, 1, "§aShop").build();

        Bukkit.getLogger().info("Initializing protocol manager");
        protocolManager = ProtocolLibrary.getProtocolManager();
        this.initializePacketListening();

        Bukkit.getLogger().info("TTT wurde initialisiert!");
        Bukkit.getLogger().info("-----------------------------------");
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("-----------------------------------");
        Bukkit.getLogger().info("TTT wird deaktiviert");
        Bukkit.getLogger().info("Scheduler werden abgebrochen");
        Bukkit.getScheduler().cancelTasks(this);

        this.deadSkinTexture = null;
        if (!this.playerSkinData.isEmpty()) this.playerSkinData.clear();
        if (!this.CORPSES_MAP.isEmpty()) this.CORPSES_MAP.clear();

        if (!this.ALLOWED_ITEMS_TO_DROP.isEmpty()) this.ALLOWED_ITEMS_TO_DROP.clear();

        if (!this.SPECTATORS.isEmpty()) this.SPECTATORS.clear();

        if (!this.roleManager.roleList.isEmpty()) this.roleManager.roleList.clear();

        if (!this.positionManager.ARMORSTANDS_EDITABLE.isEmpty()) {
            this.positionManager.ARMORSTANDS_EDITABLE.forEach(((uuid, armorStand) -> armorStand.remove()));
            this.positionManager.ARMORSTANDS_EDITABLE.clear();
        }

        if (!this.positionManager.spawnPositions.isEmpty()) this.positionManager.spawnPositions.clear();

        Bukkit.getLogger().info("TTT wurde deaktiviert");
        Bukkit.getLogger().info("-----------------------------------");
    }

    private void initializePacketListening() {
        protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL,
                PacketType.Play.Server.ENTITY_EQUIPMENT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (gameStateManager.currentGameState().getGameStatePhase() != Types.INGAME) return;

                Player packetReceiver = event.getPlayer();

                PacketContainer packetContainer = event.getPacket();
                int entityId = packetContainer.getIntegers().read(0);

                Player equipped = getPlayer(entityId);
                if (equipped == null) return;

                Role packetReceiversRole = null;
                if (roleManager.getRole(packetReceiver) != null)
                    packetReceiversRole = roleManager.getRole(packetReceiver);

                if (roleManager.getRole(equipped) == null) return;
                Role packetSendersRole = roleManager.getRole(equipped);

                List<Pair<EnumWrappers.ItemSlot, ItemStack>> item = packetContainer.getSlotStackPairLists().read(0);
                //List<Pair<EnumWrappers.ItemSlot, ItemStack>> sendingItems = new ArrayList<>();

                for (Pair<EnumWrappers.ItemSlot, ItemStack> itemSlotItemStackPair : item) {
                    EnumWrappers.ItemSlot itemSlot = itemSlotItemStackPair.getFirst();

                    ItemStack itemStack = item.get(0).getSecond();
                    if (itemStack == null) return;

                    if (itemSlot == EnumWrappers.ItemSlot.MAINHAND) {
                        if (itemStack.getType() == Material.AIR || isMaterialOfInterest(itemStack.getType())) continue;
                        if (packetSendersRole.roleId != 2) continue;
                        if (equipped.hasMetadata("BUSTED_TRAITOR")) continue;
                        if (gameManager.isSpectator(packetReceiver)) continue;
                        if (packetReceiversRole != null
                                && packetReceiversRole.roleId == packetSendersRole.roleId) continue;
                        //if (gameManager.isSpectator(packetReceiver) && packetSendersRole.roleId != 2) continue;

                        //item.clear();
                        //item.add(new Pair<>(EnumWrappers.ItemSlot.MAINHAND, new ItemStack(Material.AIR)));

                        item.remove(itemSlotItemStackPair);
                        item.add(new Pair<>(EnumWrappers.ItemSlot.MAINHAND, new ItemStack(Material.AIR)));
                    }

                    /**
                    if (itemSlot == EnumWrappers.ItemSlot.CHEST) {
                        if (itemStack.getType() != Material.LEATHER_CHESTPLATE) continue;
                        //if (packetSendersRole.roleId == 1) continue;
                        if (packetSendersRole.roleId != 2) continue;
                        if (equipped.hasMetadata("BUSTED_TRAITOR")) continue;
                        if (gameManager.isSpectator(packetReceiver)) continue;
                        if (packetReceiversRole != null
                                && packetReceiversRole.roleId == packetSendersRole.roleId) continue;
                        //if (gameManager.isSpectator(packetReceiver) && packetSendersRole.roleId != 2) continue;

                        //INNOCENT AS FAKE CHESTPLATE

                        //item.clear();
                        //item.add(new Pair<>(EnumWrappers.ItemSlot.CHEST, innocentChestplate));

                        item.remove(itemSlotItemStackPair);
                        item.add(new Pair<>(EnumWrappers.ItemSlot.CHEST, innocentChestplate));
                    }
                     */

                    if (itemSlot == EnumWrappers.ItemSlot.FEET) {
                        if (itemStack.getType() != Material.LEATHER_BOOTS) continue;
                        if (packetSendersRole.roleId != 2) continue;
                        if (equipped.hasMetadata("BUSTED_TRAITOR")) continue;
                        if (gameManager.isSpectator(packetReceiver)) continue;
                        if (packetReceiversRole != null
                                && packetReceiversRole.roleId == packetSendersRole.roleId) continue;
                        //if (gameManager.isSpectator(packetReceiver) && packetSendersRole.roleId != 2) continue;

                        //item.clear();
                        //item.add(new Pair<>(EnumWrappers.ItemSlot.FEET, new ItemStack(Material.AIR)));

                        item.remove(itemSlotItemStackPair);
                        item.add(new Pair<>(EnumWrappers.ItemSlot.FEET, new ItemStack(Material.AIR)));
                    }
                }

                packetContainer.getSlotStackPairLists().write(0, item);
            }
        });
    }

    private boolean isMaterialOfInterest(Material material) {
        List<Material> materialsOfInterest = Arrays.asList(
                Material.WOODEN_SWORD,
                Material.STONE_SWORD,
                Material.IRON_SWORD,
                Material.BOW,
                Material.ARROW,
                Material.EMERALD
        );
        return materialsOfInterest.contains(material);
    }

    private void registerListener() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        try {
            for (ClassPath.ClassInfo classInfo : ClassPath.from(getInstance().getClassLoader()).getTopLevelClasses("de.voldechse.wintervillage.ttt.listener")) {
                Class clazz = Class.forName(classInfo.getName());
                if (Listener.class.isAssignableFrom(clazz))
                    pluginManager.registerEvents((Listener) clazz.newInstance(), getInstance());
            }
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException exception) {
            Logger.getLogger(TTT_Unused.class.getName()).log(Level.SEVERE, null, exception);
        }
        pluginManager.registerEvents(new Tester(), getInstance());
    }

    private void initializeGameConfig_notAvailable() {
        if (Files.exists(Paths.get(getDataFolder().getAbsolutePath(), "config.json"))) return;

        Bukkit.getLogger().warning("config.json wurde nicht gefunden, weshalb sie nun erstellt wird");

        PositionEntity lobbySpawn = new PositionEntity(0.0, 0.0, 0.0, 0.0F, 0.0F, "world");
        Document lobbySpawnDocument = new Document("x", lobbySpawn.getX())
                .append("y", lobbySpawn.getY())
                .append("z", lobbySpawn.getZ())
                .append("yaw", lobbySpawn.getYaw())
                .append("pitch", lobbySpawn.getPitch())
                .append("world", lobbySpawn.getWorld());

        new Document("lobby_sleepDelay", 15)
                .append("lobbyCountdown", 60)
                .append("protectionCountdown", 35)
                .append("ingameCountdown", 12 * 60)
                .append("minPlayers", 6)
                .append("maxPercent_INNOCENT", 52.5)
                .append("maxPercent_DETECTIVE", 17.5)
                .append("maxPercent_TRAITOR", 30)
                .append("lobbySpawn", lobbySpawnDocument)
                .saveAsConfig(Paths.get(getDataFolder().getAbsolutePath(), "config.json"));

        Bukkit.getLogger().info("config.json wurde erstellt!");
    }

    private void initializeTesterConfig_notAvailable() {
        if (Files.exists(Paths.get(getDataFolder().getAbsolutePath(), "tester.json"))) return;

        Bukkit.getLogger().warning("tester.json wurde nicht gefunden, weshalb sie nun erstellt wird");

        PositionEntity position = new PositionEntity(0.0, 0.0, 0.0, 0.0F, 0.0F, "world");
        Document location = new Document("x", position.getX())
                .append("y", position.getY())
                .append("z", position.getZ())
                .append("world", position.getWorld());
        Document entitySpawn = new Document("x", position.getX())
                .append("y", position.getY())
                .append("z", position.getZ())
                .append("yaw", position.getYaw())
                .append("pitch", position.getPitch())
                .append("world", position.getWorld());

        new Document("lampBlock_idling", "WHITE_STAINED_GLASS")
                .append("lampBlock_tested", "LIME_STAINED_GLASS")
                .append("lampBlock_busted", "RED_STAINED_GLASS")
                .append("barrierBlock", "WHITE_STAINED_GLASS")
                .append("floorBlock", "IRON_BLOCK")
                .append("cornerA", location)
                .append("cornerB", location)
                .append("leftLamp", location)
                .append("rightLamp", location)
                .append("activationButton", location)
                .append("traitorTrap", location)
                .append("barrier_1", location)
                .append("barrier_2", location)
                .append("barrier_3", location)
                .append("playerSpawn", entitySpawn)
                .append("outsideTester", entitySpawn)
                .saveAsConfig(Paths.get(getDataFolder().getAbsolutePath(), "tester.json"));

        Bukkit.getLogger().info("tester.json wurde erstellt!");
    }

    private void initializeSpawnsConfig_notAvailable() {
        if (Files.exists(Paths.get(getDataFolder().getAbsolutePath(), "spawns.json"))) return;

        Bukkit.getConsoleSender().sendMessage("spawns.json wurde nicht gefunden, weshalb sie nun erstellt wird");

        new Document(UUID.randomUUID().toString(), new PositionEntity(
                0.0,
                0.0,
                0.0,
                0.0F,
                0.0F,
                "world")
        ).saveAsConfig(Paths.get(getDataFolder().getAbsolutePath(), "spawns.json"));

        Bukkit.getConsoleSender().sendMessage("spawns.json wurde erstellt!");
    }

    private void initializeChestsConfig_notAvailable() {
        if (Files.exists(Paths.get(getDataFolder().getAbsolutePath(), "chests.json"))) return;

        Bukkit.getConsoleSender().sendMessage("chests.json wurde nicht gefunden, weshalb sie nun erstellt wird");

        new Document(UUID.randomUUID().toString(), new PositionBlock(
                0,
                0,
                0,
                "world",
                "CHEST")
        ).saveAsConfig(Paths.get(getDataFolder().getAbsolutePath(), "chests.json"));

        Bukkit.getConsoleSender().sendMessage("chests.json wurde erstellt!");
    }

    public void setupTester() {
        Material idling = Material.valueOf(this.testerDocument.getString("lampBlock_idling"));
        Material tested = Material.valueOf(this.testerDocument.getString("lampBlock_tested"));
        Material busted = Material.valueOf(this.testerDocument.getString("lampBlock_busted"));
        Material barrier = Material.valueOf(this.testerDocument.getString("barrierBlock"));
        Material floor = Material.valueOf(this.testerDocument.getString("floorBlock"));

        Document CORNER_A_DOCUMENT = this.testerDocument.getDocument("cornerA");
        Location cornerA = new Location(
                Bukkit.getWorld(CORNER_A_DOCUMENT.getString("world")),
                CORNER_A_DOCUMENT.getDouble("x"),
                CORNER_A_DOCUMENT.getDouble("y"),
                CORNER_A_DOCUMENT.getDouble("z")
        );

        Document CORNER_B_DOCUMENT = this.testerDocument.getDocument("cornerB");
        Location cornerB = new Location(
                Bukkit.getWorld(CORNER_B_DOCUMENT.getString("world")),
                CORNER_B_DOCUMENT.getDouble("x"),
                CORNER_B_DOCUMENT.getDouble("y"),
                CORNER_B_DOCUMENT.getDouble("z")
        );

        Document LEFT_LAMP_DOCUMENT = this.testerDocument.getDocument("leftLamp");
        Location leftLamp = new Location(
                Bukkit.getWorld(LEFT_LAMP_DOCUMENT.getString("world")),
                LEFT_LAMP_DOCUMENT.getDouble("x"),
                LEFT_LAMP_DOCUMENT.getDouble("y"),
                LEFT_LAMP_DOCUMENT.getDouble("z")
        );

        Document RIGHT_LAMP_DOCUMENT = this.testerDocument.getDocument("rightLamp");
        Location rightLamp = new Location(
                Bukkit.getWorld(RIGHT_LAMP_DOCUMENT.getString("world")),
                RIGHT_LAMP_DOCUMENT.getDouble("x"),
                RIGHT_LAMP_DOCUMENT.getDouble("y"),
                RIGHT_LAMP_DOCUMENT.getDouble("z")
        );

        Location[] lamps = new Location[2];
        lamps[0] = leftLamp;
        lamps[1] = rightLamp;

        Document BARRIER_1_DOCUMENT = this.testerDocument.getDocument("barrier_1");
        Location barrier1 = new Location(
                Bukkit.getWorld(BARRIER_1_DOCUMENT.getString("world")),
                BARRIER_1_DOCUMENT.getDouble("x"),
                BARRIER_1_DOCUMENT.getDouble("y"),
                BARRIER_1_DOCUMENT.getDouble("z")
        );

        Document BARRIER_2_DOCUMENT = this.testerDocument.getDocument("barrier_2");
        Location barrier2 = new Location(
                Bukkit.getWorld(BARRIER_2_DOCUMENT.getString("world")),
                BARRIER_2_DOCUMENT.getDouble("x"),
                BARRIER_2_DOCUMENT.getDouble("y"),
                BARRIER_2_DOCUMENT.getDouble("z")
        );

        Document BARRIER_3_DOCUMENT = this.testerDocument.getDocument("barrier_3");
        Location barrier3 = new Location(
                Bukkit.getWorld(BARRIER_3_DOCUMENT.getString("world")),
                BARRIER_3_DOCUMENT.getDouble("x"),
                BARRIER_3_DOCUMENT.getDouble("y"),
                BARRIER_3_DOCUMENT.getDouble("z")
        );

        Location[] barriers = new Location[3];
        barriers[0] = barrier1;
        barriers[1] = barrier2;
        barriers[2] = barrier3;

        Document ACTIVATION_BUTTON_DOCUMENT = this.testerDocument.getDocument("activationButton");
        Location activationButton = new Location(
                Bukkit.getWorld(ACTIVATION_BUTTON_DOCUMENT.getString("world")),
                ACTIVATION_BUTTON_DOCUMENT.getDouble("x"),
                ACTIVATION_BUTTON_DOCUMENT.getDouble("y"),
                ACTIVATION_BUTTON_DOCUMENT.getDouble("z")
        );

        Document TRAITOR_TRAP_DOCUMENT = this.testerDocument.getDocument("traitorTrap");
        Location traitorTrap = new Location(
                Bukkit.getWorld(TRAITOR_TRAP_DOCUMENT.getString("world")),
                TRAITOR_TRAP_DOCUMENT.getDouble("x"),
                TRAITOR_TRAP_DOCUMENT.getDouble("y"),
                TRAITOR_TRAP_DOCUMENT.getDouble("z")
        );

        Document PLAYER_SPAWN_DOCUMENT = this.testerDocument.getDocument("playerSpawn");
        Location playerSpawn = new Location(
                Bukkit.getWorld(PLAYER_SPAWN_DOCUMENT.getString("world")),
                PLAYER_SPAWN_DOCUMENT.getDouble("x"),
                PLAYER_SPAWN_DOCUMENT.getDouble("y"),
                PLAYER_SPAWN_DOCUMENT.getDouble("z"),
                PLAYER_SPAWN_DOCUMENT.getFloat("yaw"),
                PLAYER_SPAWN_DOCUMENT.getFloat("pitch")
        );

        Document OUTSIDE_TESTER_DOCUMENT = this.testerDocument.getDocument("outsideTester");
        Location outsideTester = new Location(
                Bukkit.getWorld(OUTSIDE_TESTER_DOCUMENT.getString("world")),
                OUTSIDE_TESTER_DOCUMENT.getDouble("x"),
                OUTSIDE_TESTER_DOCUMENT.getDouble("y"),
                OUTSIDE_TESTER_DOCUMENT.getDouble("z"),
                OUTSIDE_TESTER_DOCUMENT.getFloat("yaw"),
                OUTSIDE_TESTER_DOCUMENT.getFloat("pitch")
        );

        this.testerSetup = new TesterSetup(
                lamps,
                barriers,
                cornerA,
                cornerB,
                playerSpawn,
                outsideTester,
                activationButton,
                traitorTrap,
                idling,
                tested,
                busted,
                barrier,
                floor
        );
    }

    public void reloadConfigs() {
        this.positionManager.spawnPositions.clear();
        this.positionManager.chestPositions.clear();
        this.positionManager.CHEST_UUID_MAP.clear();
        this.positionManager.ARMORSTANDS_EDITABLE.forEach(((uuid, armorStand) -> {
            armorStand.remove();
        }));
        this.positionManager.ARMORSTANDS_EDITABLE.clear();

        reloadGameConfig();
        setupTester();
    }

    public void reloadGameConfig() {
        this.configDocument = Document.loadDocument(Paths.get(getDataFolder().getAbsolutePath(), "config.json"));
        this.testerDocument = Document.loadDocument(Paths.get(getDataFolder().getAbsolutePath(), "tester.json"));
        this.spawnsDocument = Document.loadDocument(Paths.get(getDataFolder().getAbsolutePath(), "spawns.json"));
        this.chestsDocument = Document.loadDocument(Paths.get(getDataFolder().getAbsolutePath(), "chests.json"));

        this.maxPercent_innocents = this.configDocument.getDouble("maxPercent_INNOCENT");
        this.maxPercent_detectives = this.configDocument.getDouble("maxPercent_DETECTIVE");
        this.maxPercent_traitors = this.configDocument.getDouble("maxPercent_TRAITOR");
        this.lobby_sleepDelay = this.configDocument.getInt("lobby_sleepDelay");
        this.lobbyCountdown = this.configDocument.getInt("lobbyCountdown");
        this.protectionCountdown = this.configDocument.getInt("protectionCountdown");
        this.ingameCountdown = this.configDocument.getInt("ingameCountdown");
        this.minPlayers = this.configDocument.getInt("minPlayers");

        this.roleManager = new RoleManager();
        this.roleItemManager = new RoleItemManager();
        //this.roleManager.roleList.add(new Role(0, "Innocent", "§a", "§7Unterstütze die Detectives dabei die Traitor zu enttarnen", maxPercent_innocents, new ArrayList<Player>()));
        //this.roleManager.roleList.add(new Role(1, "Detective", "§9", "§7Identifiziere Leichen und enttarne die Traitor", maxPercent_detectives, new ArrayList<Player>()));
        //this.roleManager.roleList.add(new Role(2, "Traitor", "§4", "§fTöte unauffällig alle Innocents und Detectives", maxPercent_traitors, new ArrayList<Player>()));

        this.roleManager.roleList.add(new Role(0, "Elf", "§a", "§7Unterstütze die Frosts dabei die Krampusse zu enttarnen", maxPercent_innocents, new ArrayList<Player>()));
        this.roleManager.roleList.add(new Role(1, "Mr. Frost", "§9", "§7Identifiziere Leichen und enttarne die Krampusse", maxPercent_detectives, new ArrayList<Player>()));
        this.roleManager.roleList.add(new Role(2, "Krampus", "§4", "§fTöte unaufällig alle Elfen und Frosts", maxPercent_traitors, new ArrayList<Player>()));

        Bukkit.getLogger().info("Es wurden folgende Teams erstellt");
        for (Role role : this.roleManager.roleList) Bukkit.getLogger().info(role.toString());

        this.setupTester();
        Arrays.stream(this.testerSetup.testerLamps).forEach(location -> location.getBlock().setType(this.testerSetup.idling));
        Arrays.stream(this.testerSetup.barrier).forEach(location -> location.getBlock().setType(Material.AIR));

        this.positionManager = new PositionManager();
        this.positionManager.readSpawns();
        this.positionManager.readChests();
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

    private Player getPlayer(int entityId) {
        for (Player player : Bukkit.getOnlinePlayers()) if (player.getEntityId() == entityId) return player;
        return null;
    }

    public boolean hasMetadata(Entity entity, String name) {
        return entity.hasMetadata(name);
    }

    public static TTT_Unused getInstance() {
        return instance;
    }
}