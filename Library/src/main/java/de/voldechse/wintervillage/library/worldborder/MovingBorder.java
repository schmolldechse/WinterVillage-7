package de.voldechse.wintervillage.library.worldborder;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Objects;

public class MovingBorder {

    private World world;
    private WorldBorder border;

    private double radius, centerX, centerZ;

    private Plugin plugin;

    public MovingBorder(World world, double centerX, double centerZ, double radius) {
        super();

        this.world = world;
        this.border = world.getWorldBorder();

        this.centerX = centerX;
        this.centerZ = centerZ;

        this.radius = radius;

        this.border.setCenter(this.centerX, this.centerZ);
        this.border.setSize(this.radius);
        this.border.setDamageAmount(0);
        this.border.setDamageBuffer(0);
        this.border.setWarningDistance(0);
        this.border.setWarningTime(0);
    }

    public MovingBorder(World world, Location location, double radius) {
        super();

        this.world = world;
        this.border = world.getWorldBorder();

        this.centerX = location.getX();
        this.centerZ = location.getZ();

        this.radius = radius;

        this.border.setCenter(location);
        this.border.setSize(this.radius);
        this.border.setDamageAmount(0);
        this.border.setDamageBuffer(0);
        this.border.setWarningDistance(0);
        this.border.setWarningTime(0);
    }

    public MovingBorder(World world, WorldBorder worldBorder) {
        super();

        this.world = world;
        this.border = worldBorder;

        this.centerX = worldBorder.getCenter().getX();
        this.centerZ = worldBorder.getCenter().getZ();

        this.radius = worldBorder.getSize();

        this.border.setDamageAmount(0);
        this.border.setDamageBuffer(0);
        this.border.setWarningDistance(0);
        this.border.setWarningTime(0);
    }

    public void move(double newX, double newZ, double newRadius, int time) {
        if (this.plugin == null) {
            Bukkit.getLogger().warning("Cannot move border because plugin is not initialized");
            return;
        }
        BukkitScheduler scheduler = Bukkit.getScheduler();

        int steps = time * 20;

        //if (damageAmount != this.borderDamage) scheduler.runTaskLater(plugin, () -> {
        //    setDamageAmount(damageAmount);
        //}, steps);

        int stepsPerIteration = steps / 5;

        double gapX = 0.0D;
        double gapZ = 0.0D;

        if (newX < this.centerX) gapX = this.centerX - newX;
        else gapX = newX - this.centerX;

        if (newZ < this.centerZ) gapZ = this.centerZ - newZ;
        else gapZ = newZ - this.centerZ;

        double stepsX = (gapX / stepsPerIteration);
        double stepsZ = (gapZ / stepsPerIteration);

        this.border.setSize(newRadius, time);

        for (int i = 1; i <= stepsPerIteration; i++) {
            if (newX < this.centerX) {
                double toX = this.centerX - (i * stepsX);
                scheduler.runTaskLater(this.plugin, () -> {
                    this.border.setCenter(toX, this.border.getCenter().getZ());
                }, i * 5);
            } else {
                double toX = this.centerX + (i * stepsX);
                scheduler.runTaskLater(this.plugin, () -> {
                    this.border.setCenter(toX, this.border.getCenter().getZ());
                }, i * 5);
            }
        }

        for (int i = 1; i <= stepsPerIteration; i++) {
            if (newZ < this.centerZ) {
                double toZ = this.centerZ - (i * stepsZ);
                scheduler.runTaskLater(this.plugin, () -> {
                    this.border.setCenter(this.border.getCenter().getX(), toZ);
                }, i * 5);
            } else {
                double toZ = this.centerZ + (i * stepsZ);
                scheduler.runTaskLater(this.plugin, () -> {
                    this.border.setCenter(this.border.getCenter().getX(), toZ);
                }, i * 5);
            }
        }

        scheduler.runTaskLater(this.plugin, () -> {
            this.centerX = newX;
            this.centerZ = newZ;

            this.border = Objects.requireNonNull(Bukkit.getWorld(world.getName())).getWorldBorder();
        }, steps);
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public void updateBorder(WorldBorder border) {
        this.border = border;
    }

    public void updateCenter(double centerX, double centerZ) {
        this.centerX = centerX;
        this.centerZ = centerZ;
    }

    public World getWorld() {
        return world;
    }

    public WorldBorder getWorldBorder() {
        return border;
    }

    public double getRadius() {
        return radius;
    }

    public double getCenterX() {
        return centerX;
    }

    public double getCenterZ() {
        return centerZ;
    }
}
