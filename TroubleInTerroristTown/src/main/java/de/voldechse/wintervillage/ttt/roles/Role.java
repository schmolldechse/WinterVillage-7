package de.voldechse.wintervillage.ttt.roles;

import org.bukkit.entity.Player;

import java.util.List;

public class Role {

    public final int roleId;
    private final String roleName, rolePrefix, description;
    private List<Player> players;
    private final double maxPercentPlayersInRole;

    public Role(int roleId, String roleName, String rolePrefix, String description, double maxPercentPlayersInRole, List<Player> players) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.rolePrefix = rolePrefix;
        this.description = description;
        this.maxPercentPlayersInRole = maxPercentPlayersInRole;
        this.players = players;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getRolePrefix() {
        return rolePrefix;
    }

    public String getDescription() {
        return description;
    }

    public double getMaxPercentPlayersInRole() {
        return maxPercentPlayersInRole;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    @Override
    public String toString() {
        return "Role{" +
                "roleId=" + roleId +
                ", roleName=" + roleName +
                ", rolePrefix=" + rolePrefix +
                ", description=" + description +
                ", maxPercentPlayersInRole=" + maxPercentPlayersInRole +
                ", players=" + players.toString() +
                "}";
    }
}