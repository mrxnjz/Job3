package com.enhancedjobs.gui;

import com.enhancedjobs.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public final class GuiUtil {

    private GuiUtil() {}

    public static Inventory createInventory(String title, int rows) {
        return Bukkit.createInventory(null, rows * 9, net.kyori.adventure.text.Component.text(
                MessageUtil.color(title)));
    }

    public static ItemStack makeItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.displayName(net.kyori.adventure.text.Component.text(MessageUtil.color(name)));
        if (lore.length > 0) {
            meta.lore(Arrays.stream(lore)
                    .map(l -> net.kyori.adventure.text.Component.text(MessageUtil.color(l)))
                    .toList());
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeItem(Material mat, String name, List<String> lore) {
        return makeItem(mat, name, lore.toArray(new String[0]));
    }

    public static ItemStack fillerPane() {
        return makeItem(Material.GRAY_STAINED_GLASS_PANE, " ");
    }

    public static void fillBorder(Inventory inv) {
        int size = inv.getSize();
        int cols = 9;
        int rows = size / cols;
        ItemStack pane = fillerPane();
        // Top row
        for (int i = 0; i < cols; i++) inv.setItem(i, pane);
        // Bottom row
        for (int i = size - cols; i < size; i++) inv.setItem(i, pane);
        // Left and right columns
        for (int r = 1; r < rows - 1; r++) {
            inv.setItem(r * cols, pane);
            inv.setItem(r * cols + cols - 1, pane);
        }
    }

    public static void fillRow(Inventory inv, int row) {
        ItemStack pane = fillerPane();
        int start = row * 9;
        for (int i = start; i < start + 9; i++) inv.setItem(i, pane);
    }

    /** Build a text progress bar using Unicode blocks. */
    public static String progressBar(double progress, int length) {
        int filled = (int) Math.round(progress * length);
        filled = Math.max(0, Math.min(filled, length));
        String bar = "█".repeat(filled) + "░".repeat(length - filled);
        String color = progress >= 1.0 ? "§a" : progress >= 0.5 ? "§e" : "§c";
        return color + "█".repeat(filled) + "§8" + "░".repeat(length - filled);
    }

    /** Slots for items in a grid inside a bordered inventory. */
    public static int[] innerSlots(int rows) {
        // Returns inner slots (excluding border) for up to 4 inner rows
        int innerRows = rows - 2;
        int[] slots = new int[innerRows * 7];
        int idx = 0;
        for (int r = 1; r <= innerRows; r++) {
            for (int c = 1; c <= 7; c++) {
                slots[idx++] = r * 9 + c;
            }
        }
        return slots;
    }
}
