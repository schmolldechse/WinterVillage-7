package de.voldechse.wintervillage.masterbuilders.teams.voting;

import org.bukkit.inventory.ItemStack;

public class TemporaryVotepoints {

    private int rawSlot, lastGivenPoints;
    private ItemStack votedWith;

    public TemporaryVotepoints(int rawSlot, int lastGivenPoints, ItemStack votedWith) {
        this.rawSlot = rawSlot;
        this.lastGivenPoints = lastGivenPoints;
        this.votedWith = votedWith;
    }

    public int getRawSlot() {
        return rawSlot;
    }

    public void setRawSlot(int rawSlot) {
        this.rawSlot = rawSlot;
    }

    public int getLastGivenPoints() {
        return lastGivenPoints;
    }

    public void setLastGivenPoints(int lastGivenPoints) {
        this.lastGivenPoints = lastGivenPoints;
    }

    public ItemStack getVotedWith() {
        return votedWith;
    }

    public void setVotedWith(ItemStack votedWith) {
        this.votedWith = votedWith;
    }
}