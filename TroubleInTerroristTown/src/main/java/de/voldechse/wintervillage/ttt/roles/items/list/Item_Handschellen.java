package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.library.countdown.CountdownListener;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class Item_Handschellen extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    private Countdown countdown;

    private Map<Player, ItemStack[]> PLAYER_INVENTORY = new HashMap<>();

    @Override
    public String getName() {
        return "§7Handschellen";
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.CHAIN, 1, this.getName())
                .lore("",
                        "§7Entwaffne einen Spieler für kurze Zeit",
                        "",
                        "§ePreis: " + (traitor ? "§c" : "§a") + this.getNeededPoints() + " Punkte",
                        "",
                        "§a<Klicke zum Kaufen>")
                .build();
    }

    @Override
    public int getNeededPoints() {
        return 4;
    }

    @Override
    public void equipItems(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.CHAIN, 1, this.getName())
                .lore("",
                        "§7Entwaffne einen Spieler für kurze Zeit")
                .build());
    }

    @Override
    public int howOftenBuyable() {
        return 2;
    }

    @Override
    public boolean alradyInInventory(Player player) {
        return player.getInventory().contains(Material.CHAIN);
    }

    @EventHandler
    public void execute(PlayerInteractAtEntityEvent event) {
        Player player = (Player) event.getPlayer();

        if (event.getRightClicked() instanceof Player rightClicked) {
            event.setCancelled(true);

            if (player.getItemInHand().getType() == Material.AIR) return;
            if (!player.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(this.getName())) return;

            if (this.plugin.gameManager.isSpectator(player)) return;

            if (this.plugin.roleManager.getRole(player) == null) return;

            if (!this.plugin.roleManager.isPlayerAssigned(player, 1)) return;

            if (this.plugin.roleManager.getRole(rightClicked) == null) return;

            if (this.plugin.roleManager.isPlayerAssigned(rightClicked, 1)) {
                //player.sendMessage(this.plugin.serverPrefix + "§eDu kannst einem §9Detective §ekeine Handschellen anlegen");
                player.sendMessage(this.plugin.serverPrefix + "§eDu kannst einem §9Mr. Frost §ckeine Handschellen anlegen");
                return;
            }

            if (PLAYER_INVENTORY.containsKey(rightClicked)) {
                player.sendMessage(this.plugin.serverPrefix + "§cIst bereits entwaffnet!");
                return;
            }

            player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);

            countdown = new Countdown(this.plugin.getInstance(), new CountdownListener() {
                @Override
                public void start() {
                    PLAYER_INVENTORY.put(rightClicked, rightClicked.getInventory().getContents());

                    for (int i = 0; i < 9; i++)
                        rightClicked.getInventory().setItem(i, new ItemBuilder(Material.BARRIER, 1, "§4ENTWAFFNET").build());
                    plugin.roleManager.getRole(1).getPlayers().forEach(detective -> detective.sendMessage(plugin.serverPrefix + "§a" + rightClicked.getName() + " §eist nun entwaffnet!"));
                    rightClicked.sendTitle("§9" + player.getName(), "§fhat dich vorerst entwaffnet!");
                }

                @Override
                public void stop() {
                    if (plugin.roleManager.isPlayerAssigned(rightClicked)) {
                        rightClicked.getInventory().clear();

                        ItemStack[] inventoryContents = PLAYER_INVENTORY.get(rightClicked);
                        rightClicked.getInventory().setContents(inventoryContents);

                        PLAYER_INVENTORY.remove(rightClicked);

                        rightClicked.sendMessage(plugin.serverPrefix + "§eDu bist nun frei");
                    }

                    plugin.roleManager.getRole(1).getPlayers().forEach(detective -> detective.sendMessage(plugin.serverPrefix + "§a" + player.getName() + " §eist nun wieder bewaffnet!"));
                }

                @Override
                public void second(int v0) {
                }

                @Override
                public void sleep() {
                }
            });
            countdown.startCountdown(20, false);
        }
    }
}