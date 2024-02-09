package de.voldechse.wintervillage.masterbuilders.teams;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.masterbuilders.MasterBuilders;
import de.voldechse.wintervillage.masterbuilders.teams.position.Team_Corner;
import de.voldechse.wintervillage.masterbuilders.teams.position.Team_Entity;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

public class TeamManager {
    
    private final MasterBuilders plugin;

    private List<Team> teamList;
    public final HashMap<Player, Team> assignedTeams;

    public TeamManager() {
        this.plugin = InjectionLayer.ext().instance(MasterBuilders.class);
        
        this.teamList = new ArrayList<Team>();
        this.assignedTeams = new HashMap<Player, Team>();
    }

    public void addTeam(Team team) {
        if (this.teamList.contains(team)) return;
        this.teamList.add(team);
    }

    public void removeTeam(Team team) {
        removeTeam(team.teamId);
    }

    public void removeTeam(int teamId) {
        if (this.teamList.contains(getTeam(teamId))) this.teamList.remove(getTeam(teamId));
    }

    public Team getTeam(int teamId) {
        for (Team teams : this.teamList)
            if (teams.teamId == teamId) return teams;
        return null;
    }

    public Team getTeam(Player player) {
        if (!assignedTeams.containsKey(player)) return null;
        return assignedTeams.get(player);
    }

    public void setPlayersInTeam() {
        for (Player player : this.plugin.gameManager.getPlayers_start()) {
            if (this.isPlayerInTeam(player)) continue;

            Team team = this.getLowestTeam();
            this.setCurrentTeam(player, -1, team.teamId);

            this.plugin.getInstance().getLogger().info(team.toString() + " assigned to " + player.getName());
        }
    }

    public boolean isFull(Team team) {
        return isFull(team.teamId);
    }

    public boolean isFull(int teamId) {
        if (!containsTeam(teamId)) return false;
        return getTeam(teamId).plotOwner.size() == this.plugin.maxPlayersInTeam;
    }

    public Team getLowestTeam() {
        Team team = null;
        int MAX_PER_TEAM = Integer.MAX_VALUE;
        for (Team teams : this.teamList) {
            if (MAX_PER_TEAM > teams.plotOwner.size()) {
                MAX_PER_TEAM = teams.plotOwner.size();
                team = teams;
            }
        }
        return team;
    }

    public void setCurrentTeam(Player player, int oldTeamId, int newTeamId) {
        if (this.assignedTeams.containsKey(player)) this.removeFromCurrentTeam(player, oldTeamId);

        Team team = getTeam(newTeamId);
        this.assignedTeams.put(player, team);

        List<Player> playersInTeam = team.plotOwner;
        if (!playersInTeam.contains(player)) playersInTeam.add(player);

        this.assignedTeams.get(player).setPlotOwner(playersInTeam);
    }

    public void removeFromCurrentTeam(Player player, int teamId) {
        List<Player> playersInTeam = this.getTeam(teamId).plotOwner;
        if (playersInTeam.contains(player)) playersInTeam.remove(player);
        this.assignedTeams.get(player).setPlotOwner(playersInTeam);
        this.assignedTeams.remove(player);
    }

    public boolean teamInConfig(int toCheck) {
        JsonArray existingTeamId = this.plugin.teamDocument.getArray("teams");
        if (existingTeamId != null) {
            for (JsonElement element : existingTeamId) {
                JsonObject teamObject = element.getAsJsonObject();
                int teamId = teamObject.get("teamId").getAsInt();
                if (teamId == toCheck) return true;
            }
        }
        return false;
    }

    public Inventory getTeamSelectInventory() {
        int inventorySize = (this.getTeamList().size() / 9 + 1) * 9;
        Inventory inventory = Bukkit.createInventory((InventoryHolder) null, inventorySize, "§aTeamauswahl");

        for (Team team : this.teamList) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Player players : team.plotOwner) {
                if (stringBuilder.length() > 0) stringBuilder.append("§8, ");
                stringBuilder.append("§c" + players.getName());
            }

