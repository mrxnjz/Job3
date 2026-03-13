package com.enhancedjobs.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class MessageUtil {

    public static final String PREFIX = ChatColor.GOLD + "[" + ChatColor.YELLOW + "Jobs" + ChatColor.GOLD + "] " + ChatColor.RESET;
    public static final String ERROR   = ChatColor.RED.toString();
    public static final String SUCCESS = ChatColor.GREEN.toString();
    public static final String INFO    = ChatColor.AQUA.toString();
    public static final String ACCENT  = ChatColor.YELLOW.toString();

    private MessageUtil() {}

    public static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static void send(CommandSender sender, String msg) {
        sender.sendMessage(PREFIX + color(msg));
    }

    public static void sendRaw(CommandSender sender, String msg) {
        sender.sendMessage(color(msg));
    }

    public static void sendError(CommandSender sender, String msg) {
        sender.sendMessage(PREFIX + ChatColor.RED + color(msg));
    }

    public static void sendSuccess(CommandSender sender, String msg) {
        sender.sendMessage(PREFIX + ChatColor.GREEN + color(msg));
    }

    public static void sendInfo(CommandSender sender, String msg) {
        sender.sendMessage(PREFIX + ChatColor.AQUA + color(msg));
    }

    public static void actionBar(Player player, String msg) {
        player.sendActionBar(net.kyori.adventure.text.Component.text(
                ChatColor.translateAlternateColorCodes('&', msg)));
    }

    public static List<String> colorList(List<String> lines) {
        return lines.stream().map(MessageUtil::color).toList();
    }

    public static String formatCurrency(double amount) {
        if (amount == Math.floor(amount)) return String.valueOf((long) amount);
        return String.format("%.1f", amount);
    }
}
