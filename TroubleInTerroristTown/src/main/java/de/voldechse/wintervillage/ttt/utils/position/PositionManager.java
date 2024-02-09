package de.voldechse.wintervillage.ttt.utils.position;

import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;

import java.nio.file.Paths;
import java.util.*;

public class PositionManager {
    
    private final TTT plugin;

    public List<PositionEntity> spawnPositions;
    public List<PositionBlock> chestPositions;

    public Map<UUID, ArmorStand> ARMORSTANDS_EDITABLE;
    public Map<Location, UUID> CHEST_UUID_MAP;

    public PositionManager() {
        this.plugin = InjectionLayer.ext().instance(TTT.class);
        
        this.spawnPositions = new ArrayList<>();
        this.chestPositions = new ArrayList<>();
        this.ARMORSTANDS_EDITABLE = new HashMap<>();
        this.CHEST_UUID_MAP = new HashMap<>();
    }

    public void addSpawnConfig(Location location) {
        PositionEntity position = new PositionEntity(
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch(),
                location.getWorld().getName()
        );

        String generated = UUID.randomUUID().toString();

        this.plugin.spawnsDocument.append(generated, position);
        this.plugin.spawnsDocument.saveAsConfig(Paths.get(this.plugin.getInstance().getDataFolder().getAbsolutePath(), "spawns.json"));
    }

    public void removeSpawnConfig(UUID uuid) {
        if (!this.plugin.spawnsDocument.contains(uuid.toString())) return;

        this.plugin.spawnsDocument.remove(uuid.toString());
        this.plugin.spawnsDocument.saveAsConfig(Paths.get(this.plugin.getInstance().getDataFolder().getAbsolutePath(), "spawns.json"));

        if (ARMORSTANDS_EDITABLE.containsKey(uuid)) {
            ARMORSTANDS_EDITABLE.get(uuid).remove();
            ARMORSTANDS_EDITABLE.remove(uuid);
        }
    }

    public PositionEntity getPositionFromConfig(UUID uuid) {
        if (!this.plugin.spawnsDocument.contains(uuid.toString())) return null;

        Document POSITION_DOCUMENT = this.plugin.spawnsDocument.getDocument(uuid.toString());
        return new PositionEntity(
                POSITION_DOCUMENT.getDouble("x"),
                POSITION_DOCUMENT.getDouble("y"),
                POSITION_DOCUMENT.getDouble("z"),
                POSITION_DOCUMENT.getFloat("yaw"),
                POSITION_DOCUMENT.getFloat("pitch"),
                POSITION_DOCUMENT.getString("world")
        );
    }

    public boolean isSpawnSaved(UUID uuid) {
        return this.plugin.spawnsDocument.contains(uuid.toString());
    }

    public void readSpawns() {
        for (String uuid : this.plugin.spawnsDocument.keys()) {
            Document spawn = this.plugin.spawnsDocument.getDocument(uuid);
            if (Bukkit.getWorld(spawn.getString("world")) == null) continue;

            double x = spawn.getDouble("x");
            double y = spawn.getDouble("y");
            double z = spawn.getDouble("z");

            float yaw = spawn.getFloat("yaw");
            float pitch = spawn.getFloat("pitch");

            String world = spawn.getString("world");

            this.spawnPositions.add(new PositionEntity(
                    x,
                    y,
                    z,
                    yaw,
                    pitch,
                    world
            ));
        }

        this.plugin.getInstance().getLogger().info("Read " + this.spawnPositions.size() + " spawns from config");
    }

    public void editSpawns() {
        for (String uuid : this.plugin.spawnsDocument.keys()) {
            Document DOCUMENT = this.plugin.spawnsDocument.getDocument(uuid);
            if (Bukkit.getWorld(DOCUMENT.getString("world")) == null) continue;

            double x = DOCUMENT.getDouble("x");
            double y = DOCUMENT.getDouble("y");
            double z = DOCUMENT.getDouble("z");

            float yaw = DOCUMENT.getFloat("yaw");
            float pitch = DOCUMENT.getFloat("pitch");

            String world = DOCUMENT.getString("world");

            Location location = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);

            ArmorStand armorStand = location.getWorld().spawn(location, ArmorStand.class);
            armorStand.setCustomNameVisible(true);
            armorStand.setCustomName("§4§l" + uuid);

            armorStand.setGravity(false);
            armorStand.setAI(false);

            armorStand.setHelmet(new ItemBuilder(Material.PLAYER_HEAD, 1).build());

            armorStand.setHeadPose(new EulerAngle(Math.toRadians(yaw), Math.toRadians(pitch), 0));

            armorStand.setGlowing(true);
            armorStand.setVisible(false);

            this.ARMORSTANDS_EDITABLE.put(UUID.fromString(uuid), armorStand);
        }
    }

    public boolean addChestConfig(Block clickedBlock) {
        PositionBlock position = new PositionBlock(clickedBlock.getLocation().getBlockX(),
                clickedBlock.getLocation().getBlockY(),
                clickedBlock.getLocation().getBlockZ(),
                clickedBlock.getWorld().getName(),
                clickedBlock.getType().toString());
        if (this.CHEST_UUID_MAP.containsKey(position)) return false;

        UUID generated = UUID.randomUUID();

        this.plugin.chestsDocument.append(generated.toString(), position);
        this.plugin.chestsDocument.saveAsConfig(Paths.get(this.plugin.getInstance().getDataFolder().getAbsolutePath(), "chests.json"));

        this.chestPositions.add(position);
        this.CHEST_UUID_MAP.put(
                new Location(
                        clickedBlock.getWorld(),
                        clickedBlock.getLocation().getBlockX(),
                        clickedBlock.getLocation().getBlockY(),
                        clickedBlock.getLocation().getBlockZ()),
                generated);
        return true;
    }

    public boolean removeChestConfig(Block clickedBlock) {
        UUID fromMap = this.CHEST_UUID_MAP.get(clickedBlock.getLocation());

        if (!this.plugin.chestsDocument.contains(fromMap.toString())) return false;

        this.plugin.chestsDocument.remove(fromMap.toString());
        this.plugin.chestsDocument.saveAsConfig(Paths.get(this.plugin.getInstance().getDataFolder().getAbsolutePath(), "chests.json"));

        this.CHEST_UUID_MAP.remove(clickedBlock.getLocation());
        return true;
    }

    public void readChests() {
        for (String uuid : this.plugin.chestsDocument.keys()) {
            Document DOCUMENT = this.plugin.chestsDocument.getDocument(uuid);
            if (Bukkit.getWorld(DOCUMENT.getString("world")) == null) continue;

            int x = DOCUMENT.getInt("x");
            int y = DOCUMENT.getInt("y");
            int z = DOCUMENT.getInt("z");

            String world = DOCUMENT.getString("world");

            String material = DOCUMENT.getString("type");
            if (Material.getMaterial(material) == null)
                this.plugin.getInstance().getLogger().info("Could not find '" + material.toUpperCase() + "' as type");

            PositionBlock position = new PositionBlock(x, y, z, world, material);

            this.chestPositions.add(position);
            this.CHEST_UUID_MAP.put(
                    new Location(
                            Bukkit.getWorld(world),
                            x,
                            y,
                            z),
                    UUID.fromString(uuid));

            Bukkit.getWorld(world).getBlockAt((int) x, (int) y, (int) z).setType(Objects.requireNonNull(Material.getMaterial(material)));
        }

        this.plugin.getInstance().getLogger().info("Read " + this.chestPositions.size() + " chests from config");
    }
}