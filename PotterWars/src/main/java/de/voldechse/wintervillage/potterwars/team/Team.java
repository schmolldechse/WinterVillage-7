package de.voldechse.wintervillage.potterwars.team;

import de.voldechse.wintervillage.potterwars.team.position.TeamPosition;
import org.bukkit.entity.Player;

import java.util.List;

public class Team {

    public final String teamBlock;
    public final int teamId;
    public final String teamName, teamPrefix;
    public List<Player> players;
    public final TeamPosition teamPosition;

    public Team(String teamBlock, int teamId, String teamName, String teamPrefix, List<Player> players, TeamPosition teamPosition) {
        this.teamBlock = teamBlock;
        this.teamId = teamId;
        this.teamName = teamName;
        this.teamPrefix = teamPrefix;
        this.players = players;
        this.teamPosition = teamPosition;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    @Override
    public String toString() {
        return "[teamBlock=" + this.teamBlock + ",teamId=" + this.teamId + ",teamName=" + this.teamName + ",teamPrefix=" + this.teamPrefix + ",teamPosition" + this.teamPosition.toString() + ",players=" + this.players.toString() + "]";
    }
}