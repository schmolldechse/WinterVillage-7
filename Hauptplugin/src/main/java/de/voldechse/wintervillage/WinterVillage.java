package de.voldechse.wintervillage;

import com.google.common.reflect.ClassPath;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import de.voldechse.wintervillage.commands.*;
import de.voldechse.wintervillage.database.*;
import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.library.gradient.Gradient;
import de.voldechse.wintervillage.util.*;
import dev.derklaro.aerogel.Inject;
import dev.derklaro.aerogel.Singleton;
import dev.derklaro.aerogel.binding.BindingConstructor;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionManagement;
import eu.cloudnetservice.ext.platforminject.api.PlatformEntrypoint;
import eu.cloudnetservice.ext.platforminject.api.stereotype.Command;
import eu.cloudnetservice.ext.platforminject.api.stereotype.PlatformPlugin;
import me.joel.wv6.clansystem.CS;
import org.bson.UuidRepresentation;
import org.bukkit.*;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.popcraft.chunky.Chunky;
import org.popcraft.chunky.ChunkyProvider;
import org.popcraft.chunky.api.ChunkyAPI;
import org.popcraft.chunky.api.ChunkyAPIImpl;
import org.popcraft.chunky.event.EventBus;
import org.popcraft.chunky.platform.BukkitConfig;
import org.popcraft.chunky.platform.BukkitServer;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
@PlatformPlugin(
        platform = "bukkit",
        pluginFileNames = "plugin.yml",
        name = "WinterVillage",
        version = "1.1-RELEASE",
        authors = "Voldechse",
        commands = {
                @Command(name = "farmwelt"),
                @Command(name = "world",
                    permission = "wintervillage.server.command.world"),
                @Command(name = "spawnegg",
                    permission = "wintervillage.server.command.spawnegg"),
                @Command(name = "spawn"),
                @Command(name = "balance"),
                @Command(name = "transactions"),
                @Command(name = "transfer"),
                @Command(name = "shop",
                    permission = "wintervillage.server.command.shop"),
                @Command(name = "sethome"),
                @Command(name = "home"),
                @Command(name = "inventory",
                    permission = "wintervillage.server.command.inventory"),
                @Command(name = "mute",
                    permission = "wintervillage.server.command.mute"),
                @Command(name = "unmute",
                    permission = "wintervillage.server.command.unmute"),
                @Command(name = "tpa"),
                @Command(name = "tpaccept"),
                @Command(name = "admin",
                    permission = "wintervillage.server.command.admin"),
                @Command(name = "freeze",
                    permission = "wintervillage.server.command.freeze"),
                @Command(name = "sorry"),
                @Command(name = "fly"),
                @Command(name = "serverproblem"),
                @Command(name = "schmerzen"),
                @Command(name = "deathcounter",
                    permission = "wintervillage.server.command.deathcounter")
        }
)
public class WinterVillage implements PlatformEntrypoint {

    private final JavaPlugin instance;
    public String serverPrefix;

    public WorldManager worldManager;
    public CS clanSystem;
    public Backup backup;

    public SpawnEggDatabase spawnEggDatabase;
    public DeathsDatabase deathsDatabase;
    public BalanceDatabase balanceDatabase;
    public TransactionsDatabase transactionsDatabase;
    public ShopDatabase shopDatabase;
    public CompensationDatabase compensationDatabase;
    public WhitelistDatabase whitelistDatabase;
    public SecondCompensationDatabase secondCompensationDatabase;

    public HomeDatabase homeDatabase;

    public MojangAPIFetcher mojangAPIFetcher;
    public ScoreboardManager scoreboardManager;

    public PermissionManagement permissionManagement;

    public MongoClient mongoClient;
    public MongoDatabase database;

    public int potencyAdmin,
            potencyDeveloper,
            potencySupporter,
            potencyContentCreator,
            potencyTeilnehmer,
            potencySpectator;

    public final DecimalFormat DECIMAL_FORMAT;
    public boolean CHAT_MUTED, PLAYERS_FREEZED, RESET_EXECUTED;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public Document deathcounterDocument;

    @Inject
    public WinterVillage(JavaPlugin plugin) {
        this.instance = plugin;

        this.instance.getLogger().info("-----------------------------------");
        this.instance.getLogger().info("WinterVillage wird initialisiert");

        this.serverPrefix = " " + Gradient.color("WinterVillage", Color.WHITE, Color.CYAN) + " §8| §r";

        this.mojangAPIFetcher = new MojangAPIFetcher();
        this.backup = new Backup(this);

        this.permissionManagement = InjectionLayer.ext().instance(PermissionManagement.class);

        File backupsFolder = new File(this.instance.getDataFolder().getAbsolutePath());
        backupsFolder.mkdirs();
        this.initializeGameConfig_notAvailable();

        this.potencyAdmin = 100;
        this.potencyDeveloper = 80;
        this.potencySupporter = 60;
        this.potencyContentCreator = 40;
        this.potencyTeilnehmer = 20;
        this.potencySpectator = 0;

        this.DECIMAL_FORMAT = new DecimalFormat("#,###.##", new DecimalFormatSymbols(Locale.GERMANY));
        this.DECIMAL_FORMAT.setGroupingUsed(true);

        this.CHAT_MUTED = false;
        this.PLAYERS_FREEZED = false;
        this.RESET_EXECUTED = false;

        this.instance.getLogger().info("WinterVillage wurde initialisiert!");
        this.instance.getLogger().info("-----------------------------------");
    }

