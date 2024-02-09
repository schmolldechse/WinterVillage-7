package de.voldechse.wintervillage.listener;

import de.voldechse.wintervillage.WinterVillage;
import de.voldechse.wintervillage.database.ShopDatabase;
import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.util.Position;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlockBreakListener implements Listener {

    @EventHandler
    public void execute(BlockBreakEvent event) {
        Player player = event.getPlayer();

        WinterVillage plugin = InjectionLayer.ext().instance(WinterVillage.class);

        if (!player.isOp()
                && ((event.getBlock().getWorld().getName().equalsIgnoreCase("world_nether") && withinRadius(Bukkit.getWorld("world_nether").getSpawnLocation(), 15).contains(event.getBlock()))
                || ((event.getBlock().getWorld().getName().equalsIgnoreCase("world_farmwelt") && withinRadius(Bukkit.getWorld("world_farmwelt").getSpawnLocation(), 15).contains(event.getBlock()))
                || ((event.getBlock().getWorld().getName().equalsIgnoreCase("world_the_end") && withinRadius(Bukkit.getWorld("world_the_end").getSpawnLocation(), 5).contains(event.getBlock())))))) {
            event.setCancelled(true);
            player.sendMessage(plugin.serverPrefix + "§cDu kannst hier nicht abbauen");
            return;
        }

        if (event.getBlock().getState() instanceof Sign) {
            Sign sign = (Sign) event.getBlock().getState();

            if (sign.getLine(1).equalsIgnoreCase("§0[§4Admin-Shop§0]")) {
                if (!player.hasPermission("wintervillage.edit.create.adminshop")) {
                    event.setCancelled(true);
                    return;
                }

                player.sendMessage(plugin.serverPrefix + "§aEntfernt!");
                return;
            }

            if (sign.getLine(0).equalsIgnoreCase("§0[§aShop§0]")) {
                if (!plugin.shopDatabase.isSavedPlayerShop(event.getBlock().getLocation())) return;

                ShopDatabase.PlayerShop playerShop = plugin.shopDatabase.shop(event.getBlock().getLocation());
                if (!playerShop.owner.equals(player.getUniqueId())) {
                    event.setCancelled(true);
                    player.sendMessage(plugin.serverPrefix + "§cDieser Shop gehört nicht dir");
                    return;
                }

                if (from(playerShop.location.getWorld(), playerShop.itemDisplay) != null)
                    from(playerShop.location.getWorld(), playerShop.itemDisplay).remove();

                plugin.shopDatabase.removePlayerShop(playerShop.shopIdentifier);
                player.sendMessage(plugin.serverPrefix + "§eDu hast deinen Shop gelöscht");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                for (int i = 1; i <= playerShop.currentAmount.intValue(); i++)
                    sign.getBlock().getWorld().dropItem(sign.getLocation().add(0, 1, 0), new ItemStack(playerShop.material));

                sign.getBlock().getWorld().dropItem(sign.getLocation().add(0, 1, 0), new ItemStack(sign.getType()));
                sign.getBlock().setType(Material.AIR);
                return;
            }

            if (sign.getLine(0).isEmpty()
                    && sign.getLine(1).equalsIgnoreCase("§cSHOP")
                    && sign.getLine(2).equalsIgnoreCase("§cKONFIGURATION")
                    && sign.getLine(3).isEmpty()) {
                event.setCancelled(true);

                if (!player.hasMetadata("SHOP_CONFIGURATION")) {
                    player.sendMessage(plugin.serverPrefix + "§cDu kannst diesen Shop nicht bearbeiten, da er nicht dir gehört");
                    return;
                }

                Document document = (Document) player.getMetadata("SHOP_CONFIGURATION").get(0).value();

                if (document.get("shop_location") == null || !document.contains("shop_location")) {
                    player.sendMessage(plugin.serverPrefix + "§cEin Fehler ist während des Erstellens deines Shops aufgetreten");
                    player.sendMessage(plugin.serverPrefix + "§bshop_location §cis null");
                    return;
                }

                Document shop_location = document.getDocument("shop_location");
                Position position = new Position(shop_location.getDouble("x"), shop_location.getDouble("y"), shop_location.getDouble("z"), shop_location.getString("world"));

                if (!(event.getBlock().getLocation().getX() == position.x
                        && event.getBlock().getLocation().getY() == position.y
                        && event.getBlock().getLocation().getZ() == position.z
                        && event.getBlock().getLocation().getWorld().getName().equals(position.world))) {
                    player.sendMessage(plugin.serverPrefix + "§cDieser Shop gehört nicht dir");
                    return;
                }

                player.sendMessage(plugin.serverPrefix + "§cVorgang abgebrochen");

                plugin.removeMetadata(player, "SHOP_CONFIGURATION");

                sign.getWorld().dropItem(sign.getLocation().add(0, 1, 0), new ItemBuilder(sign.getType(), 1).build());
                sign.getBlock().setType(Material.AIR);
            }

            return;
        }

        BlockFace blockFace = BlockFace.SELF;
        for (BlockFace values : BlockFace.values()) {
            if (!(values == BlockFace.DOWN
                    || values == BlockFace.UP
                    || values == BlockFace.SELF
                    || values == BlockFace.NORTH
                    || values == BlockFace.SOUTH
                    || values == BlockFace.EAST
                    || values == BlockFace.WEST)) continue;
            if (!(event.getBlock().getRelative(values).getState() instanceof Sign)) continue;
            blockFace = values;
        }

        if (!(event.getBlock().getRelative(blockFace).getState() instanceof Sign)) return;

        Sign sign = (Sign) event.getBlock().getRelative(blockFace).getState();

        if (!plugin.shopDatabase.isSavedPlayerShop(event.getBlock().getRelative(blockFace).getLocation())) return;

        if (!sign.getLine(0).equalsIgnoreCase("§0[§aShop§0]")) return;

        ShopDatabase.PlayerShop playerShop = plugin.shopDatabase.shop(event.getBlock().getRelative(blockFace).getLocation());
        if (!playerShop.owner.equals(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(plugin.serverPrefix + "§cDieser Shop gehört nicht dir");
            return;
        }

        if (from(playerShop.location.getWorld(), playerShop.itemDisplay) != null)
            from(playerShop.location.getWorld(), playerShop.itemDisplay).remove();

        plugin.shopDatabase.removePlayerShop(playerShop.shopIdentifier);
        player.sendMessage(plugin.serverPrefix + "§eDu hast deinen Shop gelöscht");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

        for (int i = 1; i <= playerShop.currentAmount.intValue(); i++)
            sign.getBlock().getWorld().dropItem(sign.getLocation().add(0, 1, 0), new ItemStack(playerShop.material));

        sign.getBlock().getWorld().dropItem(sign.getLocation().add(0, 1, 0), new ItemStack(sign.getType()));
        sign.getBlock().setType(Material.AIR);
    }

    private Entity from(World world, UUID uuid) {
        return world.getEntities().stream()
                .filter(entity -> entity.getUniqueId().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    private List<Block> withinRadius(Location center, int radius) {
        List<Block> blocks = new ArrayList<>();

        for (int xOffset = -radius; xOffset <= radius; xOffset++) {
            for (int yOffset = -radius; yOffset <= radius; yOffset++) {
                for (int zOffset = -radius; zOffset <= radius; zOffset++) {
                    blocks.add(center.getWorld().getBlockAt(center.getBlockX() + xOffset, center.getBlockY() + yOffset, center.getBlockZ() + zOffset));
                }
            }
        }

        return blocks;
    }
}