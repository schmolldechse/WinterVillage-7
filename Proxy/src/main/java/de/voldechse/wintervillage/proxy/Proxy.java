package de.voldechse.wintervillage.proxy;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import de.voldechse.wintervillage.proxy.commands.CommandPunish;
import de.voldechse.wintervillage.proxy.commands.CommandRank;
import de.voldechse.wintervillage.proxy.commands.CommandWhitelist;
import de.voldechse.wintervillage.proxy.commands.CommandWildcard;
import de.voldechse.wintervillage.proxy.listener.PreLoginListener;
import de.voldechse.wintervillage.proxy.util.MojangAPIFetcher;
import de.voldechse.wintervillage.proxy.wildcard.BanDatabase;
import de.voldechse.wintervillage.proxy.wildcard.WhitelistDatabase;
import de.voldechse.wintervillage.proxy.wildcard.WildcardDatabase;
import dev.derklaro.aerogel.Inject;
import dev.derklaro.aerogel.Singleton;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionManagement;
import eu.cloudnetservice.driver.permission.PermissionUser;
import eu.cloudnetservice.driver.registry.ServiceRegistry;
import eu.cloudnetservice.ext.platforminject.api.PlatformEntrypoint;
import eu.cloudnetservice.ext.platforminject.api.stereotype.Command;
import eu.cloudnetservice.ext.platforminject.api.stereotype.PlatformPlugin;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import org.bson.UuidRepresentation;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Singleton
@PlatformPlugin(
        platform = "bungeecord",
        pluginFileNames = "bungee.yml",
        name = "WinterVillage Proxy",
        version = "1.1-RELEASE",
        authors = "Voldechse",
        commands = {
                @Command(name = "rank",
                    permission = "wintervillage.proxy.command.rank",
                    aliases = "rang"),
                @Command(name = "whitelist",
                    permission = "wintervillage.proxy.command.whitelist"),
                @Command(name = "wildcard"),
                @Command(name = "punish",
                permission = "wintervillage.proxy.command.punish")
        }
)
public class Proxy implements PlatformEntrypoint {

    private final Plugin instance;
    public String proxyPrefix;

    public MojangAPIFetcher mojangAPIFetcher;
    public WildcardDatabase wildcardDatabase;
    public WhitelistDatabase whitelistDatabase;

    public PermissionManagement permissionManagement;
    public PlayerManager playerManager;

    public MongoClient mongoClient;
    public MongoDatabase database;
    public BanDatabase banDatabase;

    public int potencyAdmin,
            potencyDeveloper,
            potencySupporter,
            potencyContentCreator,
            potencyTeilnehmer,
            potencySpectator;

    public boolean WHITELIST_ENABLED, WHITELIST_PRIORITY;

    @Inject
    public Proxy(Plugin instance) {
        this.instance = instance;

        this.instance.getLogger().info("-----------------------------------");
        this.instance.getLogger().info("Proxy wird initialisiert");

        this.proxyPrefix = " §cProxy §8| §r";

        this.mojangAPIFetcher = new MojangAPIFetcher();

        this.permissionManagement = InjectionLayer.ext().instance(PermissionManagement.class);
        this.playerManager = InjectionLayer.ext().instance(ServiceRegistry.class).firstProvider(PlayerManager.class);

        this.potencyAdmin = 100;
        this.potencyDeveloper = 80;
        this.potencySupporter = 60;
        this.potencyContentCreator = 40;
        this.potencyTeilnehmer = 20;
        this.potencySpectator = 0;

        this.WHITELIST_ENABLED = false;
        this.WHITELIST_PRIORITY = false;

        this.instance.getLogger().info("Proxy wurde initialisiert");
        this.instance.getLogger().info("-----------------------------------");
    }

    @Override
    public void onLoad() {
        this.instance.getLogger().info("Starting proxy");
        this.register();

        this.instance.getLogger().info("Datenbank wird verbunden");
        this.connect();

        getInstance().getProxy().getScheduler().schedule(this.instance, () -> {
            LocalDateTime current = LocalDateTime.now();

            if (current.getDayOfWeek() == DayOfWeek.MONDAY && current.isAfter(LocalDateTime.of(2023, 11, 24, 0, 0))) {
                for (UUID uuid : this.wildcardDatabase.list()) {
                    PermissionUser permissionUser = this.permissionManagement.userAsync(uuid).join();
                    if (permissionUser == null) continue;
                    PermissionGroup permissionGroup = this.permissionManagement.highestPermissionGroup(permissionUser);
                    if (permissionGroup == null) continue;
                    if (permissionGroup.potency() < this.potencyContentCreator) continue;

                    if (current.getDayOfMonth() != this.wildcardDatabase.data(uuid).lastReset.getDayOfMonth()) {
                        this.wildcardDatabase.reset(uuid);
                        this.wildcardDatabase.reset(uuid, current);

                        if (getInstance().getProxy().getPlayer(uuid) != null)
                            getInstance().getProxy().getPlayer(uuid).sendMessage(new ComponentBuilder(this.proxyPrefix + "§aDeine wöchentlichen Wildcards wurden zurückgesetzt").create());
                    }
                }
            }
        }, 0, 30, TimeUnit.MINUTES);
    }

    @Override
    public void onDisable() {
        this.instance.getLogger().info("Disabling proxy");

        if (this.mongoClient != null) this.mongoClient.close();
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

        this.wildcardDatabase = new WildcardDatabase(this);
        this.whitelistDatabase = new WhitelistDatabase(this);
        this.banDatabase = new BanDatabase(this);
    }

    private void register() {
        PluginManager pluginManager = this.instance.getProxy().getPluginManager();

        pluginManager.registerCommand(this.instance, new CommandRank(this));
        pluginManager.registerCommand(this.instance, new CommandWildcard(this));
        pluginManager.registerCommand(this.instance, new CommandWhitelist(this));
        pluginManager.registerCommand(this.instance, new CommandPunish(this));

        pluginManager.registerListener(this.instance, new PreLoginListener());
    }

    public Plugin getInstance() {
        return instance;
    }
}
