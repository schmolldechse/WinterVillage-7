package de.voldechse.wintervillage.ttt.roles;

import de.voldechse.wintervillage.ttt.TTT;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class RoleManager {
    
    private final TTT plugin;

    public List<Role> roleList;
    public Map<Player, Role> assignedRole;

    public RoleManager() {
        this.plugin = InjectionLayer.ext().instance(TTT.class);
        
        this.roleList = new ArrayList<>();
        this.assignedRole = new HashMap<>();
    }

    public void setPlayersInRoles() {
        Collections.shuffle(getUnassignedPlayers());
        List<Player> unassigned = getUnassignedPlayers();

        unassigned.forEach(player -> {
            Collections.shuffle(this.roleList);
            boolean assigned = false;

            for (Role role : this.roleList) {
                int maxPlayers = calculateMaxPlayers(role.getMaxPercentPlayersInRole(), unassigned.size());

                if (role.getPlayers().size() < maxPlayers && !isPlayerAssigned(player)) {
                    role.getPlayers().add(player);
                    assignedRole.put(player, role);
                    assigned = true;
                    break;
                }
            }

            //TODO: elf setzen
            if (!assigned) {
                /**
                player.sendMessage(this.plugin.serverPrefix + "§cEs konnte keine Rolle für dich gefunden werden");
                this.plugin.gameManager.setSpectator(player, true);
                 */
                getRole(0).getPlayers().add(player);
                assignedRole.put(player, getRole(0));
            }
        });
    }

    private List<Player> getUnassignedPlayers() {
        List<Player> unassignedPlayers = new ArrayList<>();
        for (Player player : getPlayerList()) {
            if (isPlayerAssigned(player)) continue;
            unassignedPlayers.add(player);
        }
        return unassignedPlayers;
    }

    public void setCurrentRole(Player player, int oldRoleId, int newRoleId) {
        if (oldRoleId != -1 && isPlayerAssigned(player, oldRoleId)) removeFromRole(player, oldRoleId);

        Role role = getRole(newRoleId);
        this.assignedRole.put(player, role);

        List<Player> playersInRole = role.getPlayers();
        if (!playersInRole.contains(player)) playersInRole.add(player);

        this.assignedRole.get(player).setPlayers(playersInRole);
    }

    public void removeFromRole(Player player, int roleId) {
        List<Player> playersInRole = getRole(roleId).getPlayers();
        if (playersInRole.contains(player)) playersInRole.remove(player);
        this.assignedRole.get(player).setPlayers(playersInRole);
        this.assignedRole.remove(player);
    }

    public boolean isFull(int roleId) {
        if (getRole(roleId) == null) return true;
        return getRole(roleId).getPlayers().size() >= calculateMaxPlayers(getRole(roleId).getMaxPercentPlayersInRole(), getPlayerList().size());
    }

    public boolean isPlayerAssigned(Player player, int roleId) {
        if (getRole(roleId) == null) return false;
        return getRole(roleId).getPlayers().contains(player);
    }

    public boolean isPlayerAssigned(Player player, Role role) {
        if (getRole(role.roleId) == null) return false;
        return isPlayerAssigned(player, role.roleId);
    }

    public boolean isPlayerAssigned(Player player) {
        boolean check = getRole(player) != null;
        if (!check) return false;
        return isPlayerAssigned(player, getRole(player));
    }

    public boolean containsRole(Role role) {
        return roleList.contains(role);
    }

    public Role getRole(int roleId) {
        for (Role role : roleList)
            if (role.roleId == roleId)
                return role;
        return null;
    }

    public Role getRole(Player player) {
        for (Role role : roleList) if (role.getPlayers().contains(player)) return role;
        return null;
    }

    public int getShopPoints(Player player) {
        if (!player.hasMetadata("SHOP_POINTS")) return 0;
        return player.getMetadata("SHOP_POINTS").get(0).asInt();
    }

    public void changeShopPoints(Player player, int toChange) {
        int currentPoints = getShopPoints(player);
        currentPoints += toChange;
        this.plugin.setMetadata(player, "SHOP_POINTS", currentPoints);
    }

    public int calculateMaxPlayers(double percent, int totalPlayers) {
        percent = Math.max(0, Math.min(100, percent));

        double calculatedPlayers = totalPlayers * (percent / 100);
        return (int) Math.round(calculatedPlayers);
    }

    public List<Player> getPlayerList() {
        List<Player> playerList = new ArrayList<Player>();
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (!this.plugin.gameManager.isSpectator(player)) playerList.add(player);
        });
        return playerList;
    }
}