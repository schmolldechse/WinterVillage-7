package de.voldechse.wintervillage.listener;

import de.voldechse.wintervillage.WinterVillage;
import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.util.Position;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.math.BigDecimal;

public class SignChangeListener implements Listener {

    @EventHandler
    public void execute(SignChangeEvent event) {
        Player player = event.getPlayer();

        WinterVillage plugin = InjectionLayer.ext().instance(WinterVillage.class);

        if (!plugin.permissionManagement.containsUserAsync(player.getUniqueId()).join()) return;
        PermissionUser permissionUser = plugin.permissionManagement.userAsync(player.getUniqueId()).join();
        PermissionGroup permissionGroup = plugin.permissionManagement.highestPermissionGroup(permissionUser);
        if (permissionGroup == null) return;

        if (!(event.getBlock().getState() instanceof Sign)) return;
        Sign sign = (Sign) event.getBlock().getState();

        switch (event.getLine(0)) {
            case "[farmwelt]" -> {
                event.setLine(0, "§f-§c-§f-§c-§f-§c-§f-§c-§f-§c-§f-§c-§f-");
                event.setLine(1, "§0Zur §aFarmwelt");
                event.setLine(2, "§0teleportieren");
                event.setLine(3, "§f-§c-§f-§c-§f-§c-§f-§c-§f-§c-§f-§c-§f-");

                sign.setEditable(false);
            }

            case "[nether]" -> {
                event.setLine(0, "§f-§c-§f-§c-§f-§c-§f-§c-§f-§c-§f-§c-§f-");
                event.setLine(1, "§0In den §cNether");
                event.setLine(2, "§0teleportieren");
                event.setLine(3, "§f-§c-§f-§c-§f-§c-§f-§c-§f-§c-§f-§c-§f-");

                sign.setEditable(false);
            }

            case "[end]" -> {
                if (permissionGroup.potency() <= plugin.potencyDeveloper) {
                    event.setCancelled(true);
                    return;
                }

                event.setLine(0, "§f-§c-§f-§c-§f-§c-§f-§c-§f-§c-§f-§c-§f-");
                event.setLine(1, "§0In das §5End");
                event.setLine(2, "§0teleportieren");
                event.setLine(3, "§f-§c-§f-§c-§f-§c-§f-§c-§f-§c-§f-§c-§f-");

                sign.setEditable(false);
            }

            case "[shop]" -> {
                if (!player.getWorld().getName().equalsIgnoreCase("world")) {
                    event.setCancelled(true);
                    player.sendMessage(plugin.serverPrefix + "§cDu kannst hier keinen Shop erstellen");
                    return;
                }

                if (plugin.shopDatabase.isSavedPlayerShop(sign.getLocation())) {
                    event.setCancelled(true);

                    event.setLine(0, "");
                    event.setLine(1, "");
                    event.setLine(2, "");
                    event.setLine(3, "");

                    player.sendMessage(plugin.serverPrefix + "§cAn dieser Stelle steht bereits ein Shop");
                    return;
                }

                if (plugin.shopDatabase.shops(player.getUniqueId()).size() >= 3 && permissionGroup.potency() <= plugin.potencyContentCreator) {
                    event.setCancelled(true);

                    event.setLine(0, "");
                    event.setLine(1, "");
                    event.setLine(2, "");
                    event.setLine(3, "");

                    player.sendMessage(plugin.serverPrefix + "§cDu hast die maximale Anzahl an Shops erreicht");
                    return;
                }

                if (player.hasMetadata("SHOP_CONFIGURATION")) {
                    event.setCancelled(true);

                    event.setLine(0, "");
                    event.setLine(1, "");
                    event.setLine(2, "");
                    event.setLine(3, "");

                    player.sendMessage(plugin.serverPrefix + "§cDu musst deinen aktuellen Shop, den du bearbeiten möchtest, erst fertigstellen");
                    return;
                }

                if (event.getLine(1).isEmpty() || event.getLine(2).isEmpty() || event.getLine(3).isEmpty()) {
                    event.setCancelled(true);

                    event.setLine(0, "");
                    event.setLine(1, "");
                    event.setLine(2, "");
                    event.setLine(3, "");

                    player.sendMessage(plugin.serverPrefix + "§cEin Fehler ist beim Erstellen deines Shops aufgetreten");
                    for (int i = 1; i <= 3; i++)
                        if (event.getLine(i).isEmpty())
                            player.sendMessage(plugin.serverPrefix + "§cZeile " + (i + 1) + " ist leer");
                    return;
                }

                BigDecimal price = BigDecimal.valueOf(0);
                BigDecimal amount = BigDecimal.valueOf(0);
                try {
                    price = new BigDecimal(event.getLine(2));
                    amount = new BigDecimal(event.getLine(3));
                } catch (NumberFormatException exception) {
                    event.setCancelled(true);

                    event.setLine(0, "");
                    event.setLine(1, "");
                    event.setLine(2, "");
                    event.setLine(3, "");

                    player.sendMessage(plugin.serverPrefix + "§cEin Fehler ist beim Erstellen deines Shops aufgetreten");
                    player.sendMessage(plugin.serverPrefix + "§cUngültige Zahl");
                    return;
                }

                if (price.compareTo(BigDecimal.ZERO) < 0 || amount.compareTo(BigDecimal.ZERO) < 0) {
                    event.setCancelled(true);

                    event.setLine(0, "");
                    event.setLine(1, "");
                    event.setLine(2, "");
                    event.setLine(3, "");

                    player.sendMessage(plugin.serverPrefix + "§cEin Fehler ist beim Erstellen deines Shops aufgetreten");

                    if (price.compareTo(BigDecimal.ZERO) < 0)
                        player.sendMessage(plugin.serverPrefix + "§cDer Preis beträgt weniger als 0 $ §o(Zeile 3)");
                    if (amount.compareTo(BigDecimal.ONE) < 0)
                        player.sendMessage(plugin.serverPrefix + "§cDie Anzahl beträgt weniger als 1 Item §o(Zeile 4)");
                    return;
                }

                if (amount.scale() > 0) {
                    event.setCancelled(true);

                    event.setLine(0, "");
                    event.setLine(1, "");
                    event.setLine(2, "");
                    event.setLine(3, "");

                    player.sendMessage(plugin.serverPrefix + "§cDie Anzahl der Items, die pro Preis verkauft werden soll, muss eine ganze Zahl sein");
                    return;
                }

                BigDecimal comparing = new BigDecimal(27*64);
                if (amount.compareTo(comparing) > 0) {
                    event.setCancelled(true);

                    event.setLine(0, "");
                    event.setLine(1, "");
                    event.setLine(2, "");
                    event.setLine(3, "");

                    player.sendMessage(plugin.serverPrefix + "§cDein Shop hat eine maximale Kapazität von §e1.728 §cItems");
                    return;
                }

                plugin.setMetadata(player, "SHOP_CONFIGURATION",
                        new Document("shop_location", new Position(
                                event.getBlock().getX(),
                                event.getBlock().getY(),
                                event.getBlock().getZ(),
                                event.getBlock().getWorld().getName())
                        ).append("shop_name", event.getLine(1))
                                .append("price", price)
                                .append("amount", amount)
                                .append("sign_side", event.getSide().name()));

                event.setLine(0, "");
                event.setLine(1, "§cSHOP");
                event.setLine(2, "§cKONFIGURATION");
                event.setLine(3, "");

                sign.setEditable(false);

                player.sendMessage(plugin.serverPrefix + "§aDu hast die Konfiguration zu deinem eigenem Shop erstellt! Rechtsklicke das Schild mit einem Block/ Item, welches du verkaufen willst, um die Konfiguration abzuschließen");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }

            case "[adminshop]" -> {
                if (!player.hasPermission("wintervillage.edit.create.adminshop")) {
                    event.setCancelled(true);
                    return;
                }

                event.setLine(0, "");
                event.setLine(1, "§0[§4Admin-Shop§0]");
                event.setLine(2, "§fItems verkaufen");
                event.setLine(3, "");

                sign.setEditable(false);

                player.sendMessage(plugin.serverPrefix + "§aErstellt!");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }
        }
    }
}
