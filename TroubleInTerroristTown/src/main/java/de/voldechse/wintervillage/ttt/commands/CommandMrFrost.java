package de.voldechse.wintervillage.ttt.commands;

import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import de.voldechse.wintervillage.ttt.roles.Role;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandMrFrost implements CommandExecutor {

    private final TTT plugin;

    public CommandMrFrost() {
        this.plugin = InjectionLayer.ext().instance(TTT.class);

        this.plugin.getInstance().getCommand("mrfrost").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        Player player = (Player) commandSender;

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();
        if (gamePhase != Types.LOBBY) {
            player.sendMessage(this.plugin.serverPrefix + "§cDas kannst du nur in der Lobby");
            return true;
        }

        if (this.plugin.roleManager.isPlayerAssigned(player)) {
            Role role = this.plugin.roleManager.getRole(player);
            if (role.roleId == 1) {
                player.sendMessage(this.plugin.serverPrefix + "§aDir wird nun wieder eine zufällige Rolle zugewiesen");
                this.plugin.roleManager.removeFromRole(player, 1);
                return true;
            }

            this.plugin.roleManager.setCurrentRole(player, role.roleId, 1);
            player.sendMessage(this.plugin.serverPrefix + "§eDu wirst nun §f" + this.plugin.roleManager.getRole(1).getRolePrefix() + this.plugin.roleManager.getRole(1).getRoleName() + " §esein");
            return true;
        }

        this.plugin.roleManager.setCurrentRole(player, -1, 1);
        player.sendMessage(this.plugin.serverPrefix + "§eDu wirst nun §f" + this.plugin.roleManager.getRole(1).getRolePrefix() + this.plugin.roleManager.getRole(1).getRoleName() + " §esein");
        return false;
    }
}