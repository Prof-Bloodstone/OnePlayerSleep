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

public class OnePlayerSleepRunnable extends BukkitRunnable {

    private OnePlayerSleep plugin;
    private World world;

    OnePlayerSleepRunnable(OnePlayerSleep plugin, World world) {
        this.plugin = plugin;
        this.world = world;
    }

    @Override
    public void run() {
        if(plugin.isEnoughPlayersSleeping(world)) {
            advanceTime();
        }
    }

    private void advanceTime() {
        long time = world.getFullTime();
        long rate = plugin.getRate();
        if (!plugin.nightEnded(world)) {
            world.setFullTime(time + rate);
        } else if (world.hasStorm()) {
            world.setWeatherDuration(1);
        }
    }

}
