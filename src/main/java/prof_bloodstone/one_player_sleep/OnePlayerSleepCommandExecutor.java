package main.java.prof_bloodstone.one_player_sleep;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class OnePlayerSleepCommandExecutor implements CommandExecutor {

    private OnePlayerSleep plugin;

    OnePlayerSleepCommandExecutor(OnePlayerSleep plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length != 0)
            return false;
        plugin.kickEveryoneFromBed();
        return true;
    }
}
