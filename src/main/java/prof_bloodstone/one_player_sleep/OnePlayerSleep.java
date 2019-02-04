package main.java.prof_bloodstone.one_player_sleep;

import net.md_5.bungee.api.chat.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.permission.ChildPermission;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.LoadOrder;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.stream.Stream;


@Plugin(name = "OnePlayerSleep", version = "0.1.1")
@Description("Allows for skipping the night with just 1 player in bed!")
@LoadOrder(PluginLoadOrder.POSTWORLD)
@Author("Prof_Bloodstone")
@Command(
        name = "wakeup",
        desc = "Wakes everyone up",
        permission = "ops.wakeup",
        usage = "/<command>"
)
@Permission(
        name = "ops.wakeup",
        desc = "Allows use of wakeup command",
        defaultValue = PermissionDefault.TRUE
)
@Permission(
        name = "ops.*",
        desc = "Allows for everything this plugin provides",
        defaultValue = PermissionDefault.OP,
        children = {@ChildPermission(name = "ops.wakeup")}
)
@ApiVersion(ApiVersion.Target.v1_13)
public class OnePlayerSleep extends JavaPlugin implements Listener {

    private static String min_player_config = "min_players_in_bed";
    private static String min_prcnt_config = "prcnt_players_in_bed";
    private boolean enabled = true;

    final long day_length = 20 * 60 * 20;  // 20 min * 60 sec / min * 20 ticks / sec

    BukkitTask task;

    @Override
    public void onEnable() {
        super.onEnable();
        saveDefaultConfig();
        verifyConfig();
        getLogger().info("Running FINEST plugin");
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerBedEnterEvent(PlayerBedEnterEvent event) {
        getLogger().info("Player " + event.getPlayer().getDisplayName() + " entered bed");
        if (getSleepingMessages() != null)
            getServer().spigot().broadcast(generateRandomSleepMessage(event.getPlayer()));
//            broadcast(generateRandomSleepMessage(event.getPlayer()));
        if (enabled && isEnoughPlayersSleeping(+1)) {
            getLogger().info("Enough players in bed");
            if (task == null || task.isCancelled()) {
                getLogger().info("Enough players in bed and runnable not running");
                task = new OnePlayerSleepRunnable(this, getOverworld()).runTaskTimer(
                        this,
                        getConfig().getInt("delay", 60),
                        getConfig().getInt("interval", 20)
                );
            }
        }
    }

    @EventHandler
    public void onPlayerBedLeaveEvent (PlayerBedLeaveEvent event) {
        if (!isEnoughPlayersSleeping(0) && task != null && !task.isCancelled()) {
            getLogger().info("Players left the bed - canceling task");
            task.cancel();
        }
    }

    private void resetConfig(String path) {
        getConfig().set(path, getConfig().getDefaults().get(path));
    }

    private void verifyConfig() {
        int min_players = minPlayers();
        if (min_players <= 0) {
            getLogger().warning(String.format("\"%s\" == %d, which is negative!", min_player_config, min_players));
//            enabled = false;
            resetConfig(min_player_config);
        }

        double min_prcnt = minPrcnt();
        if (min_prcnt <= 0) {
            getLogger().warning(String.format("\"%s\" == %f, which is negative or zero!", min_prcnt_config, min_prcnt));
//            enabled = false;
            resetConfig(min_prcnt_config);
        } else if (min_prcnt > 1) {
            getLogger().warning(String.format("\"%s\" == %f, which is more than 100%%!", min_prcnt_config, min_prcnt));
//            enabled = false;
            resetConfig(min_prcnt_config);
        }
    }

    private int minPlayers() {
        return getConfig().getInt(min_player_config, 1);
    }

    private double minPrcnt() {
        return getConfig().getDouble(min_prcnt_config, 0.3);
    }

    long getRate() {
        return getConfig().getLong("rate");
    }

    List<String> getSleepingMessages() {
        return getConfig().getStringList("enter_bed_messages");
    }

    World getOverworld() {
        // TODO: Don't assume we always have overworld
        return getServer().getWorlds().stream().filter(world -> world.getEnvironment() == World.Environment.NORMAL).findFirst().get();
    }

    int getPlayerCount() {
        return getOverworld().getPlayers().size();
    }

    long getSleepingPlayerCount() {
        return getSleepingPlayers().count();
    }

    Stream<Player> getSleepingPlayers() {
        return getOverworld().getPlayers().stream().filter(HumanEntity::isSleeping);
    }

    boolean isEnoughPlayersSleeping() { return isEnoughPlayersSleeping(0); }

    boolean isEnoughPlayersSleeping(int bias) {
        // bias is for when called from bed enter / leave events
//        int total_players = getPlayerCount();
        long sleeps = getSleepingPlayerCount() + bias;
//        getLogger().info(String.format("total_players = %d; sleeps = %d; minPlayers() = %d; minPrcnt() * total_players = %f",
//                total_players, sleeps, minPlayers(), minPrcnt() * total_players));
//        return sleeps >= minPlayers()
//                || sleeps >= minPrcnt() * total_players;
        return sleeps > getTreshold();
    }

    int getTreshold() {
        int countTreshold = minPlayers();
        int prcntTreshold = (int) Math.ceil(minPrcnt() * getPlayerCount());
        return Math.min(countTreshold, prcntTreshold);
    }

    BaseComponent[] generateRandomSleepMessage(Player player) {
        List<String> messageList = getSleepingMessages();
        Random rand = new Random();
        String message = messageList.get(rand.nextInt(messageList.size()));
        return toSleepMessage(processMessage(message, player));
    }

//    void broadcast(BaseComponent msg) {
//        getServer().getOnlinePlayers().forEach(player -> player.spigot().sendMessage(msg));
//    }

    BaseComponent[] toSleepMessage(String m) {
        getLogger().info("Using " + m);
        BaseComponent[] components = TextComponent.fromLegacyText(m);
        for(BaseComponent message : components) {
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "OnePlayerSleep:wakeup")); // TODO: Add slash at the beginning
            String hoverText = getConfig().getString("message_hover_text");
            if (hoverText != null)
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
        }
        return components;
    }

    String processMessage(String message, Player player) {
        getLogger().info("%playername% went to bed".replaceAll("%playername%", player.getDisplayName()));
        return message.replaceAll("%playername%", player.getDisplayName()) // WTF: Why doesn't it display player name???
                .replaceAll("%sleeping%", Long.toString(getSleepingPlayerCount()))
                .replaceAll("%threshold%", Integer.toString(getTreshold()))
                .replaceAll("&([a-z0-9])", "\u00a7$1"); // Allow use of color codes
    }

    void kickFromBed(Player player) {
        player.damage(0.0);
    }

    void kickEveryoneFromBed() {
        getSleepingPlayers().forEach(this::kickFromBed);
    }
}
