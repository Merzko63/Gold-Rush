package ru.goldRush.core;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.goldRush.GoldRush;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class RatingManager implements CommandExecutor, Listener, TabCompleter {
    private final GoldRush plugin;
    private final Database db;

    public RatingManager(GoldRush plugin) {
        this.plugin = plugin;
        this.db = plugin.getDB();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public int getRating(UUID uuid) {
        return db.getRating(uuid);
    }

    public void modifyRating(UUID uuid, int amount) {
        int current = getRating(uuid);
        int newRating = current + amount;
        newRating = Math.max(-15, Math.min(15, newRating));
        db.setRating(uuid, newRating);
    }

    public String getRatingStars(UUID uuid) {
        return formatStars(getRating(uuid));
    }

    private String formatStars(int r) {
        if (r == 0) {
            return ChatColor.translateAlternateColorCodes('&', "&f*****");
        }

        int abs = Math.abs(r);
        int progress = (abs - 1) % 5 + 1;
        int tier = (abs - 1) / 5;

        String leftColor;
        String rightColor;

        if (r > 0) {
            if (tier == 0) { leftColor = "&f"; rightColor = "&7"; }
            else if (tier == 1) { leftColor = "&b"; rightColor = "&f"; }
            else { leftColor = "&3"; rightColor = "&b"; }
        } else {
            if (tier == 0) { leftColor = "&7"; rightColor = "&f"; }
            else if (tier == 1) { leftColor = "&8"; rightColor = "&7"; }
            else { leftColor = "&0"; rightColor = "&8"; }
        }

        String stars;
        if (r > 0) {
            stars = leftColor + repeat("*", progress) + rightColor + repeat("*", 5 - progress);
        } else {
            stars = rightColor + repeat("*", 5 - progress) + leftColor + repeat("*", progress);
        }

        return ChatColor.translateAlternateColorCodes('&', stars);
    }

    private String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append(s);
        return sb.toString();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (!db.existsInRatings(uuid)) {
            db.setRating(uuid, 0);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp() && !(sender instanceof Player && plugin.getMods().isMod((Player) sender))) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав!");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Использование: /rating <игрок> <+/-> <количество>");
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            sender.sendMessage(ChatColor.RED + "Игрок не найден!");
            return true;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
            if (amount <= 0) {
                sender.sendMessage(ChatColor.RED + "Количество должно быть положительным!");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Неверное число!");
            return true;
        }
        UUID uuid = target.getUniqueId();
        if (args[1].equals("+")) {
            modifyRating(uuid, amount);
            sender.sendMessage(ChatColor.GREEN + "Рейтинг увеличен для " + target.getName());
        } else if (args[1].equals("-")) {
            modifyRating(uuid, -amount);
            sender.sendMessage(ChatColor.RED + "Рейтинг уменьшен для " + target.getName());
        } else {
            sender.sendMessage(ChatColor.RED + "Используйте + или -");
            return true;
        }
        sender.sendMessage(ChatColor.GRAY + "Новый рейтинг: " + formatStars(getRating(uuid)));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return null;
        if (args.length == 2) return Arrays.asList("+", "-");
        if (args.length == 3) return Arrays.asList("1", "3", "5");
        return new ArrayList<>();
    }
}