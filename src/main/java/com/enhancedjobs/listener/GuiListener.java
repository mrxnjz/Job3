package com.enhancedjobs.listener;

import com.enhancedjobs.EnhancedJobsPlugin;
import com.enhancedjobs.api.Job;
import com.enhancedjobs.api.JobPerk;
import com.enhancedjobs.data.PlayerData;
import com.enhancedjobs.gui.JobMenuGui;
import com.enhancedjobs.gui.QuestMenuGui;
import com.enhancedjobs.util.MessageUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Collection;
import java.util.Optional;

/**
 * Handles all click events for EnhancedJobSystem GUIs.
 */
public class GuiListener implements Listener {

    private static final String TITLE_JOB_MENU    = "⚒ Job Menu";
    private static final String TITLE_QUEST_MENU  = "📜 Quest Menu";
    private static final String TITLE_PERK_SUFFIX = "— Perks";
    private static final String TITLE_JOB_QUEST   = "📜";

    private final EnhancedJobsPlugin plugin;

    public GuiListener(EnhancedJobsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getType() == Material.AIR) return;

        Inventory inv = event.getInventory();
        String rawTitle = PlainTextComponentSerializer.plainText().serialize(inv.viewers().get(0).openInventory().title());

        if (rawTitle.contains(stripColor(TITLE_JOB_MENU)) && !rawTitle.contains(TITLE_PERK_SUFFIX)) {
            event.setCancelled(true);
            handleJobMenuClick(player, event.getSlot(), event.isLeftClick(), event.isRightClick(), rawTitle);
        } else if (rawTitle.contains(TITLE_PERK_SUFFIX)) {
            event.setCancelled(true);
            handlePerkMenuClick(player, event.getSlot(), rawTitle);
        } else if (rawTitle.contains(stripColor(TITLE_QUEST_MENU)) && !rawTitle.contains(stripColor(TITLE_JOB_QUEST) + " ")) {
            event.setCancelled(true);
            handleQuestMainClick(player, event.getSlot(), event.isLeftClick(), event.isRightClick());
        } else if (rawTitle.contains(stripColor(TITLE_JOB_QUEST)) && rawTitle.contains("Quests")) {
            event.setCancelled(true);
            handleJobQuestClick(player, event.getSlot(), event.isLeftClick(), rawTitle);
        }
    }

    // ─── Job Menu ──────────────────────────────────────────────────────────────

    private void handleJobMenuClick(Player player, int slot, boolean left, boolean right, String title) {
        if (slot < 0) return;
        playClick(player);

        Inventory inv = player.getOpenInventory().getTopInventory();
        var itemStack = inv.getItem(slot);
        if (itemStack == null || itemStack.getType() == Material.AIR) return;
        if (itemStack.getType() == Material.GRAY_STAINED_GLASS_PANE) return;
        if (itemStack.getType() == Material.BOOK) return; // info item

        String itemName = PlainTextComponentSerializer.plainText().serialize(
                itemStack.getItemMeta().displayName());

        // Find which job was clicked by matching name
        Collection<Job> jobs = plugin.getJobManager().getAllJobs();
        for (Job job : jobs) {
            String cleanJobName = stripColor(job.getName());
            if (itemName.contains(cleanJobName)) {
                PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());

                if (right && data.hasJob(job.getId())) {
                    // Right-click = leave job
                    player.closeInventory();
                    plugin.getJobManager().leaveJob(player, job.getId());
                } else if (left && data.hasJob(job.getId())) {
                    // Left-click on enrolled job = open perk GUI
                    player.openInventory(plugin.getJobMenuGui().buildPerkGui(player, job));
                } else if (left && !data.hasJob(job.getId())) {
                    // Left-click on non-enrolled = join
                    player.closeInventory();
                    plugin.getJobManager().joinJob(player, job.getId());
                }
                return;
            }
        }
    }

    // ─── Perk Menu ─────────────────────────────────────────────────────────────

    private void handlePerkMenuClick(Player player, int slot, String title) {
        if (slot < 0) return;
        playClick(player);

        Inventory inv = player.getOpenInventory().getTopInventory();
        var itemStack = inv.getItem(slot);
        if (itemStack == null || itemStack.getType() == Material.AIR) return;
        if (itemStack.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        // Back button
        if (itemStack.getType() == Material.ARROW) {
            player.openInventory(plugin.getJobMenuGui().buildJobListGui(player));
            return;
        }

        // Determine which job we're in from title
        Job job = getJobFromTitle(title, " — Perks");
        if (job == null) return;

        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        if (!data.hasJob(job.getId())) return;

        // All perks toggle (slot 4)
        if (slot == 4) {
            plugin.getJobManager().toggleAllPerks(player, job.getId());
            player.openInventory(plugin.getJobMenuGui().buildPerkGui(player, job));
            return;
        }

        // Individual perk toggle
        String itemName = PlainTextComponentSerializer.plainText().serialize(
                itemStack.getItemMeta().displayName());
        for (JobPerk perk : job.getPerks()) {
            if (itemName.contains(stripColor(perk.getName()))) {
                plugin.getJobManager().togglePerk(player, job.getId(), perk.getId());
                player.openInventory(plugin.getJobMenuGui().buildPerkGui(player, job));
                return;
            }
        }
    }

    // ─── Quest Main Menu ───────────────────────────────────────────────────────

    private void handleQuestMainClick(Player player, int slot, boolean left, boolean right) {
        if (slot < 0) return;
        playClick(player);

        Inventory inv = player.getOpenInventory().getTopInventory();
        var itemStack = inv.getItem(slot);
        if (itemStack == null || itemStack.getType() == Material.AIR) return;
        if (itemStack.getType() == Material.GRAY_STAINED_GLASS_PANE) return;
        if (itemStack.getType() == Material.BOOK) return;
        if (itemStack.getType() == Material.BARRIER) return;

        String itemName = PlainTextComponentSerializer.plainText().serialize(
                itemStack.getItemMeta().displayName());

        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        for (Job job : plugin.getJobManager().getAllJobs()) {
            if (!data.hasJob(job.getId())) continue;
            if (itemName.contains(stripColor(job.getName()))) {
                if (right && data.hasActiveQuest(job.getId())) {
                    // Right-click = abandon
                    plugin.getQuestManager().abandonQuest(player, job.getId());
                    player.openInventory(plugin.getQuestMenuGui().buildMainQuestGui(player));
                } else if (left) {
                    // Left-click = open job quest GUI
                    player.openInventory(plugin.getQuestMenuGui().buildJobQuestGui(player, job));
                }
                return;
            }
        }
    }

    // ─── Job Quest GUI ─────────────────────────────────────────────────────────

    private void handleJobQuestClick(Player player, int slot, boolean left, String title) {
        if (slot < 0) return;
        playClick(player);

        Inventory inv = player.getOpenInventory().getTopInventory();
        var itemStack = inv.getItem(slot);
        if (itemStack == null || itemStack.getType() == Material.AIR) return;
        if (itemStack.getType() == Material.GRAY_STAINED_GLASS_PANE) return;
        if (itemStack.getType() == Material.EXPERIENCE_BOTTLE) return; // info item

        // Back button
        if (itemStack.getType() == Material.ARROW) {
            player.openInventory(plugin.getQuestMenuGui().buildMainQuestGui(player));
            return;
        }

        Job job = getJobFromTitle(title, " Quests");
        if (job == null) return;

        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());

        // Abandon button (slot 31)
        if (itemStack.getType() == Material.BARRIER && slot == 31) {
            plugin.getQuestManager().abandonQuest(player, job.getId());
            player.openInventory(plugin.getQuestMenuGui().buildJobQuestGui(player, job));
            return;
        }

        // Get new quest (slot 13, writable book = no active quest)
        if (itemStack.getType() == Material.WRITABLE_BOOK && slot == 13) {
            player.closeInventory();
            plugin.getQuestManager().acquireQuest(player, job.getId());
            return;
        }
    }

    // ─── Utilities ─────────────────────────────────────────────────────────────

    private Job getJobFromTitle(String title, String suffix) {
        // Strip the suffix and leading GUI decoration to get the job name
        String cleaned = title.replace(suffix, "").trim();
        // Remove the icon prefix (📜 or ⚙) and spaces
        cleaned = cleaned.replaceAll("^[^a-zA-Z]+", "").trim();

        for (Job job : plugin.getJobManager().getAllJobs()) {
            if (cleaned.equalsIgnoreCase(stripColor(job.getName()))) {
                return job;
            }
        }
        return null;
    }

    private void playClick(Player player) {
        String soundName = plugin.getConfig().getString("gui.click-sound", "UI_BUTTON_CLICK");
        try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, 0.5f, 1f);
        } catch (Exception ignored) {}
    }

    private static String stripColor(String s) {
        return s.replaceAll("§[0-9a-fk-or]", "").replaceAll("&[0-9a-fk-or]", "").trim();
    }
}
