package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.game.corpse.CorpseEntity;
import de.voldechse.wintervillage.ttt.game.corpse.player.CorpseData;
import de.voldechse.wintervillage.ttt.roles.Role;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Item_Radar extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @Override
    public String getName() {
        return "§3Radar";
    }

    @Override
    public int getNeededPoints() {
        return 2;
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.COMPASS, 1, this.getName())
                .lore("",
                        "§7Das Radar zeigt",
                        "§7dir den nächsten Spieler",
                        "§7in deiner Nähe",
                        "",
                        "§ePreis: " + (traitor ? "§c" : "§a") + this.getNeededPoints() + " Punkte",
                        "",
                        "§a<Klicke zum Kaufen>")
                .build();
    }

    @Override
    public boolean alradyInInventory(Player player) {
        return player.getInventory().contains(Material.COMPASS);
    }

    @Override
    public void equipItems(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.COMPASS, 1, this.getName())
                .lore("",
                        "§7Das Radar zeigt",
                        "§7dir den nächsten Spieler",
                        "§7in deiner Nähe")
                .build());
    }

    @Override
    public int howOftenBuyable() {
        return 1;
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

            Role role = this.plugin.roleManager.getRole(player);
            switch (role.roleId) {
                case 0 -> {
                    return;
                }
                case 1 -> {
                    Inventory inventory = Bukkit.createInventory(null, 27, "§3Radar");

                    inventory.setItem(11, new ItemBuilder(Material.GREEN_CONCRETE, 1, "§aSpieler").build());
                    //inventory.setItem(13, new ItemBuilder(Material.RED_CONCRETE, 1, "§cEindeutige Traitor").build());
                    inventory.setItem(13, new ItemBuilder(Material.RED_CONCRETE, "§cEindeutige Krampusse").build());
                    inventory.setItem(15, new ItemBuilder(Material.SKELETON_SKULL, 1, "§3Leichen").build());

                    player.openInventory(inventory);
                }
                case 2 -> {
                    Player randomPlayer = getRandomPlayer(this.plugin.roleManager.getRole(2).getPlayers());
                    Role randomPlayersRole = this.plugin.roleManager.getRole(randomPlayer);
                    if (randomPlayersRole == null) {
                        player.sendMessage(this.plugin.serverPrefix + "§cSomething seems to be not working. Try again");
                        return;
                    }

                    player.setCompassTarget(randomPlayer.getLocation());
                    player.sendMessage(this.plugin.serverPrefix + "§eDein Kompass zeigt nun auf §f" + randomPlayersRole.getRolePrefix() + randomPlayer.getName());
                }
            }
        }
    }

    @EventHandler
    public void execute(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (event.getClickedInventory() != null && event.getView().getTitle().equalsIgnoreCase(this.getName())) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null) return;

            if (this.plugin.gameManager.isSpectator(player)) return;

            if (this.plugin.roleManager.getRole(player) == null) return;

            switch (event.getCurrentItem().getItemMeta().getDisplayName()) {
                case "§aSpieler" -> {
                    event.getView().close();
                    player.setCompassTarget(getRandomPlayer(player).getLocation());
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                }
                //case "§cEindeutige Traitor" -> {
                case "§cEindeutige Krampusse" -> {
                    event.getView().close();
                    if (identifiedTraitors().size() == 0) {
                        event.getView().close();
                        player.setCompassTarget(player.getLocation());
                        player.playSound(player.getLocation(), Sound.ENTITY_PILLAGER_DEATH, 1.0f, 1.0f);
                        //player.sendMessage(this.plugin.serverPrefix + "§cEs wurden keine identifizierten Traitor gefunden");
                        player.sendMessage(this.plugin.serverPrefix + "§cEs wurden keine identifizierten Krampusse gefunden");
                    } else {
                        int inventorySize = (this.plugin.roleManager.getRole(2).getPlayers().size() / 9 + 1) * 9;

                        //Inventory inventory = Bukkit.createInventory(null, inventorySize, "§cEindeutige Traitor");
                        Inventory inventory = Bukkit.createInventory(null, inventorySize, "§cEindeutige Krampusse");
                        identifiedTraitors().forEach(traitor -> {
                            ItemStack itemStack = new ItemBuilder(Material.PLAYER_HEAD, 1, "§4" + player.getName()).build();
                            SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
                            skullMeta.setOwnerProfile(Bukkit.createPlayerProfile(player.getName()));
                            itemStack.setItemMeta(skullMeta);

                            inventory.addItem(itemStack);
                        });

                        player.openInventory(inventory);
                    }
                }
                case "§3Leichen" -> {
                    if (this.plugin.CORPSES_MAP.size() == 0) {
                        event.getView().close();
                        player.setCompassTarget(player.getLocation());
                        player.playSound(player.getLocation(), Sound.ENTITY_PILLAGER_DEATH, 1.0f, 1.0f);
                        player.sendMessage(this.plugin.serverPrefix + "§cEs wurden keine identifizierten Leichen gefunden");
                    } else {
                        List<CorpseData> REVEALED_CORPSES = new ArrayList<>();
                        this.plugin.CORPSES_MAP.forEach((entityId, corpse) -> {
                            boolean revealed = corpse.corpseData.getDocument().getBoolean("revealed");
                            if (revealed) REVEALED_CORPSES.add(corpse.corpseData);
                        });

                        int inventorySize = (REVEALED_CORPSES.size() / 9 + 1) * 9;

                        Inventory inventory = Bukkit.createInventory(null, inventorySize, "§3Identifizierte Leichen");
                        REVEALED_CORPSES.forEach(revealed -> {
                            long startTime = revealed.getDocument().getLong("TIMESTAMP");
                            long currentTime = System.currentTimeMillis();

                            long differenceInSeconds = (currentTime - startTime) / 1000;

                            ItemStack itemStack = new ItemBuilder(Material.PLAYER_HEAD, 1,
                                    revealed.getDocument().getString("diedPlayer_PREFIX") + revealed.getDocument().getString("diedPlayer_NAME")
                            ).build();
                            SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
                            skullMeta.setOwnerProfile(Bukkit.createPlayerProfile(player.getName()));
                            skullMeta.setLore(Arrays.asList(
                                    "§7Dieser war ein§8: §f" + revealed.getDocument().getString("diedPlayer_ROLE"),
                                    "§7Tot seit§8: §e" + String.format("%02d:%02d", (differenceInSeconds / 60), (differenceInSeconds % 60)) + " Minuten"
                            ));
                            itemStack.setItemMeta(skullMeta);

                            inventory.addItem(itemStack);
                        });

                        player.openInventory(inventory);
                    }
                }
            }
        }

        //if (event.getClickedInventory() != null && event.getView().getTitle().equalsIgnoreCase("§cEindeutige Traitor")) {
        if (event.getClickedInventory() != null && event.getView().getTitle().equalsIgnoreCase("§cEindeutige Krampusse")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null) return;

            if (this.plugin.gameManager.isSpectator(player)) return;

            if (this.plugin.roleManager.getRole(player) == null) return;

            String displayName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

            Player toSpectate = Bukkit.getPlayer(displayName);
            if (toSpectate == null) {
                player.sendMessage(this.plugin.serverPrefix + "§cSomething seems to be not working. Try again");
                return;
            }

            if (this.plugin.gameManager.isSpectator(toSpectate)) {
                player.sendMessage(this.plugin.serverPrefix + "§cSomething seems to be not working. Try again");
                return;
            }

            event.getView().close();
            player.setCompassTarget(toSpectate.getLocation());
            player.playSound(player.getLocation(), Sound.ENTITY_PILLAGER_DEATH, 1.0f, 1.0f);
        }

        if (event.getClickedInventory() != null && event.getView().getTitle().equalsIgnoreCase("§3Identifizierte Leichen")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null) return;

            if (this.plugin.gameManager.isSpectator(player)) return;

            if (this.plugin.roleManager.getRole(player) == null) return;

            if (getCorpse(event.getCurrentItem().getItemMeta().getDisplayName()) == null) return;

            Location corpseLocation = getCorpse(event.getCurrentItem().getItemMeta().getDisplayName()).getLocation();
            event.getView().close();
            player.setCompassTarget(corpseLocation);
            player.playSound(player.getLocation(), Sound.ENTITY_PILLAGER_DEATH, 1.0f, 1.0f);
        }
    }

    private CorpseData getCorpse(String displayName) {
        for (CorpseEntity corpse : this.plugin.CORPSES_MAP.values()) {
            String corpsesName = corpse.corpseData.getDocument().getString("diedPlayer_PREFIX") + corpse.corpseData.getDocument().getString("diedPlayer_NAME");
            if (corpsesName.equalsIgnoreCase(displayName)) return corpse.corpseData;
        }
        return null;
    }

    private Player getRandomPlayer(List<Player> excluded) {
        List<Player> LIST = this.plugin.roleManager.getPlayerList();
        LIST.removeAll(excluded);

        int randomIndex = new Random().nextInt(LIST.size());
        return LIST.get(randomIndex);
    }

    private Player getRandomPlayer(Player excluded) {
        List<Player> LIST = this.plugin.roleManager.getPlayerList();
        LIST.remove(excluded);

        int randomIndex = new Random().nextInt(LIST.size());
        return LIST.get(randomIndex);
    }

    private List<Player> identifiedTraitors() {
        List<Player> IDENTIFIED = new ArrayList<>();
        this.plugin.roleManager.getPlayerList().forEach(player -> {
            if (player.hasMetadata("BUSTED_TRAITOR")) IDENTIFIED.add(player);
        });
        return IDENTIFIED;
    }
}