            if (team.plotOwner.isEmpty()) {
                inventory.addItem(new ItemBuilder(Material.PAPER, 1, "§7Team §r" + team.teamId + " §7(" + team.plotOwner.size() + "/" + this.plugin.maxPlayersInTeam + ")").build());
            } else {
                inventory.addItem(new ItemBuilder(Material.PAPER, 1, "§7Team §r" + team.teamId + " §7(" + team.plotOwner.size() + "/" + this.plugin.maxPlayersInTeam + ")").lore(
                        "",
                        "§8» " + stringBuilder.toString(),
                        ""
                ).build());
            }
        }
        return inventory;
    }

    public void sortByPoints(List<Team> teamList) {
        teamList.sort((team1, team2) -> Integer.compare(team2.earnedVotepoints, team1.earnedVotepoints));
    }

    public int calculateTeamRanking(Team team) {
        int ranking = 1;
        for (Team teams : getTeamList()) {
            if (teams.equals(team)) return ranking;
            ranking++;
        }
        return -1;
    }

    public boolean isPlayerInTeam(Player player, int teamId) {
        return isPlayerInTeam(player) && this.assignedTeams.get(player).teamId == teamId;
    }

    public boolean isPlayerInTeam(Player player, Team team) {
        return isPlayerInTeam(player, team.teamId);
    }

    public boolean isPlayerInTeam(Player player) {
        return this.assignedTeams.containsKey(player);
    }

    public boolean containsTeam(int teamId) {
        return getTeamIds().contains(teamId);
    }

    public int getTeamId(Player player) {
        if (!isPlayerInTeam(player)) return -1;
        return getAssignedPlayerTeams().get(player).teamId;
    }

    public Set<Integer> getTeamIds() {
        Set<Integer> teamIds = new HashSet<Integer>();
        getTeamList().forEach(team -> teamIds.add(team.teamId));
        return teamIds;
    }

    public void loadFromConfig() {
        JsonArray teamsArray = this.plugin.teamDocument.getArray("teams");

        teamsArray.forEach(teamElement -> {
            Document document = new Document((JsonObject) teamElement.getAsJsonObject());

            int teamId = document.getInt("teamId");

            Document cornerADocument = document.getDocument("cornerA");
            Team_Corner cornerA = new Team_Corner(
                    cornerADocument.getDouble("x"),
                    cornerADocument.getDouble("y"),
                    cornerADocument.getDouble("z"),
                    cornerADocument.getString("world")
            );

            Document cornerBDocument = document.getDocument("cornerB");
            Team_Corner cornerB = new Team_Corner(
                    cornerBDocument.getDouble("x"),
                    cornerBDocument.getDouble("y"),
                    cornerBDocument.getDouble("z"),
                    cornerBDocument.getString("world")
            );

            Document playerSpawnDocument = document.getDocument("playerSpawn");
            Team_Entity playerSpawn = new Team_Entity(
                    playerSpawnDocument.getDouble("x"),
                    playerSpawnDocument.getDouble("y"),
                    playerSpawnDocument.getDouble("z"),
                    playerSpawnDocument.getFloat("yaw"),
                    playerSpawnDocument.getFloat("pitch"),
                    playerSpawnDocument.getString("world")
            );

            Document villagerSpawnDocument = document.getDocument("villagerSpawn");
            Team_Entity villagerSpawn = new Team_Entity(
                    villagerSpawnDocument.getDouble("x"),
                    villagerSpawnDocument.getDouble("y"),
                    villagerSpawnDocument.getDouble("z"),
                    villagerSpawnDocument.getFloat("yaw"),
                    villagerSpawnDocument.getFloat("pitch"),
                    villagerSpawnDocument.getString("world")
            );

            this.teamList.add(new Team(teamId, cornerA, cornerB, playerSpawn, villagerSpawn, new ArrayList<Player>(), 0));
        });

        this.teamList.sort(Comparator.comparing(team -> team.teamId));

        this.plugin.getInstance().getLogger().info("The following teams were created:");
        for (Team team : this.teamList) this.plugin.getInstance().getLogger().info(team.toString());
    }

    public void saveTeamsToConfig(List<Team> teams) {
        Set<Integer> existingTeamIds = getTeamIds();

        JsonArray existingTeamsArray = this.plugin.teamDocument.getArray("teams");

        File file = new File(this.plugin.getInstance().getDataFolder() + "\\teams.json");
        file.delete();

        JsonArray newTeamsArray = new JsonArray();

        Team_Corner testCorner = new Team_Corner(0.0, 0.0, 0.0, "world");
        Team_Entity entitySpawn = new Team_Entity(0.0, 0.0, 0.0, 0.0F, 0.0F, "world");

        Document cornerData = new Document("x", testCorner.getX())
                .append("y", testCorner.getY())
                .append("z", testCorner.getZ())
                .append("world", "world");
        Document entityData = new Document("x", entitySpawn.getX())
                .append("y", entitySpawn.getY())
                .append("z", entitySpawn.getZ())
                .append("yaw", entitySpawn.getYaw())
                .append("pitch", entitySpawn.getPitch())
                .append("world", "world");

        teams.forEach(newTeam -> {
            System.out.println(existingTeamIds.contains(newTeam.teamId));
            if (!existingTeamIds.contains(newTeam.teamId)) {

                Document newTeamDocument = new Document("teamId", newTeam.teamId)
                        .append("cornerA", cornerData)
                        .append("cornerB", cornerData)
                        .append("playerSpawn", entityData)
                        .append("villagerSpawn", entityData);

                newTeamsArray.add(newTeamDocument.obj());
                existingTeamIds.add(newTeam.teamId);
            }
        });

        JsonArray mergedTeamsArray = existingTeamIds != null ? existingTeamsArray : new JsonArray();
        mergedTeamsArray.addAll(newTeamsArray);

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("teams", mergedTeamsArray);
        new Document(jsonObject).saveAsConfig(Paths.get(this.plugin.getInstance().getDataFolder().getAbsolutePath(), "teams.json"));

        this.plugin.reloadTeams();
    }

    public void update(int teamId, String option, Location location) {
        JsonArray existingTeamsArray = this.plugin.teamDocument.getArray("teams");
        Document optionDocument = new Document();

        File file = new File(this.plugin.getInstance().getDataFolder() + "\\teams.json");
        file.delete();

        switch (option) {
            case "CORNER_A" -> {
                optionDocument.append("x", location.getBlock().getRelative(BlockFace.DOWN).getX())
                        .append("y", location.getBlock().getRelative(BlockFace.DOWN).getY())
                        .append("z", location.getBlock().getRelative(BlockFace.DOWN).getZ())
                        .append("world", location.getWorld().getName());
                option = "cornerA";
            }
            case "CORNER_B" -> {
                optionDocument.append("x", location.getBlock().getRelative(BlockFace.DOWN).getX())
                        .append("y", location.getBlock().getRelative(BlockFace.DOWN).getY())
                        .append("z", location.getBlock().getRelative(BlockFace.DOWN).getZ())
                        .append("world", location.getWorld().getName());
                option = "cornerB";
            }
            case "PLAYER_SPAWN" -> {
                optionDocument.append("x", location.getX())
                        .append("y", location.getY())
                        .append("z", location.getZ())
                        .append("yaw", location.getYaw())
                        .append("pitch", location.getPitch())
                        .append("world", location.getWorld().getName());
                option = "playerSpawn";
            }
            case "VILLAGER_SPAWN" -> {
                optionDocument.append("x", location.getX())
                        .append("y", location.getY())
                        .append("z", location.getZ())
                        .append("yaw", location.getYaw())
                        .append("pitch", location.getPitch())
                        .append("world", location.getWorld().getName());
                option = "villagerSpawn";
            }
            default -> {
            }
        }

        for (JsonElement teamElement : existingTeamsArray) {
            JsonObject teamObject = teamElement.getAsJsonObject();
            int currentTeamId = teamObject.get("teamId").getAsInt();

            if (currentTeamId == teamId) {
                teamObject.add(option, optionDocument.obj());
                break;
            }
        }

        new Document("teams", existingTeamsArray).saveAsConfig(Paths.get(this.plugin.getInstance().getDataFolder().getAbsolutePath(), "teams.json"));

        this.plugin.reloadTeams();
    }

    public boolean isBlockInPlot(int teamId, Location blockLocation) {
        JsonObject plotObject = getPlotById(teamId);
        if (plotObject == null) return false;

        JsonObject cornerA = plotObject.getAsJsonObject("cornerA");
        JsonObject cornerB = plotObject.getAsJsonObject("cornerB");

        double minX = Math.min(cornerA.get("x").getAsDouble(), cornerB.get("x").getAsDouble());
        double minY = Math.min(cornerA.get("y").getAsDouble(), cornerB.get("y").getAsDouble()) + 1;
        double minZ = Math.min(cornerA.get("z").getAsDouble(), cornerB.get("z").getAsDouble());
        double maxX = Math.max(cornerA.get("x").getAsDouble(), cornerB.get("x").getAsDouble());
        double maxY = Math.max(cornerA.get("y").getAsDouble(), cornerB.get("y").getAsDouble()) + 1;
        double maxZ = Math.max(cornerA.get("z").getAsDouble(), cornerB.get("z").getAsDouble());

        double blockX = blockLocation.getX();
        double blockY = blockLocation.getY();
        double blockZ = blockLocation.getZ();

        boolean isInsideX = blockX >= minX && blockX <= maxX;
        boolean isInsideY = blockY >= minY && blockY <= maxY;
        boolean isInsideZ = blockZ >= minZ && blockZ <= maxZ;

        //double minHeightY = Math.min(cornerA.get("y").getAsDouble(), cornerB.get("y").getAsDouble()) + 1;
        //double maxHeightY = Math.max(cornerA.get("y").getAsDouble(), cornerB.get("y").getAsDouble()) + 1;

        //boolean isWithinAllowedHeightDifference = blockY >= minHeightY && blockY <= maxHeightY + this.plugin.configDocument.getInt("allowedHeightDifference");
        boolean isWithinAllowedHeightDifference = blockY >= minY && blockY <= maxY + this.plugin.allowedHeightDifference;

        return isInsideX && (isInsideY || isWithinAllowedHeightDifference) && isInsideZ;
    }

    private JsonObject getPlotById(int plotId) {
        JsonArray plotsArray = this.plugin.teamDocument.getArray("teams");
        for (JsonElement element : plotsArray) {
            JsonObject plotObject = element.getAsJsonObject();
            int currentPlotId = plotObject.get("teamId").getAsInt();
            if (currentPlotId == plotId)
                return plotObject;
        }
        return null;
    }

    public List<Team> getTeamList() {
        return teamList;
    }

    public HashMap<Player, Team> getAssignedPlayerTeams() {
        return assignedTeams;
    }

    public List<Player> getPlayerTeamList() {
        List<Player> playerList = new ArrayList<Player>();
        getAssignedPlayerTeams().keySet().forEach(player -> {
            if (!this.plugin.gameManager.isSpectator(player)) playerList.add(player);
        });
        return playerList;
    }
}