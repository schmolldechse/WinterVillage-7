package de.voldechse.wintervillage.aura.gamestate.list;

import de.voldechse.wintervillage.aura.Aura;
import de.voldechse.wintervillage.aura.gamestate.GameState;
import de.voldechse.wintervillage.aura.gamestate.Types;
import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.library.countdown.CountdownListener;
import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class GameStateIngame extends GameState {

    private final Aura plugin = InjectionLayer.ext().instance(Aura.class);

    private Countdown countdown;

    @Override
    public void startCountdown() {
        int pvpEnablingAfter = this.plugin.ingameCountdown - this.plugin.pvpEnabledAfter;
        int worldBorderAfter = this.plugin.ingameCountdown - this.plugin.worldBorderAfter;
        int compassAfter = this.plugin.ingameCountdown - this.plugin.compassAfter;

        this.countdown = new Countdown(this.plugin.getInstance(), new CountdownListener() {
            @Override
            public void start() {
                plugin.gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F);

                Bukkit.broadcastMessage(plugin.serverPrefix + "§eDiese Runde startete mit §b" + plugin.gameManager.getLivingPlayers().size() + "§8/§b" + plugin.PLAYING + " §eSpieler");
            }

            @Override
            public void stop() {
                endCountdown();
            }

            @Override
            public void second(int var0) {
                plugin.scoreboardManager.updateScoreboard("ingameTimer", " §e" + String.format("%02d:%02d", (var0 / 60), (var0 % 60)), " " + (plugin.PVP_ENABLED ? "" : " §c§o(Schutzzeit)"));
                plugin.scoreboardManager.updateScoreboard("currentPlayers", " §a" + plugin.gameManager.getLivingPlayers().size() + "§8/§a" + plugin.PLAYING, "");

                if (var0 == plugin.ingameCountdown) {
                    Bukkit.broadcastMessage(plugin.serverPrefix + "§7Die Schutzzeit endet in §e" + plugin.pvpEnabledAfter + " §7Sekunden");
                    plugin.gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f);
                } else if (var0 == pvpEnablingAfter) {
                    plugin.PVP_ENABLED = true;

                    Bukkit.broadcastMessage(plugin.serverPrefix + "§eDie Schutzzeit ist vorbei");
                    plugin.gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f);
                }

                if (plugin.USE_WORLDBORDER && var0 == worldBorderAfter)
                    plugin.worldBorderController.start();

                if (var0 == compassAfter) {
                    plugin.gameManager.getLivingPlayers().forEach(player -> {
                        player.getInventory().addItem(new ItemBuilder(Material.COMPASS, 1, "§cKompass").build());
                    });
                    plugin.gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 2.0f);
                    Bukkit.broadcastMessage(plugin.serverPrefix + "Alle Spieler haben einen Kompass erhalten");
                }

                switch (var0) {
                    case 60, 50, 40, 30, 20, 10, 5, 4, 3, 2 -> {
                        Bukkit.broadcastMessage(plugin.serverPrefix + "§cDas Spiel endet in §b" + var0 + " §cSekunden");
                    }

                    case 1 -> {
                        Bukkit.broadcastMessage(plugin.serverPrefix + "§cDas Spiel endet in §b" + var0 + " §cSekunde");
                    }

                    default -> {
                    }
                }
            }

            @Override
            public void sleep() {

            }
        });

        this.countdown.startCountdown(this.plugin.ingameCountdown, false);
    }

    @Override
    public void endCountdown() {
        this.plugin.gameStateManager.nextGameState();
    }

    @Override
    public Types getGameStatePhase() {
        return Types.INGAME;
    }

    @Override
    public Countdown getCountdown() {
        return this.countdown;
    }
}