    @Override
    public void onLoad() {
        this.register();

        this.instance.getLogger().info("Datenbank wird verbunden");
        this.connect();

        File file = new File(Bukkit.getWorldContainer(), "world_farmwelt");
        if (file != null && Bukkit.getWorld(file.getName()) == null)
            new WorldCreator(file.getName())
                    .environment(World.Environment.NORMAL)
                    .generateStructures(true)
                    .type(WorldType.NORMAL)
                    .seed(System.currentTimeMillis())
                    .createWorld();

        this.worldManager = new WorldManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.clanSystem = CS.instance;

        this.deathcounterDocument = Document.loadDocument(Paths.get(this.instance.getDataFolder().getAbsolutePath(), "deathcounter.json"));

        this.resetFarmworlds();
        new BukkitRunnable() {
            @Override
            public void run() {
                createBackup();
            }
        }.runTaskTimer(this.getInstance(), 1, 10 * 60 * 20L);

        getInstance().getServer().getServicesManager().getRegistration(Chunky.class).getProvider().getApi().onGenerationProgress(generationProgressEvent -> {
            if (this.worldManager.bossBarMap.containsKey(generationProgressEvent.world())) {
                BossBar bossBar = this.worldManager.bossBarMap.get(generationProgressEvent.world());
                bossBar.setProgress((float) generationProgressEvent.progress() / 100);

                float value = BigDecimal.valueOf(generationProgressEvent.progress())
                        .setScale(2, RoundingMode.HALF_UP)
                        .floatValue();

                String finished = String.format("%02d:%02d:%02d", generationProgressEvent.hours(), generationProgressEvent.minutes(), generationProgressEvent.seconds());
                bossBar.setTitle("§a" + generationProgressEvent.world() + " §fvorladen §8- §b" + value + " % §8| §ffertig in §c" + finished);

                if (generationProgressEvent.complete()) {
                    bossBar.setProgress(1);
                    bossBar.setVisible(false);
                    this.worldManager.bossBarMap.remove(generationProgressEvent.world());
                }
            }
        });
    }

    @Override
    public void onDisable() {
        if (this.mongoClient != null) this.mongoClient.close();

        this.scheduler.shutdown();
        Bukkit.getScheduler().cancelTasks(this.getInstance());
    }

