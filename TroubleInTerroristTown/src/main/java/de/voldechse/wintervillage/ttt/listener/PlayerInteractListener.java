package de.voldechse.wintervillage.ttt.listener;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import de.voldechse.wintervillage.ttt.roles.Role;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayerInteractListener implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    private final ItemStack woodenSword, stoneSword, bow;

    {
        woodenSword = new ItemBuilder(Material.WOODEN_SWORD, 1).build();
        ItemMeta woodenSwordMeta = woodenSword.getItemMeta();
        woodenSwordMeta.setUnbreakable(true);
        woodenSword.setItemMeta(woodenSwordMeta);

        stoneSword = new ItemBuilder(Material.STONE_SWORD, 1).build();
        ItemMeta stoneSwordMeta = stoneSword.getItemMeta();
        stoneSwordMeta.setUnbreakable(true);
        stoneSword.setItemMeta(stoneSwordMeta);

        bow = new ItemBuilder(Material.BOW, 1).build();
        ItemMeta bowMeta = bow.getItemMeta();
        bowMeta.setUnbreakable(true);
        bow.setItemMeta(bowMeta);
    }

    @EventHandler
    public void execute(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();

        if (event.getItem() != null
                && event.getItem().hasItemMeta()
                && event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("§bTeleporter")) {
            if (!this.plugin.gameManager.isSpectator(player)) return;
            player.openInventory(this.plugin.gameManager.getSpectatorInventory());
        }

        if (event.getItem() != null
                && event.getItem().hasItemMeta()
                && event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("§aShop")) {
            if (this.plugin.gameManager.isSpectator(player)) return;
            if (this.plugin.roleManager.getRole(player) == null) return;
            Role role = this.plugin.roleManager.getRole(player);

            switch (role.roleId) {
                case 0 -> {
                    this.plugin.roleItemManager.openShop_innocent(player);
                }
                case 1 -> {
                    this.plugin.roleItemManager.openShop_detective(player);
                }
                case 2 -> {
                    this.plugin.roleItemManager.openShop_traitor(player);
                }
            }
        }

        if (event.getClickedBlock() != null
                && event.getClickedBlock().getType() == Material.CHEST
                && !player.hasMetadata("BUILD_MODE")
        && !player.hasMetadata("EDIT_CHESTS")) {
            event.setCancelled(true);

            if (gamePhase == Types.PREPARING_START
                    && this.plugin.gameStateManager.currentGameState().getCountdown().getCountdownTime() >= 30) return;

            if (this.plugin.gameManager.isSpectator(player)) return;

            if (gamePhase == Types.LOBBY || gamePhase == Types.RESTART) return;

            if (!equip(player)) return;

            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.0f);
            event.getClickedBlock().setType(Material.AIR);
        }

        if (event.getClickedBlock() != null
                && event.getClickedBlock().getType() == Material.ENDER_CHEST
                && !player.hasMetadata("BUILD_MODE")
                && !player.hasMetadata("EDIT_CHESTS")) {
            event.setCancelled(true);

            if (gamePhase == Types.PREPARING_START
                    && this.plugin.gameStateManager.currentGameState().getCountdown().getCountdownTime() >= 30) return;

            if (this.plugin.gameManager.isSpectator(player)) return;

            if (gamePhase != Types.INGAME) {
                player.sendMessage(this.plugin.serverPrefix + "§cDu kannst dieses Item erst nach Ende der Schutzzeit aufnehmen");
                return;
            }

            player.getInventory().addItem(new ItemBuilder(Material.IRON_SWORD, 1).build());
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.0f);
            event.getClickedBlock().setType(Material.AIR);
        }
    }

    /**
     * private boolean equip(Player player) {
     * if (!player.getInventory().contains(Material.BOW)
     * && !player.getInventory().contains(Material.WOODEN_SWORD)
     * && !player.getInventory().contains(Material.STONE_SWORD)) {
     * int index = RANDOM.nextInt(3);
     * switch (index) {
     * case 0 -> {
     * player.getInventory().addItem(bow);
     * player.getInventory().addItem(new ItemBuilder(Material.ARROW, 32).build());
     * }
     * case 1 -> {
     * player.getInventory().addItem(woodenSword);
     * }
     * case 2 -> {
     * player.getInventory().addItem(stoneSword);
     * }
     * }
     * } else if (player.getInventory().contains(Material.BOW)
     * && !player.getInventory().contains(Material.WOODEN_SWORD)
     * && !player.getInventory().contains(Material.STONE_SWORD)) {
     * int index = RANDOM.nextInt(2);
     * switch (index) {
     * case 0 -> {
     * player.getInventory().addItem(woodenSword);
     * }
     * case 1 -> {
     * player.getInventory().addItem(stoneSword);
     * }
     * }
     * } else if (player.getInventory().contains(Material.WOODEN_SWORD)
     * && !player.getInventory().contains(Material.STONE_SWORD)
     * && !player.getInventory().contains(Material.BOW)) {
     * int index = RANDOM.nextInt(2);
     * switch (index) {
     * case 0 -> {
     * player.getInventory().addItem(bow);
     * player.getInventory().addItem(new ItemBuilder(Material.ARROW, 32).build());
     * }
     * case 1 -> {
     * player.getInventory().addItem(stoneSword);
     * }
     * }
     * } else if (player.getInventory().contains(Material.STONE_SWORD)
     * && !player.getInventory().contains(Material.WOODEN_SWORD)
     * && !player.getInventory().contains(Material.BOW)) {
     * int index = RANDOM.nextInt(2);
     * switch (index) {
     * case 0 -> {
     * player.getInventory().addItem(bow);
     * player.getInventory().addItem(new ItemBuilder(Material.ARROW, 32).build());
     * }
     * case 1 -> {
     * player.getInventory().addItem(woodenSword);
     * }
     * }
     * } else if (player.getInventory().contains(Material.BOW)
     * && player.getInventory().contains(Material.WOODEN_SWORD)
     * && !player.getInventory().contains(Material.STONE_SWORD)) {
     * player.getInventory().addItem(stoneSword);
     * } else if (player.getInventory().contains(Material.BOW)
     * && player.getInventory().contains(Material.STONE_SWORD)
     * && !player.getInventory().contains(Material.WOODEN_SWORD)) {
     * player.getInventory().addItem(woodenSword);
     * } else if (player.getInventory().contains(Material.WOODEN_SWORD)
     * && player.getInventory().contains(Material.STONE_SWORD)
     * && !player.getInventory().contains(Material.BOW)) {
     * player.getInventory().addItem(bow);
     * player.getInventory().addItem(new ItemBuilder(Material.ARROW, 32).build());
     * } else if (player.getInventory().contains(Material.WOODEN_SWORD)
     * && player.getInventory().contains(Material.STONE_SWORD)
     * && player.getInventory().contains(Material.BOW)) {
     * <p>
     * }
     * }
     */

    private boolean equip(Player player) {
        List<ItemStack> items = new ArrayList<>();

        if (!(player.getInventory().contains(Material.WOODEN_SWORD) || (player.getInventory().getItemInOffHand().getType() == Material.WOODEN_SWORD))) items.add(woodenSword);
        if (!(player.getInventory().contains(Material.STONE_SWORD) || player.getInventory().getItemInOffHand().getType() == Material.STONE_SWORD)) items.add(stoneSword);
        if (player.getInventory().getItemInOffHand().getType() != Material.BOW && player.getInventory().getItemInOffHand().getType() != Material.ARROW
                && !(player.getInventory().contains(Material.BOW) && player.getInventory().contains(Material.ARROW)))
            items.add(bow);

        if (!items.isEmpty()) {
            int index = new Random().nextInt(items.size());

            ItemStack itemStack = items.get(index);
            player.getInventory().addItem(items.get(index));
            if (itemStack.getType() == Material.BOW)
                player.getInventory().addItem(new ItemBuilder(Material.ARROW, 32).build());
            return true;
        } else return false;
    }
}