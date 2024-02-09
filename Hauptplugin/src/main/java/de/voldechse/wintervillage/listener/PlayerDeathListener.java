package de.voldechse.wintervillage.listener;

import com.google.gson.JsonElement;
import de.voldechse.wintervillage.WinterVillage;
import de.voldechse.wintervillage.database.DeathsDatabase;
import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.UUID;

public class PlayerDeathListener implements Listener {

    private final WinterVillage plugin = InjectionLayer.ext().instance(WinterVillage.class);

    @EventHandler
    public void execute(PlayerDeathEvent event) {
        Player player = event.getEntity();

        this.plugin.deathsDatabase.modify(player.getUniqueId(), 1);
        this.updateCounter();

        if (!plugin.permissionManagement.containsUserAsync(player.getUniqueId()).join()) return;

        PermissionUser permissionUser = plugin.permissionManagement.userAsync(player.getUniqueId()).join();
        if (permissionUser == null) return;
        PermissionGroup permissionGroup = plugin.permissionManagement.highestPermissionGroup(permissionUser);
        if (permissionGroup == null) return;

        ItemStack itemStack = new ItemBuilder(Material.PLAYER_HEAD, 1).build();
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        skullMeta.setOwnerProfile(Bukkit.createPlayerProfile(player.getName()));
        skullMeta.setDisplayName("§fKopf von " + permissionGroup.color() + permissionUser.name());
        itemStack.setItemMeta(skullMeta);

        event.getEntity().getLocation().getWorld().dropItem(event.getEntity().getLocation(), itemStack);
    }

    private void updateCounter() {
        if (!this.plugin.deathcounterDocument.contains("location")) {
            this.plugin.getInstance().getLogger().severe("Could not find 'location' entry in deathcounter.json");
            return;
        }

        Document locationDocument = this.plugin.deathcounterDocument.getDocument("location");
        if (Bukkit.getWorld(locationDocument.getString("world")) == null) {
            this.plugin.getInstance().getLogger().severe("Could not find world named " + locationDocument.getString("world"));
            return;
        }

        World world = Bukkit.getWorld(locationDocument.getString("world"));

        if (!this.plugin.deathcounterDocument.contains("entities_dynamic")) {
            this.plugin.getInstance().getLogger().severe("Could not find 'entities_dynamic' entry in deathcounter.json");
            return;
        }

        List<DeathsDatabase.DeathsData> deaths = this.plugin.deathsDatabase.top(10);

        int index = 1;
        for (JsonElement entityElement : this.plugin.deathcounterDocument.getArray("entities_dynamic")) {
            UUID uniqueId = UUID.fromString(entityElement.getAsString());
            if (from(world, uniqueId) == null) {
                this.plugin.getInstance().getLogger().severe("Could not find entity [uuid=" + uniqueId + "]");
                continue;
            }

            Entity entity = from(world, uniqueId);
            if (!(entity instanceof TextDisplay)) {
                this.plugin.getInstance().getLogger().severe("Entitiy isn't a instance of TextDisplay");
                continue;
            }

            DeathsDatabase.DeathsData deathsData = deaths.get(index - 1);

            if (!this.plugin.permissionManagement.containsUserAsync(deathsData.uuid).join()) {
                this.plugin.getInstance().getLogger().severe("An error occurred while searching " + deathsData.uuid + "'s entry in database");
                continue;
            }

            PermissionUser permissionUser = this.plugin.permissionManagement.userAsync(deathsData.uuid).join();
            if (permissionUser == null) {
                this.plugin.getInstance().getLogger().severe("An error occurred while searching " + deathsData.uuid + "'s entry in database");
                continue;
            }
            PermissionGroup permissionGroup = this.plugin.permissionManagement.highestPermissionGroup(permissionUser);
            if (permissionGroup == null) {
                this.plugin.getInstance().getLogger().severe("An error occurred while searching " + permissionUser.name() + "'s entry in database");
                continue;
            }

            TextDisplay display = (TextDisplay) entity;
            display.setText("§d" + index + ". §fPlatz §8§l: " + permissionGroup.color() + permissionUser.name() + " §8-§l §e" + deathsData.deaths + " §fTode");

            index++;

            if (index == 10) break;
        }

        this.plugin.getInstance().getLogger().info("Updated deathcounter");
    }

    private Entity from(World world, UUID uuid) {
        return world.getEntities().stream()
                .filter(entity -> entity.getUniqueId().equals(uuid))
                .findFirst()
                .orElse(null);
    }
}