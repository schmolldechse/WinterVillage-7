package de.voldechse.wintervillage.potterwars.team;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.team.position.TeamPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

public class TeamManager {

    public final List<Team> teamList;

    public TeamManager() {
        this.teamList = new ArrayList<Team>();
    }

    public void removeTeam(Team team) {
        if (this.teamList.contains(team)) this.teamList.remove(team);
    }

    public void removeTeam(int teamId) {
        if (this.teamList.contains(this.getTeam(teamId))) this.teamList.remove(this.getTeam(teamId));
    }

    public Team getTeam(int teamId) {
        for (Team teams : this.teamList) if (teams.teamId == teamId) return teams;
        return null;
    }

    public Team getTeam(Player player) {
        for (Team teams : this.teamList) if (teams.players.contains(player)) return teams;
        return null;
    }

    public List<Team> getTeamsFromConfig() {
        List<Team> teams = new ArrayList<>();

        JsonArray teamsArray = PotterWars.getInstance().teamsDocument.getArray("teams");
        for (JsonElement jsonElement : teamsArray.asList()) {
            Document document = new Document(jsonElement.getAsJsonObject());

            if (!document.contains("teamId")
                    || !document.contains("teamBlock")
                    || !document.contains("teamName")
                    || !document.contains("teamPrefix")
                    || !document.contains("playerSpawn")) continue;

            String blockName = document.getString("teamBlock");
            int teamId_config = document.getInt("teamId");
            String teamName = document.getString("teamName");
            String teamPrefix = document.getString("teamPrefix");

            Document PLAYER_SPAWN_DOCUMENT = document.getDocument("playerSpawn");
            TeamPosition playerSpawn = new TeamPosition(
                    PLAYER_SPAWN_DOCUMENT.getDouble("x"),
                    PLAYER_SPAWN_DOCUMENT.getDouble("y"),
                    PLAYER_SPAWN_DOCUMENT.getDouble("z"),
                    PLAYER_SPAWN_DOCUMENT.getFloat("yaw"),
                    PLAYER_SPAWN_DOCUMENT.getFloat("pitch"),
                    PLAYER_SPAWN_DOCUMENT.getString("world")
            );

            teams.add(new Team(blockName, teamId_config, teamName, teamPrefix, new ArrayList<>(), playerSpawn));
        }

        return teams;
    }

    public Team getTeamFromConfig(int teamId) {
        if (!teamInConfig(teamId)) return null;

        Team team = null;

        JsonArray teamsArray = PotterWars.getInstance().teamsDocument.getArray("teams");
        for (JsonElement jsonElement : teamsArray.asList()) {
            Document document = new Document(jsonElement.getAsJsonObject());
            if (document.getInt("teamId") != teamId) continue;

            String blockName = document.getString("teamBlock");
            int teamId_config = document.getInt("teamId");
            String teamName = document.getString("teamName");
            String teamPrefix = document.getString("teamPrefix");

            Document PLAYER_SPAWN_DOCUMENT = document.getDocument("playerSpawn");
            TeamPosition playerSpawn = new TeamPosition(
                    PLAYER_SPAWN_DOCUMENT.getDouble("x"),
                    PLAYER_SPAWN_DOCUMENT.getDouble("y"),
                    PLAYER_SPAWN_DOCUMENT.getDouble("z"),
                    PLAYER_SPAWN_DOCUMENT.getFloat("yaw"),
                    PLAYER_SPAWN_DOCUMENT.getFloat("pitch"),
                    PLAYER_SPAWN_DOCUMENT.getString("world")
            );

            team = new Team(blockName, teamId_config, teamName, teamPrefix, new ArrayList<>(), playerSpawn);
        }
        return team;
    }

    public void setPlayersInTeam() {
        for (Player player : PotterWars.getInstance().gameManager.getLivingPlayers()) {
            if (this.isPlayerInTeam(player)) continue;

            Team team = this.getLowestTeam();
            this.setCurrentTeam(player, team.getTeamId());
            player.sendMessage(PotterWars.getInstance().serverPrefix + "§eDa du dir kein Team ausgewählt hast, wurdest du in das Team §r" + team.teamPrefix + team.teamName + " §egesetzt");
        }
    }

    public boolean isFull(Team team) {
        return isFull(team.getTeamId());
        /**
         if (!containsTeam(team.getTeamId())) return false;
         return team.getPlayersInTeam() == team.getMaxPlayersInTeam();
         */
    }

    public boolean isFull(int teamId) {
        if (!teamAlive(getTeam(teamId))) return false;
        return getTeam(teamId).players.size() == PotterWars.getInstance().maxPlayersInTeam;
    }

