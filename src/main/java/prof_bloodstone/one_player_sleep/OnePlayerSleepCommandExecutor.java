package main.java.prof_bloodstone.one_player_sleep;

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
        if (strings.length != 0)
            return false;
        plugin.kickEveryoneFromBed(commandSender instanceof Player ? ((Player) commandSender).getDisplayName() : commandSender.getName());
        return true;
    }
}
