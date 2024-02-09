package de.voldechse.wintervillage.ttt.utils.position;

public class PositionEntity {

    private final double x, y, z;

    private final float yaw, pitch;

    private final String world;

    public PositionEntity(double x, double y, double z, float yaw, float pitch, String world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
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

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public String getWorld() {
        return world;
    }

    @Override
    public String toString() {
        return "[x=" + x + ",y=" + y + ",z=" + z + ",yaw=" + yaw + ",pitch=" + pitch + ",world=" + world + "]";
    }
}