package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.game.corpse.CorpseEntity;
import de.voldechse.wintervillage.ttt.game.corpse.player.CorpseData;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Item_BoomBody extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @Override
    public String getName() {
        //return "§3Boom-Body";
        return "§3Falscher Elf";
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.ZOMBIE_SPAWN_EGG, 1, this.getName())
                .lore("",
                        "§7Erschaffe eine Leiche,",
                        "§7welche explodiert, sobald",
                        //"§7sie von einem Spieler",
                        "§7sie von einem §9Mr. Frost",
                        "§7identifiziert wird",
                        "",
                        "§ePreis: " + (traitor ? "§c" : "§a") + this.getNeededPoints() + " Punkte",
                        "",
                        "§a<Klicke zum Kaufen>")
                .build();
    }

    @Override
    public int getNeededPoints() {
        return 3;
    }

    @Override
    public int howOftenBuyable() {
        return 1;
    }

    @Override
    public boolean alradyInInventory(Player player) {
        return player.getInventory().contains(Material.ZOMBIE_SPAWN_EGG);
    }

    @Override
    public void equipItems(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.ZOMBIE_SPAWN_EGG, 1, this.getName())
                .lore("",
                        "§7Erschaffe eine Leiche,",
                        "§7welche explodiert, sobald",
                        "§7sie von einem §9Mr. Frost",
                        "§7identifiziert wird")
                .build());
    }

    @EventHandler
    public void execute(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getItem() != null
                && event.getItem().getItemMeta().getDisplayName() != null
                && event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(this.getName())) {
            event.setCancelled(true);

            if (this.plugin.gameManager.isSpectator(player)) return;

            if (this.plugin.roleManager.getRole(player) == null) return;

            if (!this.plugin.roleManager.isPlayerAssigned(player, 2)) return;

            player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);

            CorpseData corpseData = new CorpseData(
                    player.getLocation(),
                    this.plugin.deadSkinTexture,
                    new Document("fakeCorpse", true)
                            .append("source", player.getUniqueId())
            );
            corpseData.setIdentified(false);
            new CorpseEntity(corpseData).spawn(true);
        }
    }
}