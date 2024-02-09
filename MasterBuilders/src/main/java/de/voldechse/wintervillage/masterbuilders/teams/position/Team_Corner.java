package de.voldechse.wintervillage.masterbuilders.teams.position;

public class Team_Corner {

    private final double x, y, z;

    private final String world;

    public Team_Corner(double x, double y, double z, String world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public String getWorld() {
        return world;
    }

    @Override
    public String toString() {
        return "[x=" + x + ",y=" + y + ",z=" + z + ",world=" + world + "]";
    }
}