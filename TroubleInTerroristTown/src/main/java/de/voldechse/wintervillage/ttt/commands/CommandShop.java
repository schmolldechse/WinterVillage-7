package de.voldechse.wintervillage.ttt.commands;

import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import de.voldechse.wintervillage.ttt.roles.Role;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandShop implements CommandExecutor {
    
    private final TTT plugin;

    public CommandShop() {
        this.plugin = InjectionLayer.ext().instance(TTT.class);

        this.plugin.getInstance().getCommand("shop").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        Player player = (Player) commandSender;

        if (this.plugin.gameManager.isSpectator(player)) return true;

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();
        if (gamePhase != Types.INGAME) return true;

        if (!this.plugin.roleManager.isPlayerAssigned(player)) return true;

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
            default -> {
                return true;
            }
        }

        return false;
    }
}