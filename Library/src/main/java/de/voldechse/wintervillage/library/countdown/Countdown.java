package de.voldechse.wintervillage.library.countdown;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public class Countdown {

    private final Plugin plugin;
    private final CountdownListener countdownListener;

    private BukkitTask bukkitTask;

    private int countdownTime, initializedTime;

    private boolean isSleeping;

    public Countdown(Plugin plugin, CountdownListener countdownListener) {
        this.plugin = plugin;
        this.countdownListener = countdownListener;
        this.isSleeping = true;
    }

    public void startCountdown(int seconds, boolean countUp) {
        /**
        this.isSleeping = false;
        this.countdownTime = seconds;
        this.countdownListener.start();
        this.bukkitTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            this.countdownListener.second(this.countdownTime);
            if (this.countdownTime <= 0) this.stopCountdown(true);
            this.countdownTime--;
        }, 0L, 20L);
         */

        this.isSleeping = false;
        this.countdownTime = countUp ? 0 : seconds;
        this.countdownListener.start();

        int increment = countUp ? 1 : -1;

        this.bukkitTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            this.countdownListener.second(this.countdownTime);

            /**
            if (countUp) {
                if (this.countdownTime >= seconds) this.stopCountdown(true);
            } else {
                if (this.countdownTime <= 0) this.stopCountdown(true);
            }
             */

            if ((countUp && this.countdownTime >= seconds) || (!countUp && this.countdownTime <= 0))
                this.stopCountdown(true);

            this.countdownTime += increment;
        }, 0L, 20L);
    }

    public void stopCountdown(boolean countdownStop) {
        if (countdownStop) this.countdownListener.stop();
        if (this.bukkitTask != null) this.bukkitTask.cancel();
    }

    public void sleepCountdown(int sleepingSeconds) {
        this.isSleeping = true;

        BukkitScheduler scheduler = Bukkit.getScheduler();
        Plugin plugin = this.plugin;
        CountdownListener countdownListener = this.countdownListener;

        this.bukkitTask = scheduler.runTaskTimer(plugin, countdownListener::sleep, sleepingSeconds * 20L, sleepingSeconds * 20L);
    }

    public boolean isSleeping() {
        return this.isSleeping;
    }

    public void setCountdownTime(int countdownTime) {
        this.countdownTime = countdownTime;
    }

    public int getCountdownTime() {
        return this.countdownTime;
    }

    public void setInitializedTime(int initializedTime) {
        this.initializedTime = initializedTime;
    }

    public int getInitializedTime() {
        return this.initializedTime;
    }
}