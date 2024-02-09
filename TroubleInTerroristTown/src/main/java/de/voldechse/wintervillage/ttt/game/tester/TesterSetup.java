package de.voldechse.wintervillage.ttt.game.tester;

import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.ttt.TTT;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

import java.io.File;
import java.nio.file.Paths;

public class TesterSetup {
    
    private final TTT plugin;

    public Location[] testerLamps, barrier;
    public Location cornerA, cornerB, playerSpawn, outsideTester, activationButton, traitorTrap;
    public Material idling, tested, busted, barrierMaterial, floorMaterial;

    public TesterSetup(
            Location[] lamps,
            Location[] barrier,
            Location cornerA, 
            Location cornerB, 
            Location playerSpawn,
            Location outsideTester, 
            Location activationButton, 
            Location traitorTrap,
            Material idling, 
            Material tested, 
            Material busted,
            Material barrierMaterial, 
            Material floorMaterial
    ) {
        this.plugin = InjectionLayer.ext().instance(TTT.class);
        
        this.testerLamps = lamps;
        this.barrier = barrier;
        this.cornerA = cornerA;
        this.cornerB = cornerB;
        this.playerSpawn = playerSpawn;
        this.outsideTester = outsideTester;
        this.activationButton = activationButton;
        this.traitorTrap = traitorTrap;

        this.idling = idling;
        this.tested = tested;
        this.busted = busted;
        this.barrierMaterial = barrierMaterial;
        this.floorMaterial = floorMaterial;
    }

    public void update(String option, Location location) {
        Document document = new Document();

        File file = new File(this.plugin.getInstance().getDataFolder().getAbsolutePath(), "tester.json");
        file.delete();

        switch (option) {
            case "CORNER_A" -> {
                document.append("x", location.getBlock().getRelative(BlockFace.DOWN).getX())
                        .append("y", location.getBlock().getRelative(BlockFace.DOWN).getY())
                        .append("z", location.getBlock().getRelative(BlockFace.DOWN).getZ())
                        .append("world", location.getWorld().getName());
                option = "cornerA";
            }
            case "CORNER_B" -> {
                document.append("x", location.getBlock().getRelative(BlockFace.DOWN).getX())
                        .append("y", location.getBlock().getRelative(BlockFace.DOWN).getY())
                        .append("z", location.getBlock().getRelative(BlockFace.DOWN).getZ())
                        .append("world", location.getWorld().getName());
                option = "cornerB";
            }
            case "LEFT_LAMP" -> {
                document.append("x", location.getBlock().getX())
                        .append("y", location.getBlock().getY())
                        .append("z", location.getBlock().getZ())
                        .append("world", location.getWorld().getName());
                option = "leftLamp";
            }
            case "RIGHT_LAMP" -> {
                document.append("x", location.getBlock().getX())
                        .append("y", location.getBlock().getY())
                        .append("z", location.getBlock().getZ())
                        .append("world", location.getWorld().getName());
                option = "rightLamp";
            }
            case "BARRIER_1" -> {
                document.append("x", location.getBlock().getX())
                        .append("y", location.getBlock().getY())
                        .append("z", location.getBlock().getZ())
                        .append("world", location.getWorld().getName());
                option = "barrier_1";
            }
            case "BARRIER_2" -> {
                document.append("x", location.getBlock().getX())
                        .append("y", location.getBlock().getY())
                        .append("z", location.getBlock().getZ())
                        .append("world", location.getWorld().getName());
                option = "barrier_2";
            }
            case "BARRIER_3" -> {
                document.append("x", location.getBlock().getX())
                        .append("y", location.getBlock().getY())
                        .append("z", location.getBlock().getZ())
                        .append("world", location.getWorld().getName());
                option = "barrier_3";
            }
            case "ACTIVATION_BUTTON" -> {
                document.append("x", location.getX())
                        .append("y", location.getY())
                        .append("z", location.getZ())
                        .append("world", location.getWorld().getName());
                option = "activationButton";
            }
            case "TRAITOR_TRAP" -> {
                document.append("x", location.getX())
                        .append("y", location.getY())
                        .append("z", location.getZ())
                        .append("world", location.getWorld().getName());
                option = "traitorTrap";
            }
            case "PLAYER_SPAWN" -> {
                document.append("x", location.getX())
                        .append("y", location.getY())
                        .append("z", location.getZ())
                        .append("yaw", location.getYaw())
                        .append("pitch", location.getPitch())
                        .append("world", location.getWorld().getName());
                option = "playerSpawn";
            }
            case "OUTSIDE_TESTER" -> {
                document.append("x", location.getX())
                        .append("y", location.getY())
                        .append("z", location.getZ())
                        .append("yaw", location.getYaw())
                        .append("pitch", location.getPitch())
                        .append("world", location.getWorld().getName());
                option = "outsideTester";
            }
            default -> {
            }
        }

        this.plugin.testerDocument.remove(option);
        this.plugin.testerDocument.append(option, document)
                .saveAsConfig(Paths.get(this.plugin.getInstance().getDataFolder().getAbsolutePath(), "tester.json"));
    }

    @Override
    public String toString() {
        return "Tester{" +
                "leftLamp=" + testerLamps[0] +
                ",rightLamp=" + testerLamps[1] +
                ",barrier_1=" + barrier[0] +
                ",barrier_2=" + barrier[1] +
                ",barrier_3=" + barrier[2] +
                ",cornerA=" + cornerA +
                ",cornerB=" + cornerB +
                ",playerSpawn=" + playerSpawn +
                ",outsideTester=" + outsideTester +
                ",activationButton=" + activationButton +
                ",traitorTrap=" + traitorTrap +
                ",IDLING_MATERIAL=" + idling +
                ",TESTED_MATERIAL=" + tested +
                ",BUSTED_MATERIAL=" + busted +
                ",BARRIER_MATERIAL=" + barrierMaterial +
                ",FLOOR_MATERIAL=" + floorMaterial +
                "}";
    }
}