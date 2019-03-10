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

import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class OnePlayerSleepRunnable extends BukkitRunnable {

    private OnePlayerSleep plugin;
    private World world;
    private BukkitTask task;

    OnePlayerSleepRunnable(OnePlayerSleep plugin, World world) {
        this.plugin = plugin;
        this.world = world;
    }

    @Override
    public void run() {
        plugin.DEBUG("Runnable - run");
        if(plugin.isEnoughPlayersSleeping(world)) {
            plugin.DEBUG("Runnable - Enough people sleeping!");
            advanceTime();
            // It seems like MC interrupts it before full day is over - but let's leave this alternative method.
            if (plugin.isMorning(world)) {
                plugin.DEBUG("Skipper - it's morning!");
                plugin.kickEveryoneFromBed(world);
            }
        }
        else plugin.DEBUG("Runnable - not enough people sleeping!");
    }

    private void advanceTime() {
        long time = world.getTime();
        long rate = plugin.getRate();
        world.setTime(time + rate);
    }

}
