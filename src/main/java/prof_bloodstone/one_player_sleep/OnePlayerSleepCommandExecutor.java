package main.java.prof_bloodstone.one_player_sleep;

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
