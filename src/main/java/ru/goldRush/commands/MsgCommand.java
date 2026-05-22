// MsgCommand.java - новый файл
package ru.goldRush.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ru.goldRush.GoldRush;

public class MsgCommand implements org.bukkit.command.CommandExecutor {

    public boolean onCommand(org.bukkit.command.CommandSender s, org.bukkit.command.Command c, String l, String[] a) {
        if (!(s instanceof Player sender)) {
            s.sendMessage("§cТолько для игроков!");
            return true;
        }

        if (a.length < 2) {
            sender.sendMessage(ChatColor.RED + "Использование: /msg <игрок> <сообщение>");
            return true;
        }

        String targetName = a[0];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null || !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Игрок не найден!");
            return true;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < a.length; i++) {
            builder.append(a[i]).append(" ");
        }

        String message = builder.toString().trim();
        if (message.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Нельзя отправить пустое сообщение!");
            return true;
        }

        String prefixSender = GoldRush.getInstance().getRatingManager().getRatingStars(sender.getUniqueId()) + " ";
        String prefixTarget = GoldRush.getInstance().getRatingManager().getRatingStars(target.getUniqueId()) + " ";

        String nameColorSender = GoldRush.getInstance().getTab().getPlayerNameColor(sender);
        String nameColorTarget = GoldRush.getInstance().getTab().getPlayerNameColor(target);

        String toSender = ChatColor.translateAlternateColorCodes('&',
                "&f[&2me &f-> &2" + nameColorTarget + target.getName() + "&f] " + message);
        String toTarget = ChatColor.translateAlternateColorCodes('&',
                "&f[&2" + nameColorSender + sender.getName() + " &f-> &2me&f] " + message);

        sender.sendMessage(toSender);
        target.sendMessage(toTarget);

        return true;
    }
}