    private void connect() {
        String connection = "mongodb://WinterVillage:MugAppg2xuhbQDqgZNaJY88eJsrv8QVAKgFijCPksQ4xGhCCkfbPWGervNfRUeW4@0.0.0.0:27017/?directConnection=true&serverSelectionTimeoutMS=2000&appName=mongosh+2.1.0";

        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connection))
                .applyToConnectionPoolSettings(builder ->
                        builder.maxSize(5)
                                .maxWaitTime(5000, TimeUnit.MILLISECONDS))
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .serverApi(serverApi)
                .build();

        this.mongoClient = MongoClients.create(settings);
        this.database = mongoClient.getDatabase("wintervillage");

        this.spawnEggDatabase = new SpawnEggDatabase(this);
        this.deathsDatabase = new DeathsDatabase(this);
        this.balanceDatabase = new BalanceDatabase(this);
        this.transactionsDatabase = new TransactionsDatabase(this);
        this.shopDatabase = new ShopDatabase(this);
        this.homeDatabase = new HomeDatabase(this);
        this.compensationDatabase = new CompensationDatabase(this);
        this.secondCompensationDatabase = new SecondCompensationDatabase(this);
        this.whitelistDatabase = new WhitelistDatabase(this);
    }

    private void resetFarmworlds() {
        this.scheduler.scheduleAtFixedRate(() -> {
            LocalDateTime now = LocalDateTime.now();
            //if (now.getHour() == 18) {
            //if (now.toLocalTime().equals(LocalTime.MIDNIGHT)) {
            if (now.getHour() == 2) {
                DayOfWeek dayOfWeek = now.getDayOfWeek();
                /**
                if (dayOfWeek == DayOfWeek.MONDAY
                        || dayOfWeek == DayOfWeek.WEDNESDAY
                        || dayOfWeek == DayOfWeek.FRIDAY
                        || dayOfWeek == DayOfWeek.SATURDAY
                        || dayOfWeek == DayOfWeek.SUNDAY) {
                 */
                if (dayOfWeek == DayOfWeek.MONDAY) {
                    if (!this.RESET_EXECUTED) {
                        this.RESET_EXECUTED = true;

                        LocalDateTime nextExecution = now.plusDays(1);
                        while (!isDesiredDay(nextExecution.getDayOfWeek())) {
                            nextExecution = nextExecution.plusDays(1);
                        }

                        this.getInstance().getLogger().info("Next planed reset [date=" + nextExecution + "]");

                        Bukkit.broadcastMessage(this.serverPrefix + "§eDie Farmwelt und der Nether werden in 30 Sekunden zurückgesetzt");
                        Bukkit.broadcastMessage(this.serverPrefix + "§eAlle sich darin befindenden Spieler werden davor rausteleportiert");

                        getInstance().getServer().getScheduler().runTaskLater(this.getInstance(), () -> {
                            this.worldManager.delete("world_farmwelt");
                            this.worldManager.delete("world_nether");
                            this.worldManager.create("world_farmwelt", WorldManager.Type.OVERWORLD);
                            this.worldManager.preparePreGeneration("world_farmwelt");
                            this.worldManager.create("world_nether", WorldManager.Type.NETHER);

                            Bukkit.broadcastMessage(this.serverPrefix + "§eDie Farmwelt und der Nether wurden zurückgesetzt");
                            this.getInstance().getLogger().info("Sucessfully reset world_farmwelt & world_nether");
                        }, 30 * 20L);
                    }
                }
            } else this.RESET_EXECUTED = false;
        }, 1, 1, TimeUnit.MINUTES);
    }

    /**
    private boolean isDesiredDay(DayOfWeek dayOfWeek) {
        return dayOfWeek == DayOfWeek.MONDAY
                || dayOfWeek == DayOfWeek.WEDNESDAY
                || dayOfWeek == DayOfWeek.FRIDAY
                || dayOfWeek == DayOfWeek.SATURDAY
                || dayOfWeek == DayOfWeek.SUNDAY;
    }
     */
    private boolean isDesiredDay(DayOfWeek dayOfWeek) {
        return dayOfWeek == DayOfWeek.MONDAY;
    }

    private void initializeGameConfig_notAvailable() {
        if (Files.exists(Paths.get(this.instance.getDataFolder().getAbsolutePath(), "config.json"))) return;

        this.instance.getLogger().severe("config.json wurde nicht gefunden, weshalb sie nun erstellt wird");

        new Document("test").saveAsConfig(Paths.get(this.instance.getDataFolder().getAbsolutePath(), "config.json"));

        this.instance.getLogger().info("config.json wurde erstellt!");
    }

    private void createBackup() {
        this.getInstance().getLogger().info("Creating backup [date=" + LocalDateTime.now() + "]");

        if (Bukkit.getWorld("world") == null) {
            this.instance.getLogger().severe("Could not complete creating backup - world does not exist");
            return;
        }
        Bukkit.getWorld("world").save();
        Bukkit.getOnlinePlayers().forEach(Player::saveData);

        this.instance.getLogger().info("Successfully saved world");

        File serverFile = new File(getInstance().getDataFolder() + "/../../world/");

        new BukkitRunnable() {
            @Override
            public void run() {
                backup.folderToZip(serverFile, Date.from(Instant.now()).toString().replace(":", " ") + ".zip");
            }
        }.runTaskAsynchronously(this.getInstance());

        Bukkit.broadcastMessage(serverPrefix + "§eWelt wurde gespeichert");
    }

    private void register() {
        new CommandAdmin(this);
        new CommandBalance(this);
        new CommandFarmwelt(this);
        new CommandFreeze(this);
        new CommandHome(this);
        new CommandInventory(this);
        new CommandMute(this);
        new CommandSetHome(this);
        new CommandShop(this);
        new CommandSpawn(this);
        new CommandSpawnegg(this);
        new CommandTPA(this);
        new CommandTpaccept(this);
        new CommandTransactions(this);
        new CommandTransfer(this);
        new CommandWorld(this);
        new CommandUnmute(this);
        new CommandSorry(this);
        new CommandFly(this);
        new CommandServerproblem(this);
        new CommandSchmerzen(this);
        new CommandDeathcounter(this);

        PluginManager pluginManager = Bukkit.getPluginManager();
        try {
            for (ClassPath.ClassInfo classInfo : ClassPath.from(getClass().getClassLoader()).getTopLevelClasses("de.voldechse.wintervillage.listener")) {
                Class clazz = Class.forName(classInfo.getName());
                if (Listener.class.isAssignableFrom(clazz))
                    pluginManager.registerEvents((Listener) clazz.newInstance(), getInstance());
            }
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException exception) {
            this.instance.getLogger().severe("An error occurred while registering an event : " + exception);
        }
    }

    public String formatBalance(BigDecimal balance) {
        if (balance.compareTo(BigDecimal.valueOf(1_000_000)) >= 0) {
            return DECIMAL_FORMAT.format(balance.divide(BigDecimal.valueOf(1_000_000))) + " M";
        } else {
            return DECIMAL_FORMAT.format(balance);
        }
    }

    public void removeMetadata(Entity entity, String name) {
        if (entity.hasMetadata(name)) entity.removeMetadata(name, this.getInstance());
    }

    public void setMetadata(Entity entity, String name, Object object) {
        this.removeMetadata(entity, name);
        entity.setMetadata(name, new FixedMetadataValue(this.getInstance(), object));
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