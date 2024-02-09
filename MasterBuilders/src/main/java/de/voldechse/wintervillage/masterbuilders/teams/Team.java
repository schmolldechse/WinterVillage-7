package de.voldechse.wintervillage.masterbuilders.teams;

import de.voldechse.wintervillage.masterbuilders.teams.position.Team_Corner;
import de.voldechse.wintervillage.masterbuilders.teams.position.Team_Entity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class Team {

    public final int teamId;

    public final Team_Corner cornerA, cornerB;
    public final Team_Entity playerSpawn, villagerSpawn;

    public List<Player> plotOwner;

    public int earnedVotepoints;

    public Team(int teamId, Team_Corner cornerA, Team_Corner cornerB, Team_Entity playerSpawn, Team_Entity villagerSpawn, List<Player> plotOwner, int earnedVotepoints) {
        this.teamId = teamId;
        this.cornerA = cornerA;
        this.cornerB = cornerB;
        this.playerSpawn = playerSpawn;
        this.villagerSpawn = villagerSpawn;
        this.plotOwner = plotOwner;
        this.earnedVotepoints = earnedVotepoints;
    }

    public Location getPlayerSpawn_34ezt() {
        return new Location(Bukkit.getWorld(playerSpawn.getWorld()),
                playerSpawn.getX(),
                playerSpawn.getY(),
                playerSpawn.getZ(),
                playerSpawn.getYaw(),
                playerSpawn.getPitch());
    }

    public void setPlotOwner(List<Player> plotOwner) {
        this.plotOwner = plotOwner;
    }

    public void setEarnedVotepoints(int earnedVotepoints) {
        this.earnedVotepoints = earnedVotepoints;
    }

    @Override
    public String toString() {
        return "Team{" +
                "teamId=" + teamId +
                ", cornerA=" + cornerA +
                ", cornerB=" + cornerB +
                ", playerSpawn=" + playerSpawn +
                ", villagerSpawn=" + villagerSpawn +
                ", plotOwner=" + plotOwner.toString() +
                ", earnedVotepoints=" + earnedVotepoints +
                "}";
    }
}