package de.voldechse.wintervillage.ttt.utils.position;

public class PositionBlock {

    private final int x, y, z;

    private final String world, type;

    public PositionBlock(int x, int y, int z, String world, String type) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.type = type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String getWorld() {
        return world;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "[x=" + x + ",y=" + y + ",z=" + z + ",world=" + world + ",type=" + type + "]";
    }
}