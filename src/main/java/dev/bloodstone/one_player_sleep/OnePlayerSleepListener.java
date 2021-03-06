package dev.bloodstone.one_player_sleep;

/*
   OnePlayerSleep - simple sleeping plugin for multiplayer Spigot-compatible Minecraft servers.
    Copyright (C) 2019 Prof_Bloodstone

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
        if (event.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.OK) {
            World world = event.getPlayer().getWorld();
            String world_name = world.getName();
            if (!plugin.getSleepingMessages().isEmpty()) {
                BaseComponent[] message = plugin.generateRandomSleepMessage(world, event.getPlayer().getDisplayName());
                plugin.getServer().spigot().broadcast(message);
            }
            if (plugin.tasks.getOrDefault(world_name, null) == null
                    || plugin.tasks.get(world_name).isCancelled()) {
                plugin.tasks.put(world_name, new OnePlayerSleepRunnable(plugin, world).runTaskTimer(
                        plugin,
                        plugin.getConfig().getInt("delay", 60),
                        plugin.getConfig().getInt("interval", 20)
                ));
            }
        }
    }

    @EventHandler
    public void onPlayerBedLeaveEvent (PlayerBedLeaveEvent event) {
        World world = event.getPlayer().getWorld();
        String world_name = world.getName();
        if (plugin.getSleepingPlayerCount(world) <= 1 // It's called BEFORE they leave bed
                && plugin.tasks.getOrDefault(world_name, null) != null
                && !plugin.tasks.get(world_name).isCancelled()) {
            plugin.tasks.get(world_name).cancel();
            if (plugin.isMorning(world)) {
                if (world.hasStorm()) {
                    world.setWeatherDuration(1);
                }
            }
        }
    }
}
