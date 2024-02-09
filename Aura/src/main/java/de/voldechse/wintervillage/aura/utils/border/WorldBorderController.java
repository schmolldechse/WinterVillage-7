package de.voldechse.wintervillage.aura.utils.border;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.voldechse.wintervillage.aura.Aura;
import de.voldechse.wintervillage.library.worldborder.MovingBorder;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class WorldBorderController {

    private final Aura plugin;

    private final List<BorderPhase> borderPhases;
    private int currentPhaseIndex;
    private BukkitTask completePhaseTask, announceNextPhaseTask, announceTimerTask, updatingTimerTask;

    private World world;
    private MovingBorder movingBorder;

    public boolean WAITING_FOR_NEXT_PHASE;

    public WorldBorderController() {
        this.plugin = InjectionLayer.ext().instance(Aura.class);

        this.borderPhases = new ArrayList<>();

        this.loadFromConfig();
        if (this.borderPhases.isEmpty()) {
            this.plugin.getInstance().getLogger().severe("Could not find any border phases");
            return;
        }
        this.currentPhaseIndex = 0;

        WorldBorder worldBorder = this.world.getWorldBorder();
        worldBorder.setCenter(this.borderPhases.get(0).getCenter().getX(), this.borderPhases.get(0).center.z);
        worldBorder.setSize(this.borderPhases.get(0).oldSize);

        this.movingBorder = new MovingBorder(this.world, this.borderPhases.get(0).getCenter().getX(), this.borderPhases.get(0).getCenter().getZ(), this.borderPhases.get(0).getOldSize());
        this.movingBorder.setPlugin(this.plugin.getInstance());
        this.movingBorder.getWorldBorder().setDamageBuffer(5);
        this.movingBorder.getWorldBorder().setDamageAmount(0.1);
        this.movingBorder.getWorldBorder().setWarningTime(15);
        this.movingBorder.getWorldBorder().setWarningDistance(5);

        this.WAITING_FOR_NEXT_PHASE = false;
    }

    public void start() {
        if (this.currentPhaseIndex >= 0 && this.currentPhaseIndex < this.borderPhases.size()) {
            BorderPhase currentPhase = current();

            double x;
            double z;

            if (currentPhase.center.randomCenter) {
                Location random = randomLocationInBorder(this.world);

                x = random.getX();
                z = random.getZ();
            } else {
                x = currentPhase.getCenter().getX();
                z = currentPhase.getCenter().getZ();
            }

            this.movingBorder.move(x, z, currentPhase.getNewSize(), currentPhase.timeShrinking);

            startBufferTimer(currentPhase);

            plugin.getInstance().getLogger().info("Start moving border from [oldRadius=" + currentPhase.oldSize + "] to [newRadius=" + currentPhase.newSize + "] for " + currentPhase.timeShrinking + " SECONDS [+" + currentPhase.pufferForNextBorder + " PUFFER]");
            Bukkit.broadcastMessage(plugin.serverPrefix + "§eDie Barriere schrumpft");
            plugin.gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f);
        }
    }

    public void setCurrentPhase(int phaseId) {
        if (phaseId >= 0 && phaseId < borderPhases.size()) {
            currentPhaseIndex = phaseId;
        }
    }

    public void next() {
        if (this.currentPhaseIndex + 1 < this.borderPhases.size()) {
            this.currentPhaseIndex++;
        }
    }

    public BorderPhase nextPhase() {
        int nextPhaseIndex = currentPhaseIndex + 1;
        if (nextPhaseIndex >= 0 && nextPhaseIndex < this.borderPhases.size())
            return borderPhases.get(nextPhaseIndex);
        return null;
    }

    public void last() {
        int lastIndex = this.borderPhases.size() - 1;
        if (lastIndex >= 0) currentPhaseIndex = lastIndex;
    }

    public BorderPhase current() {
        if (this.currentPhaseIndex >= 0 && this.currentPhaseIndex < this.borderPhases.size())
            return this.borderPhases.get(currentPhaseIndex);
        return null;
    }

    public boolean isLast() {
        return this.currentPhaseIndex == borderPhases.size() - 1;
    }

    private void startBufferTimer(BorderPhase phase) {
        int timeShrinking = phase.getTimeShrinking();
        int bufferTime = phase.getPufferForNextBorder();

        this.updatingTimerTask = new BukkitRunnable() {
            int time = phase.getTimeShrinking();

            @Override
            public void run() {
                if (time == 0) cancel();

                time--;
                plugin.scoreboardManager.updateScoreboard("currentPhase", " §fAktuelle Border§8:", "");
                plugin.scoreboardManager.updateScoreboard("phaseSeconds", " §d" + String.format("%02d:%02d", (time / 60), (time % 60)), "");
                plugin.scoreboardManager.updateScoreboard("phaseSize", " §a" + ((int) Math.round(world.getWorldBorder().getSize())) + "§8x§a" + ((int) Math.round(world.getWorldBorder().getSize())), " §4➜ §d" + phase.getNewSize() + "§8x§d" + phase.getNewSize());
            }
        }.runTaskTimer(this.plugin.getInstance(), 20L, 20L);

        this.announceNextPhaseTask = new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage(plugin.serverPrefix + "§eDie Barriere hat ihr §c" + (phase.id + 1) + ". §eZiel erreicht");
                plugin.gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f);

                //movingBorder.updateBorder(Bukkit.getWorld(world.getName()).getWorldBorder());
                //movingBorder.updateCenter(phase.getCenter().getX(), phase.getCenter().getZ());

                plugin.getInstance().getLogger().info("Border reached it's destination [radius=" + phase.newSize + "]");

                if (isLast()) {
                    Bukkit.broadcastMessage(plugin.serverPrefix + "§cDie Barriere hat ihr endgültiges Ziel erreicht");
                    plugin.gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f);

                    plugin.scoreboardManager.updateScoreboard("currentPhase", " §fNächste Border§8:", "");
                    plugin.scoreboardManager.updateScoreboard("phaseSeconds", " §cEnde erreicht", "");
                    plugin.getInstance().getLogger().info("Border reached it's final phases destination");
                    stopTasks();
                    cancel();
                    return;
                }

                startAnnouncingTimer(bufferTime);
                plugin.getInstance().getLogger().info("Next border phase will start in " + bufferTime + " SECONDS");
            }
        }.runTaskLater(this.plugin.getInstance(), timeShrinking * 20L);

        if (!isLast()) {
            this.completePhaseTask = new BukkitRunnable() {
                @Override
                public void run() {
                    transitionToNextPhase();
                }
            }.runTaskLater(this.plugin.getInstance(), (timeShrinking + bufferTime) * 20L);
        }
    }

    private void startAnnouncingTimer(int var0) {
        int borderSize = (nextPhase() != null ? nextPhase().newSize : current().newSize);

        this.announceTimerTask = new BukkitRunnable() {
            int puffer = var0;

            @Override
            public void run() {
                if (puffer == 0) cancel();

                puffer--;

                plugin.scoreboardManager.updateScoreboard("currentPhase", " §fNächste Border§8:", "");
                plugin.scoreboardManager.updateScoreboard("phaseSeconds", " §d" + String.format("%02d:%02d", (puffer / 60), (puffer % 60)), "");
                plugin.scoreboardManager.updateScoreboard("phaseSize", " §b" + borderSize + "§8x§b" + borderSize, "");
            }
        }.runTaskTimer(this.plugin.getInstance(), 20L, 20L);
    }

    public void stopTasks() {
        if (this.completePhaseTask != null) this.completePhaseTask.cancel();
        if (this.announceNextPhaseTask != null) this.announceNextPhaseTask.cancel();
        if (this.announceTimerTask != null) this.announceTimerTask.cancel();
        if (this.updatingTimerTask != null) this.updatingTimerTask.cancel();
    }

    private void transitionToNextPhase() {
        next();
        start();
    }

    private boolean phaseAvailable(int phase) {
        for (BorderPhase borderPhase : this.borderPhases)
            if (borderPhase.getId() == phase) return true;
        return false;
    }

    private Location randomLocationInBorder(World world) {
        WorldBorder worldBorder = world.getWorldBorder();

        double borderSize = worldBorder.getSize() / 2.0;

        double x = randomCoordinate(worldBorder.getCenter().getX() - borderSize, worldBorder.getCenter().getX() + borderSize);
        double z = randomCoordinate(worldBorder.getCenter().getZ() - borderSize, worldBorder.getCenter().getZ() + borderSize);

        return new Location(world, x, 0, z);
    }

    private double randomCoordinate(double min, double max) {
        return min + (Math.random() * (max - min));
    }

    private void loadFromConfig() {
        String world = this.plugin.worldBorderDocument.getString("world");
        if (Bukkit.getWorld(world) == null) {
            this.plugin.getInstance().getLogger().severe("Could not find " + world + " for border phases");
            world = "world";
        }

        this.world = Bukkit.getWorld(world);

        JsonArray controller = this.plugin.worldBorderDocument.getArray("controller");

        for (JsonElement controllerElement : controller.getAsJsonArray()) {
            JsonObject controllerObject = controllerElement.getAsJsonObject();

            int id = controllerObject.get("id").getAsInt();

            JsonObject centerObject = controllerObject.getAsJsonObject("center");
            boolean randomCenter = centerObject.get("randomCenter").getAsBoolean();
            double x = centerObject.get("x").getAsDouble();
            double z = centerObject.get("z").getAsDouble();

            int oldSize = controllerObject.get("oldSize").getAsInt();
            int newSize = controllerObject.get("newSize").getAsInt();
            int timeShrinking = controllerObject.get("timeShrinking").getAsInt();
            int puffer = controllerObject.get("pufferForNextBorder").getAsInt();

            BorderPhase borderPhase = new BorderPhase(id, new Center(randomCenter, x, z), oldSize, newSize, timeShrinking, puffer);
            this.borderPhases.add(borderPhase);
        }
    }

    public static class BorderPhase {

        private Center center;
        private int id, oldSize, newSize, timeShrinking, pufferForNextBorder;

        public BorderPhase(int id, Center center, int oldSize, int newSize, int timeShrinking, int pufferForNextBorder) {
            this.id = id;
            this.center = center;
            this.oldSize = oldSize;
            this.newSize = newSize;
            this.timeShrinking = timeShrinking;
            this.pufferForNextBorder = pufferForNextBorder;
        }

        public int getId() {
            return id;
        }

        public Center getCenter() {
            return center;
        }

        public int getOldSize() {
            return oldSize;
        }

        public int getNewSize() {
            return newSize;
        }

        public int getTimeShrinking() {
            return timeShrinking;
        }

        public int getPufferForNextBorder() {
            return pufferForNextBorder;
        }

        @Override
        public String toString() {
            return "Phase{" +
                    "id=" + this.id +
                    ",center=" + this.center +
                    ",oldSize=" + this.oldSize +
                    ",newSize=" + this.newSize +
                    ",timeShrinking=" + this.timeShrinking +
                    ",puffer=" + this.pufferForNextBorder +
                    "}";
        }
    }

    public static class Center {

        private boolean randomCenter;
        private double x;
        private double z;

        public Center(boolean randomCenter, double x, double z) {
            this.randomCenter = randomCenter;
            this.x = x;
            this.z = z;
        }

        public boolean isRandomCenter() {
            return randomCenter;
        }

        public double getX() {
            return x;
        }

        public double getZ() {
            return z;
        }

        @Override
        public String toString() {
            return "Center{" +
                    "randomCenter=" + this.randomCenter +
                    ",x=" + this.x +
                    ",z=" + this.z +
                    "}";
        }
    }
}
