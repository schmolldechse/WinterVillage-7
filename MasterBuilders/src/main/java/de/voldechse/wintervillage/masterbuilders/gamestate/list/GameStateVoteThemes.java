package de.voldechse.wintervillage.masterbuilders.gamestate.list;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.library.countdown.CountdownListener;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.masterbuilders.MasterBuilders;
import de.voldechse.wintervillage.masterbuilders.gamestate.GameState;
import de.voldechse.wintervillage.masterbuilders.gamestate.Types;
import de.voldechse.wintervillage.masterbuilders.listener.InventoryClickListener;
import de.voldechse.wintervillage.masterbuilders.teams.Team;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class GameStateVoteThemes extends GameState {
    
    private final MasterBuilders plugin = InjectionLayer.ext().instance(MasterBuilders.class);

    private Countdown countdown;

    public String[] themes;

    public static String winningTheme = "§c-/-";

    @Override
    public void startCountdown() {
        this.countdown = new Countdown(this.plugin.getInstance(), new CountdownListener() {
            @Override
            public void start() {
                plugin.teamManager.setPlayersInTeam();

                teleportPlayers();

                plugin.scoreboardManager.generateScoreboard();

                plugin.PLAYING = plugin.teamManager.getPlayerTeamList().size();

                plugin.teamManager.getPlayerTeamList().forEach(allPlayers -> {
                    plugin.gameManager.clearPlayer(allPlayers, false);
                    allPlayers.setGameMode(GameMode.CREATIVE);
                    allPlayers.getInventory().setItem(8, new ItemBuilder(Material.BARRIER, 1, "§4ZURÜCKSETZEN").build());

                    registerVotingInventory(allPlayers);
                });

                Bukkit.getLogger().info("Es wird folgende Themen zur Auswahl geben: " + Arrays.toString(themes));
            }

            @Override
            public void stop() {
                plugin.gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F);
                plugin.teamManager.getPlayerTeamList().forEach(HumanEntity::closeInventory);

                if (!plugin.THEME_ALREADY_SET) winningTheme = getWinningTheme();
                plugin.scoreboardManager.updateScoreboard("currentTheme", " §c" + winningTheme, "");
                plugin.gameManager.broadcastTitle("§6" + winningTheme, "");

                InventoryClickListener.playersVoted.clear();
                InventoryClickListener.voteCounts.clear();
                InventoryClickListener.clickedSlot.clear();
                Bukkit.getOnlinePlayers().forEach(player -> plugin.removeMetadata(player, "ALREADY_VOTED_THEME"));

                endCountdown();
            }

            @Override
            public void second(int i) {
                plugin.scoreboardManager.updateScoreboard("ingameTimer", " §e" + String.format("%02d:%02d", (i / 60), (i % 60)), "");
                plugin.teamManager.getPlayerTeamList().forEach(player -> updateVotingInventory(player, getCountdown().getCountdownTime()));
            }

            @Override
            public void sleep() {
            }
        });
        this.countdown.startCountdown(this.plugin.voteThemesCountdown, false);
    }

    private void teleportPlayers() {
        this.plugin.teamManager.getPlayerTeamList().forEach(assigned -> {
            Team plot = this.plugin.teamManager.getTeam(assigned);
            Location teamLocation = new Location(
                    Bukkit.getWorld(plot.playerSpawn.getWorld()),
                    plot.playerSpawn.getX(),
                    plot.playerSpawn.getY(),
                    plot.playerSpawn.getZ(),
                    plot.playerSpawn.getYaw(),
                    plot.playerSpawn.getPitch()
            );
            assigned.teleport(teamLocation);
        });
    }

    @Override
    public void endCountdown() {
        this.plugin.gameStateManager.nextGameState();
    }

    @Override
    public Types getGameStatePhase() {
        return Types.VOTING_THEME;
    }

    @Override
    public Countdown getCountdown() {
        return this.countdown;
    }

    private void registerVotingInventory(Player player) {
        Inventory votingInventory = Bukkit.createInventory(null, 9 * 3, "§8Stimme für ein Thema ab");

        themes = getThemes().toArray(new String[0]);

        for (int i = 0; i < themes.length; i++) {
            String themeName = themes[i];
            votingInventory.setItem(i * 2 + 11, new ItemBuilder(Material.PAPER, 1, "§6" + themes[i]).lore("§7Klicke hier, um für das Thema abzustimmen").build());

            InventoryClickListener.voteCounts.put(themeName, 0);
        }
        votingInventory.setItem(22, new ItemBuilder(Material.CLOCK, this.plugin.voteThemesCountdown, "§7Noch §e15 §7Sekunden!").build());

        player.openInventory(votingInventory);
    }

    private void updateVotingInventory(Player player, int countdown) {
        Inventory updatedInventory = Bukkit.createInventory(null, 9 * 3, "§8Stimme für ein Thema ab");

        for (int i = 0; i < this.themes.length; i++) {
            String themeName = themes[i];
            int amount = InventoryClickListener.voteCounts.get(themeName) == 0 ? 1 : InventoryClickListener.voteCounts.get(themeName);

            ItemStack itemStack = new ItemBuilder(Material.PAPER, amount, "§6" + themes[i])
                    .lore("§7Klicke hier, um für das Thema abzustimmen")
                    .build();

            if (InventoryClickListener.playersVoted.contains(player) && i * 2 + 11 == InventoryClickListener.clickedSlot.get(player)) {
                itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

                ItemMeta itemMeta = itemStack.getItemMeta();
                List<String> lore = itemMeta.getLore();
                lore.addAll(List.of("", "§cAusgewählt"));

                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
            }

            updatedInventory.setItem(i * 2 + 11, itemStack);
        }
        updatedInventory.setItem(22, new ItemBuilder(Material.CLOCK, countdown, "§7Noch §e" + countdown + " §7Sekunden").build());

        player.openInventory(updatedInventory);
    }

    private String getWinningTheme() {
        String theme = "";
        int maxVotes = -1;
        for (Map.Entry<String, Integer> entry : InventoryClickListener.voteCounts.entrySet()) {
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                theme = entry.getKey();
            }
        }
        return theme;
    }

    private List<String> getThemes() {
        List<String> themes = new ArrayList<String>();
        try {
            themes = readThemesFromJson();
            if (themes.isEmpty()) {
                this.plugin.getInstance().getLogger().severe("Themelist is empty");
                return null;
            }
        } catch (IOException exception) {
            this.plugin.getInstance().getLogger().severe("Could not determine themelist " + exception);
        }

        return selectRandomThemes(themes, 3);
    }

    private List<String> readThemesFromJson() throws IOException {
        List<String> themes = new ArrayList<>();

        JsonArray array = this.plugin.configDocument.getArray("themes");
        for (JsonElement element : array) themes.add(element.getAsString());

        return themes;
    }

    private List<String> selectRandomThemes(List<String> themes, int numberToSelect) {
        if (numberToSelect >= themes.size()) return themes;
        List<String> selectedThemes = new ArrayList<>();
        Random random = new Random();
        while (selectedThemes.size() < numberToSelect) {
            int randomIndex = random.nextInt(themes.size());
            String randomTheme = themes.get(randomIndex);

            if (!selectedThemes.contains(randomTheme)) selectedThemes.add(randomTheme);
        }
        return selectedThemes;
    }
}