package main.java.prof_bloodstone.one_player_sleep;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

public class OnePlayerSleepListener implements Listener {

    private OnePlayerSleep plugin;

    OnePlayerSleepListener(OnePlayerSleep plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onPlayerBedEnterEvent(PlayerBedEnterEvent event) {
        plugin.getLogger().info("Player " + event.getPlayer().getDisplayName() + " entered bed");
        if (!plugin.getSleepingMessages().isEmpty()) {
            BaseComponent[] message = plugin.generateRandomSleepMessage(event.getPlayer());
            plugin.getServer().spigot().broadcast(message);
        }
        if (plugin.enabled && plugin.isEnoughPlayersSleeping(+1)) {
            plugin.getLogger().info("Enough players in bed");
            if (plugin.task == null || plugin.task.isCancelled()) {
                plugin.getLogger().info("Enough players in bed and runnable not running");
                plugin.task = new OnePlayerSleepRunnable(plugin, plugin.getOverworld()).runTaskTimer(
                        plugin,
                        plugin.getConfig().getInt("delay", 60),
                        plugin.getConfig().getInt("interval", 20)
                );
            }
        }
    }

    @EventHandler
    public void onPlayerBedLeaveEvent (PlayerBedLeaveEvent event) {
        if (!plugin.isEnoughPlayersSleeping(0) && plugin.task != null && !plugin.task.isCancelled()) {
            plugin.getLogger().info("Players left the bed - canceling task");
            plugin.task.cancel();
            World world = event.getPlayer().getWorld();
            if (plugin.isMorning(world)) {
                if (world.isThundering()) {world.setThunderDuration(1);}
            }
        }
    }
}
