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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OnePlayerSleepCommandExecutor implements CommandExecutor {

    private OnePlayerSleep plugin;

    OnePlayerSleepCommandExecutor(OnePlayerSleep plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length > 1 || (!(commandSender instanceof Player) && strings.length == 0)) return false;

        World world = strings.length == 0 ? ((Player) commandSender).getWorld() : plugin.getServer().getWorld(strings[0]);
        if (world == null) return false;
        if (plugin.getSleepingPlayerCount(world) != 0)
            plugin.kickEveryoneFromBed(world, commandSender instanceof Player ? ((Player) commandSender).getDisplayName() : commandSender.getName());
        else {
            String msg = plugin.getNoSleepingPlayersMessage();
            if (msg != null)
                commandSender.sendMessage(msg);
        }
        return true;
    }
}
