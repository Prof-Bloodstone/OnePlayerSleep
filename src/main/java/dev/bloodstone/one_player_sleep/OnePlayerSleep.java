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

import net.md_5.bungee.api.chat.*;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;


public class OnePlayerSleep extends JavaPlugin {

    private static String min_player_config = "min_players_in_bed";
    private static String min_prcnt_config = "prcnt_players_in_bed";
    final long day_length = 20 * 60 * 20;  // 20 min * 60 sec / min * 20 ticks / sec
    Map<String, BukkitTask> tasks = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        super.onEnable();
        saveDefaultConfig();
        verifyConfig();
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

    String getNoSleepingPlayersMessage() { return getConfig().getString("no_players_sleeping", null); }

    int getPlayerCount(World world) {
        return world.getPlayers().size();
    }

    long getSleepingPlayerCount(World world) {
        return getSleepingPlayers(world).count();
    }

    Stream<Player> getSleepingPlayers(World world) {
        return world.getPlayers().stream().filter(HumanEntity::isSleeping);
    }

    boolean isEnoughPlayersSleeping(World world) {
        return isEnoughPlayersSleeping(world,0);
    }

    boolean isEnoughPlayersSleeping(World world, int bias) {
        long sleeps = getSleepingPlayerCount(world) + bias;
        return sleeps >= getTreshold(world);
    }

    int getTreshold(World world) {
        int countTreshold = minPlayers();
        int prcntTreshold = (int) Math.ceil(minPrcnt() * getPlayerCount(world));
        return Math.min(countTreshold, prcntTreshold);
    }

    BaseComponent[] generateRandomSleepMessage(World world, String player_name) {
        List<String> messageList = getSleepingMessages();
        String message = selectRandomMessage(messageList);
        return toSleepMessage(processSleepingMessage(world, message, player_name), world);
    }

    String selectRandomMessage(List<String> messages) {
        if (messages.isEmpty()) {
            throw new IllegalArgumentException("Message list cannot be empty!");
        }
        Random rand = new Random();
        return messages.get(rand.nextInt(messages.size()));
    }

    BaseComponent[] toSleepMessage(String m, World world) {
        getLogger().info("Using " + m);
        BaseComponent[] components = TextComponent.fromLegacyText(m);
        String hoverText = getConfig().getString("message_hover_text", null);
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/OnePlayerSleep:wakeup " + world.getName());
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

    String processSleepingMessage(World world, String message, String player_name) {
        long sleeping = getSleepingPlayerCount(world) + 1; // +1 because player going to bed isn't counted yet.
        int treshold = getTreshold(world);
        long needed = Math.max(treshold - sleeping, 0);
        return processMessage(message, player_name)
                .replaceAll("%sleeping%", Long.toString(sleeping))
                .replaceAll("%threshold%", Integer.toString(treshold))
                .replaceAll("%needed%", Long.toString(needed));
    }

    String processMessage(String message, String player_name) {
        return message.replaceAll("%playername%", player_name)
                .replaceAll("&([a-z0-9])", "\u00a7$1"); // Allow use of color codes
    }

    void kickFromBed(Player player) {
        player.damage(0.0);
    }

    boolean isMorning(World world) {
        long rate = getRate();
        long time = world.getTime();
        return (time % day_length) <= rate;
    }

    void kickEveryoneFromBed(World world) {
        DEBUG("Kicking everyone");
        getSleepingPlayers(world).forEach(this::kickFromBed);
    }

    void kickEveryoneFromBed(World world, String player_name) {
        List<String> kickMessages = getKickMessages();
        if (!kickMessages.isEmpty()) {
            String selectedMessage = selectRandomMessage(kickMessages);
            String kickMessage = processMessage(selectedMessage, player_name);
            getServer().broadcastMessage(kickMessage);
        }
        kickEveryoneFromBed(world);
    }

    void DEBUG(String message) {
        getServer().broadcastMessage("OPS: " + message);
    }
}
