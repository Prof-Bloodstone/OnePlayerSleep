package main.java.prof_bloodstone.one_player_sleep;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class OnePlayerSleepRunnable extends BukkitRunnable {

    private OnePlayerSleep plugin;
    private World world;

    OnePlayerSleepRunnable(OnePlayerSleep plugin, World world) {
        this.plugin = plugin;
        this.world = world;
    }

    @Override
    public void run() {
        // TODO: Extract all the logic to separate functions
        advanceTime();
        // It seems like MC interrupts it before full day is over - but let's leave this alternative method.
        if (plugin.isMorning(world)) {
            plugin.getLogger().info("Morning - waking players up!");
            plugin.kickEveryoneFromBed();
        }
    }

    private void advanceTime() {
        long time = world.getTime();
        long rate = plugin.getRate();
        world.setTime(time + rate);
        long new_time = world.getTime();
        plugin.getLogger().info(String.format("Shifted from %d to %d", time, new_time));
    }



}