    public Team getLowestTeam() {
        Team team = null;
        int teamSize = Integer.MAX_VALUE;
        for (Team teams : this.teamList) {
            if (teamSize > teams.players.size()) {
                teamSize = teams.players.size();
                team = teams;
            }
        }
        return team;
    }

    public void setCurrentTeam(Player player, int newTeamId) {
        if (getTeam(player) != null)
            this.removeFromCurrentTeam(player, getTeam(player).teamId);

        Team team = this.getTeam(newTeamId);

        List<Player> playersInTeam = team.players;
        if (!playersInTeam.contains(player)) {
            playersInTeam.add(player);
        }

        team.setPlayers(playersInTeam);
    }

    public void removeFromCurrentTeam(Player player, int teamId) {
        Team team = getTeam(teamId);
        if (team == null) return;

        List<Player> playersInTeam = team.players;
        if (playersInTeam.contains(player))
            playersInTeam.remove(player);

        team.setPlayers(playersInTeam);
    }

    public boolean teamInConfig(int toCheck) {
        JsonArray existingTeamId = PotterWars.getInstance().teamsDocument.getArray("teams");
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
        Inventory inventory = Bukkit.createInventory(null, inventorySize, "§aTeamauswahl");

        for (Team team : this.teamList) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Player players : team.players) {
                if (!stringBuilder.isEmpty())
                    stringBuilder.append("§8, ");
                stringBuilder.append(team.teamPrefix).append(players.getName());
            }

            Material material = null;
            if (Material.getMaterial(team.teamBlock) == null) material = Material.OAK_LOG;
            else material = Material.getMaterial(team.teamBlock);

