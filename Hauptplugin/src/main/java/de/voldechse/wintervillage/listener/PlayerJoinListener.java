package de.voldechse.wintervillage.listener;

import de.voldechse.wintervillage.WinterVillage;
import de.voldechse.wintervillage.database.TransactionsDatabase;
import de.voldechse.wintervillage.database.WhitelistDatabase;
import de.voldechse.wintervillage.library.head.HeadManager;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class PlayerJoinListener implements Listener {

    private final WinterVillage plugin = InjectionLayer.ext().instance(WinterVillage.class);

    private LocalDateTime check = LocalDateTime.of(2023, 11, 26, 0, 0);
    private LocalDateTime check_2 = LocalDateTime.of(2023, 11, 29, 0, 0);

    @EventHandler
    public void execute(PlayerJoinEvent event) throws ExecutionException {
        Player player = event.getPlayer();
        if (player.getWorld().getName().equalsIgnoreCase("world_farmwelt"))
            player.teleport(Bukkit.getWorld("world").getSpawnLocation().add(0, 1, 0));
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        if (player.hasMetadata("FLYING")) {
            player.setAllowFlight(false);
            player.setFlying(false);
            this.plugin.removeMetadata(player, "FLYING");
        }

        if (player.hasMetadata("SHOP_CONFIGURATION")) plugin.removeMetadata(player, "SHOP_CONFIGURATION");

        player.setResourcePack(
                "https://download.mc-packs.net/pack/dd87d08a108657e37bbe0f5c148812469ba25918.zip#dd87d08a108657e37bbe0f5c148812469ba25918",
                null,
                "§cDamit du fehlerfrei alle Grafiken sehen kannst, musst du das Resource Pack annehmen",
                true
        );

        String value = "";
        PermissionUser permissionUser = this.plugin.permissionManagement.getOrCreateUserAsync(player.getUniqueId(), player.getName()).join();
        if (permissionUser == null) value = "§cDATENBANK FEHLER";

        PermissionGroup permissionGroup = plugin.permissionManagement.highestPermissionGroup(permissionUser);
        if (permissionGroup == null) value = "§cDATENBANK FEHLER";
        value = permissionGroup.color() + permissionGroup.name();

        if (!plugin.deathsDatabase.isSaved(player.getUniqueId()))
            plugin.deathsDatabase.save(player.getUniqueId());

        if (!plugin.balanceDatabase.saved(player.getUniqueId())) {
            plugin.balanceDatabase.save(player.getUniqueId());
            plugin.balanceDatabase.modify(player.getUniqueId(), BigDecimal.valueOf(200));
        }

        if (this.plugin.whitelistDatabase.whitelisted(player.getUniqueId())) {
            WhitelistDatabase.WhitelistData whitelistData = this.plugin.whitelistDatabase.data(player.getUniqueId());
            if (!this.plugin.compensationDatabase.saved(player.getUniqueId()) && whitelistData.date.isBefore(check))
                this.plugin.compensationDatabase.save(player.getUniqueId());

            if (!this.plugin.secondCompensationDatabase.saved(player.getUniqueId()) && whitelistData.date.isBefore(check_2))
                this.plugin.secondCompensationDatabase.save(player.getUniqueId());

            if (whitelistData.date.isBefore(check) && !plugin.compensationDatabase.collected(player.getUniqueId()))
                player.sendMessage(plugin.serverPrefix + "§fEs tut uns leid, für die am Start sich befindenden Serverprobleme. Wir haben dir eine Wiedergutmachung gegeben, welche du dir mit §c/sorry §fabholen kannst");

            if (plugin.whitelistDatabase.whitelisted(player.getUniqueId()) && whitelistData.date.isBefore(check_2) && !plugin.secondCompensationDatabase.collected(player.getUniqueId()))
                player.sendMessage(plugin.serverPrefix + "§fFür erneute Serverprobleme, haben wir dir mit §c/serverproblem §fein neues Wiedergutmachungs Packet bereitgestellt");
        }

        if (plugin.balanceDatabase.canReceive(player.getUniqueId()) && permissionGroup.potency() > plugin.potencySpectator) {
            plugin.balanceDatabase.modify(player.getUniqueId(), BigDecimal.valueOf(200));
            plugin.balanceDatabase.modify(player.getUniqueId(), System.currentTimeMillis());

            plugin.transactionsDatabase.save(new TransactionsDatabase.TransactionData(
                    new UUID(0, 0),
                    player.getUniqueId(),
                    BigDecimal.valueOf(200),
                    LocalDateTime.now()
            ));

            player.sendMessage(plugin.serverPrefix + "§eSanta hat dir ein Vorweihnachtsgeschenk in Höhe von 200 $ dagelassen");
            player.sendMessage(plugin.serverPrefix + "§eSanta schaut zurück, in §c" + nextReward());
        }

        if (!this.plugin.worldManager.bossBarMap.isEmpty()) {
            this.plugin.worldManager.bossBarMap.forEach((name, bossBar) -> {
                if (permissionGroup.potency() > plugin.potencySupporter && !bossBar.getPlayers().contains(player)) bossBar.addPlayer(player);
            });
        }

        if (!player.hasMetadata("VANISH"))
            event.setJoinMessage("§a» " + permissionGroup.color() + permissionUser.name() + " §fhat Winter Village betreten");
        else event.setJoinMessage(null);

        plugin.scoreboardManager.generateScoreboard(player);

        plugin.scoreboardManager.updateScoreboard(player, "currentBalance", " §e" + plugin.formatBalance(plugin.balanceDatabase.balance(player.getUniqueId())) + " $", "");
        plugin.scoreboardManager.updateScoreboard(player, "currentRank", " " + value, "");
        plugin.scoreboardManager.updateScoreboard("currentOnline", " §a" + Bukkit.getOnlinePlayers().size(), "");
        plugin.scoreboardManager.playerList();

        //plugin.setMetadata(player, "PLAYERS_HEAD", BaseComponent.toLegacyText(plugin.headManager.getPlayerHead(player.getName())));

        if (permissionGroup.potency() == plugin.potencySpectator) player.setGameMode(GameMode.SPECTATOR);

        updateVisibility(player, permissionGroup);
    }

    private String nextReward() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (System.currentTimeMillis() > calendar.getTimeInMillis())
            calendar.add(Calendar.DAY_OF_YEAR, 1);

        long next = calendar.getTimeInMillis();
        long secondsUntilNext = (next - System.currentTimeMillis()) / 1000;

        int hours = (int) (secondsUntilNext / 3600);
        int minutes = (int) ((secondsUntilNext % 3600) / 60);
        int seconds = (int) (secondsUntilNext % 60);

        String format = (hours > 0 ? "%02d:" : "") + (minutes > 0 ? "%02d:" : 0) + "%02d";
        return String.format(format, hours, minutes, seconds);
    }

    public void updateVisibility(Player player, PermissionGroup highestPermissionGroup) {
        Bukkit.getOnlinePlayers().forEach(online -> {
            if (online.hasMetadata("VANISH") && highestPermissionGroup.potency() < this.plugin.potencySpectator) {
                player.hidePlayer(this.plugin.getInstance(), online);
                return;
            }

            online.showPlayer(this.plugin.getInstance(), player);
            player.showPlayer(this.plugin.getInstance(), online);
        });
    }
}