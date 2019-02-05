package main.java.prof_bloodstone.one_player_sleep;

import net.md_5.bungee.api.chat.*;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;


public class OnePlayerSleep extends JavaPlugin {

    private static String min_player_config = "min_players_in_bed";
    private static String min_prcnt_config = "prcnt_players_in_bed";
    final long day_length = 20 * 60 * 20;  // 20 min * 60 sec / min * 20 ticks / sec
    BukkitTask task;

    @Override
    public void onEnable() {
        super.onEnable();
        saveDefaultConfig();
        verifyConfig();
        getLogger().info("Running FINEST plugin");
        getServer().getPluginManager().registerEvents(new OnePlayerSleepListener(this), this);
        getCommand("wakeup").setExecutor(new OnePlayerSleepCommandExecutor(this));
    }

    private void resetConfig(String path) {
        getConfig().set(path, getConfig().getDefaults().get(path));
    }

    private void verifyConfig() {
        int min_players = minPlayers();
        if (min_players <= 0) {
            getLogger().warning(String.format("\"%s\" == %d, which is negative!", min_player_config, min_players));
            resetConfig(min_player_config);
        }

        double min_prcnt = minPrcnt();
        if (min_prcnt <= 0) {
            getLogger().warning(String.format("\"%s\" == %f, which is negative or zero!", min_prcnt_config, min_prcnt));
            resetConfig(min_prcnt_config);
        } else if (min_prcnt > 1) {
            getLogger().warning(String.format("\"%s\" == %f, which is more than 100%%!", min_prcnt_config, min_prcnt));
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
    List<String> getKickMessages() {
        return getConfig().getStringList("kicked_from_bed_messages");
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

    boolean isEnoughPlayersSleeping() {
        return isEnoughPlayersSleeping(0);
    }

    boolean isEnoughPlayersSleeping(int bias) {
        long sleeps = getSleepingPlayerCount() + bias;
        return sleeps > getTreshold();
    }

    int getTreshold() {
        int countTreshold = minPlayers();
        int prcntTreshold = (int) Math.ceil(minPrcnt() * getPlayerCount());
        return Math.min(countTreshold, prcntTreshold);
    }

    BaseComponent[] generateRandomSleepMessage(String player_name) {
        List<String> messageList = getSleepingMessages();
        String message = selectRandomMessage(messageList);
        return toSleepMessage(processSleepingMessage(message, player_name));
    }

    String selectRandomMessage(List<String> messages) {
        if (messages.isEmpty()) {
            throw new IllegalArgumentException("Message list cannot be empty!");
        }
        Random rand = new Random();
        return messages.get(rand.nextInt(messages.size()));
    }

    BaseComponent[] toSleepMessage(String m) {
        getLogger().info("Using " + m);
        BaseComponent[] components = TextComponent.fromLegacyText(m);
        String hoverText = getConfig().getString("message_hover_text");
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/OnePlayerSleep:wakeup");
        HoverEvent hoverEvent = null;
        if (hoverText != null)
            hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create());
        for (BaseComponent message : components) {
            message.setClickEvent(clickEvent);
            if (hoverEvent != null)
                message.setHoverEvent(hoverEvent);
        }
        return components;
    }

    String processSleepingMessage(String message, String player_name) {
        return processMessage(message, player_name)
                .replaceAll("%sleeping%", Long.toString(getSleepingPlayerCount()))
                .replaceAll("%threshold%", Integer.toString(getTreshold()));
    }

    String processMessage(String message, String player_name) {
        return message.replaceAll("%playername%", player_name)
                .replaceAll("&([a-z0-9])", "\u00a7$1"); // Allow use of color codes
    }

    void kickFromBed(Player player) {
        player.damage(0.0);
    }

    void kickEveryoneFromBed() {
        getSleepingPlayers().forEach(this::kickFromBed);
    }

    boolean isMorning(World world) {
        long rate = getRate();
        long time = world.getTime();
        return (time % day_length) <= rate;
    }

    void kickEveryoneFromBed(String player_name) {
        List<String> kickMessages = getKickMessages();
        if (!kickMessages.isEmpty()) {
            String selectedMessage = selectRandomMessage(kickMessages);
            String kickMessage = processMessage(selectedMessage, player_name);
            getSleepingPlayers().forEach(p -> p.sendMessage(kickMessage));
        }
        kickEveryoneFromBed();
    }
}