            if (team.players.isEmpty()) {
                inventory.addItem(new ItemBuilder(material, 1, "§7Team §r" + team.teamPrefix + team.teamName + " §7(" + team.players.size() + "/" + PotterWars.getInstance().maxPlayersInTeam + ")").build());
            } else {
                inventory.addItem(new ItemBuilder(material, 1, "§7Team §r" + team.teamPrefix + team.teamName + " §7(" + team.players.size() + "/" + PotterWars.getInstance().maxPlayersInTeam + ")").lore(
                        "",
                        "§8» " + stringBuilder.toString(),
                        ""
                ).build());
            }
        }
        return inventory;
    }

    public boolean isPlayerInTeam(Player player, int teamId) {
        if (getTeam(teamId) == null) return false;
        return getTeam(teamId).players.contains(player);
    }

    public boolean isPlayerInTeam(Player player, Team team) {
        if (getTeam(team.teamId) == null) return false;
        return isPlayerInTeam(player, team.teamId);
    }

    public boolean isPlayerInTeam(Player player) {
        boolean check = getTeam(player) != null;
        if (!check) return false;
        return isPlayerInTeam(player, getTeam(player));
    }

    public boolean teamAlive(Team team) {
        return this.teamList.contains(team);
    }

    public void loadFromConfig() {
        JsonArray teamsArray = PotterWars.getInstance().teamsDocument.getArray("teams");

        teamsArray.forEach(teamElement -> {
            Document document = new Document((JsonObject) teamElement.getAsJsonObject());

            Document playerSpawnDocument = document.getDocument("playerSpawn");
            TeamPosition playerSpawn = new TeamPosition(
                    playerSpawnDocument.getDouble("x"),
                    playerSpawnDocument.getDouble("y"),
                    playerSpawnDocument.getDouble("z"),
                    playerSpawnDocument.getFloat("yaw"),
                    playerSpawnDocument.getFloat("pitch"),
                    playerSpawnDocument.getString("world")
            );

            String blockName = document.getString("teamBlock");
            int teamId = document.getInt("teamId");
            String teamName = document.getString("teamName");
            String teamPrefix = document.getString("teamPrefix");

            this.teamList.add(new Team(blockName, teamId, teamName, teamPrefix, new ArrayList<Player>(), playerSpawn));
        });

        this.teamList.sort(Comparator.comparing(Team::getTeamId));

        Bukkit.getLogger().info("Es wurden folgende Teams erstellt");
        for (Team team : this.teamList) System.out.println(team.toString());
    }

    public void saveTeamsToConfig(List<Team> teams) {
        Set<Integer> existingTeamIds = getTeamIds();
        JsonArray existingTeamsArray = PotterWars.getInstance().teamsDocument.getArray("teams");

        File file = new File(PotterWars.getInstance().getDataFolder().getAbsolutePath(), "teams.json");
        file.delete();

        JsonArray newTeamsArray = new JsonArray();

        TeamPosition playerSpawn = new TeamPosition(0.0, 0.0, 0.0, 0.0f, 0.0f, "world");
        Document playerSpawnDocument = new Document("x", playerSpawn.getX())
                .append("y", playerSpawn.getY())
                .append("z", playerSpawn.getZ())
                .append("yaw", playerSpawn.getYaw())
                .append("pitch", playerSpawn.getPitch())
                .append("world", playerSpawn.getWorld());

        teams.forEach(newTeam -> {
            if (!existingTeamIds.contains(newTeam.getTeamId())) {
                Document newTeamDocument = new Document("teamBlock", "WHITE_WOOL_BLOCK")
                        .append("teamId", newTeam.getTeamId())
                        .append("teamName", "TEAM_" + newTeam.getTeamId())
                        .append("teamPrefix", "§f")
                        .append("playerSpawn", playerSpawnDocument);

                newTeamsArray.add(newTeamDocument.obj());
                existingTeamIds.add(newTeam.getTeamId());
            }
        });

        JsonArray mergedTeamsArray = existingTeamIds != null ? existingTeamsArray : new JsonArray();
        mergedTeamsArray.addAll(newTeamsArray);

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("teams", mergedTeamsArray);
        new Document(jsonObject).saveAsConfig(PotterWars.getInstance().getDataFolder() + "\\teams.json");

        reloadDocument();
    }

    public void updateLocation(int teamId, Location location) {
        JsonArray existingTeamsArray = PotterWars.getInstance().teamsDocument.getArray("teams");
        Document optionDocument = new Document();

        File file = new File(PotterWars.getInstance().getDataFolder().getAbsolutePath(), "teams.json");
        file.delete();

        optionDocument.append("x", location.getBlock().getRelative(BlockFace.DOWN).getX())
                .append("y", location.getBlock().getRelative(BlockFace.DOWN).getY())
                .append("z", location.getBlock().getRelative(BlockFace.DOWN).getZ())
                .append("yaw", location.getYaw())
                .append("pitch", location.getPitch())
                .append("world", location.getWorld().getName());

        for (JsonElement teamElement : existingTeamsArray) {
            JsonObject teamObject = teamElement.getAsJsonObject();
            int currentTeamId = teamObject.get("teamId").getAsInt();

            if (currentTeamId == teamId) {
                teamObject.add("playerSpawn", optionDocument.obj());
                break;
            }
        }

        new Document("teams", existingTeamsArray)
                .saveAsConfig(Paths.get(PotterWars.getInstance().getDataFolder().getAbsolutePath(), "teams.json"));
        reloadDocument();
    }

    public void updateString(int teamId, String option, String toChange) {
        JsonArray existingTeamsArray = PotterWars.getInstance().teamsDocument.getArray("teams");
        //Document optionDocument = new Document();

        File file = new File(PotterWars.getInstance().getDataFolder().getAbsolutePath(), "teams.json");
        file.delete();

        switch (option) {
            case "TEAM_BLOCK" -> {
                //optionDocument.append("teamBlock", toChange);
                option = "teamBlock";
            }
            case "TEAM_NAME" -> {
                //optionDocument.append("teamName", toChange);
                option = "teamName";
            }
            case "TEAM_PREFIX" -> {
                //optionDocument.append("teamPrefix", toChange);
                option = "teamPrefix";
                toChange = toChange.replace("&", "§");
            }
            default -> {

            }
        }

        for (JsonElement teamElement : existingTeamsArray) {
            JsonObject teamObject = teamElement.getAsJsonObject();
            int currentTeamId = teamObject.get("teamId").getAsInt();

            if (currentTeamId == teamId) {
                teamObject.addProperty(option, toChange);
                break;
            }
        }

        new Document("teams", existingTeamsArray)
                .saveAsConfig(Paths.get(PotterWars.getInstance().getDataFolder().getAbsolutePath(), "teams.json"));
        reloadDocument();
    }

    public void reloadDocument() {
        PotterWars.getInstance().teamsDocument = Document.loadDocument(Paths.get(PotterWars.getInstance().getDataFolder().getAbsolutePath(), "teams.json"));
        this.teamList.clear();
        this.loadFromConfig();
    }

    public Set<Integer> getTeamIds() {
        Set<Integer> teamIds = new HashSet<Integer>();
        getTeamList().forEach(team -> teamIds.add(team.teamId));
        return teamIds;
    }

    public List<Team> getTeamList() {
        return this.teamList;
    }
}