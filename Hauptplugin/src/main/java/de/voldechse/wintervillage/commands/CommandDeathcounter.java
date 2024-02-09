package de.voldechse.wintervillage.commands;

import com.google.gson.JsonElement;
import de.voldechse.wintervillage.WinterVillage;
import de.voldechse.wintervillage.database.DeathsDatabase;
import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.util.Position;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

public class CommandDeathcounter implements CommandExecutor {

    private final WinterVillage plugin;

    public CommandDeathcounter(WinterVillage plugin) {
        this.plugin = plugin;
        this.plugin.getInstance().getCommand("deathcounter").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cPlease execute this command as a player");
            return true;
        }

        List<DeathsDatabase.DeathsData> deaths = this.plugin.deathsDatabase.top(10);
        //Collections.sort(deaths);

        List<UUID> staticDisplays = new ArrayList<>();
        List<UUID> dynamicDisplays = new ArrayList<>();

        Location location = player.getLocation().clone().add(0, 4,0);

        TextDisplay header = location.getWorld().spawn(location, TextDisplay.class);
        header.setText("§b§lWinterVillage §fTop §e10 §fTode");
        header.setBillboard(Display.Billboard.CENTER);
        header.setSeeThrough(true);

        staticDisplays.add(header.getUniqueId());

        TextDisplay space_1 = location.getWorld().spawn(location.subtract(0, .26, 0), TextDisplay.class);
        space_1.setText("§8--------------------------------");
        space_1.setBillboard(Display.Billboard.CENTER);
        space_1.setSeeThrough(true);

        staticDisplays.add(space_1.getUniqueId());

        for (int i = 1; i <= 10; i++) {
            DeathsDatabase.DeathsData deathsData = deaths.get(i - 1);

            if (!this.plugin.permissionManagement.containsUserAsync(deathsData.uuid).join()) {
                player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching §b" + deathsData.uuid + "'s §centry in database");
                return true;
            }

            PermissionUser permissionUser = this.plugin.permissionManagement.userAsync(deathsData.uuid).join();
            if (permissionUser == null) {
                player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching §b" + deathsData.uuid + "'s §centry in database");
                return true;
            }
            PermissionGroup permissionGroup = this.plugin.permissionManagement.highestPermissionGroup(permissionUser);
            if (permissionGroup == null) {
                player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching §b" + permissionUser.name() + "'s §centry in database");
                return true;
            }

            TextDisplay dynamic = location.getWorld().spawn(location.subtract(0, .26, 0), TextDisplay.class);
            dynamic.setText("§d" + i + ". §fPlatz §8§l: " + permissionGroup.color() + permissionUser.name() + " §8-§l §e" + deathsData.deaths + " §fTode");
            dynamic.setBillboard(Display.Billboard.CENTER);
            dynamic.setSeeThrough(true);

            dynamicDisplays.add(dynamic.getUniqueId());
        }

        TextDisplay space_2 = location.getWorld().spawn(location.subtract(0, .26, 0), TextDisplay.class);
        space_2.setText("§8--------------------------------");
        space_2.setBillboard(Display.Billboard.CENTER);
        space_2.setSeeThrough(true);

        staticDisplays.add(space_2.getUniqueId());

        update(player.getLocation(), staticDisplays, dynamicDisplays);
        return false;
    }

    private void update(Location location, List<?> staticDisplays, List<?> dynamicDisplays) {
        File file = new File(this.plugin.getInstance().getDataFolder().getAbsolutePath(), "deathcounter.json");
        file.delete();

        Position position = new Position(location.getX(), location.getY(), location.getZ(), location.getWorld().getName());

        World old = null;
        if (this.plugin.deathcounterDocument.contains("location")) {
            if (Bukkit.getWorld(this.plugin.deathcounterDocument.getDocument("location").getString("world")) != null)
                old = Bukkit.getWorld(this.plugin.deathcounterDocument.getDocument("location").getString("world"));
            this.plugin.deathcounterDocument.remove("location");
        }

        if (this.plugin.deathcounterDocument.contains("entities_dynamic")) {
            if (old == null) {
                this.plugin.getInstance().getLogger().severe("Could not find any entities");
                return;
            }

            for (JsonElement entity : this.plugin.deathcounterDocument.getArray("entities_dynamic")) {
                UUID uniqueId = UUID.fromString(entity.getAsString());
                if (from(old, uniqueId) == null) {
                    this.plugin.getInstance().getLogger().severe("Could not find entity [uuid=" + uniqueId + "]");
                    continue;
                }

                from(old, uniqueId).remove();
            }

            this.plugin.deathcounterDocument.remove("entities_dynamic");
        }

        if (this.plugin.deathcounterDocument.contains("entities_static")) {
            if (old == null) {
                this.plugin.getInstance().getLogger().severe("Could not find any entities");
                return;
            }

            for (JsonElement entity : this.plugin.deathcounterDocument.getArray("entities_static")) {
                UUID uniqueId = UUID.fromString(entity.getAsString());
                if (from(old, uniqueId) == null) {
                    this.plugin.getInstance().getLogger().severe("Could not find entity [uuid=" + uniqueId + "]");
                    continue;
                }

                from(old, uniqueId).remove();
            }

            this.plugin.deathcounterDocument.remove("entities_static");
        }

        new Document("location", position)
                .appendList("entities_dynamic", dynamicDisplays)
                .appendList("entities_static", staticDisplays)
                .saveAsConfig(Paths.get(this.plugin.getInstance().getDataFolder().getAbsolutePath(), "deathcounter.json"));
        this.plugin.deathcounterDocument = Document.loadDocument(Paths.get(this.plugin.getInstance().getDataFolder().getAbsolutePath(), "deathcounter.json"));
    }

    private Entity from(World world, UUID uuid) {
        return world.getEntities().stream()
                .filter(entity -> entity.getUniqueId().equals(uuid))
                .findFirst()
                .orElse(null);
    }
}
