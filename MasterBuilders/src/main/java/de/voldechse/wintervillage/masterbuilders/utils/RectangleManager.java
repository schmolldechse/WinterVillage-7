package de.voldechse.wintervillage.masterbuilders.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class RectangleManager {

    public static Location calculateCenter(Location cornerA, Location cornerB) {
        Rectangle rectangle = calculateRectangle(cornerA, cornerB);

        double centerX = (rectangle.getMinX() + rectangle.getMaxX()) / 2.0;
        double centerY = (rectangle.getMinY() + rectangle.getMaxY()) / 2.0;
        double centerZ = (rectangle.getMinZ() + rectangle.getMaxZ()) / 2.0;

        return new Location(cornerA.getWorld(), centerX, centerY, centerZ);
    }

    public static void fillRectangle(Location cornerA, Location cornerB, Material blockType) {
        Rectangle rectangle = calculateRectangle(cornerA, cornerB);
        World world = cornerA.getWorld();

        for (int x = (int) rectangle.getMinX(); x <= rectangle.getMaxX(); x++) {
            for (int y = (int) rectangle.getMinY(); y <= rectangle.getMaxY(); y++) {
                for (int z = (int) rectangle.getMinZ(); z <= rectangle.getMaxZ(); z++) {
                    Location location = new Location(world, x, y, z);
                    Block block = location.getBlock();
                    block.setType(blockType);
                }
            }
        }
    }

    public static void fillRectangleBorder(Location cornerA, Location cornerB, Material blockType) {
        Rectangle rectangle = calculateRectangle(cornerA, cornerB);
        World world = cornerA.getWorld();

        for (int x = (int) rectangle.getMinX(); x <= rectangle.getMaxX(); x++) {
            for (int z = (int) rectangle.getMinZ(); z <= rectangle.getMaxZ(); z++) {
                if (x <= rectangle.getMinX() || x >= rectangle.getMaxX() ||
                        z <= rectangle.getMinZ() || z >= rectangle.getMaxZ()) {
                    setBlock(world, x, (int) rectangle.getMinY(), z, blockType);
                }
            }
        }
    }

    public static void setBlock(World world, int x, int y, int z, Material blockType) {
        Location location = new Location(world, x, y, z);
        Block block = location.getBlock();
        block.setType(blockType);
    }

    public static Rectangle calculateRectangle(Location cornerA, Location cornerB) {
        double minX = Math.min(cornerA.getX(), cornerB.getX());
        double minY = Math.min(cornerA.getY(), cornerB.getY());
        double minZ = Math.min(cornerA.getZ(), cornerB.getZ());

        double maxX = Math.max(cornerA.getX(), cornerB.getX());
        double maxY = Math.max(cornerA.getY(), cornerB.getY());
        double maxZ = Math.max(cornerA.getZ(), cornerB.getZ());

        return new Rectangle(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static class Rectangle {
        private final double minX, minY, minZ;
        private final double maxX, maxY, maxZ;

        public Rectangle(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;

            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        public double getMinX() {
            return minX;
        }

        public double getMinY() {
            return minY;
        }

        public double getMinZ() {
            return minZ;
        }

        public double getMaxX() {
            return maxX;
        }

        public double getMaxY() {
            return maxY;
        }

        public double getMaxZ() {
            return maxZ;
        }
    }
}