package ru.goldRush.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ru.goldRush.GoldRush;

public class PremiumGiveCommand implements org.bukkit.command.CommandExecutor {

    public boolean onCommand(org.bukkit.command.CommandSender s, org.bukkit.command.Command c, String l, String[] a) {
        if (!s.isOp() && !(s instanceof Player && GoldRush.getInstance().getMods().isMod((Player) s))) {
            s.sendMessage("§cУ вас нет прав!");
            return true;
        }

        if (a.length < 2) {
            s.sendMessage("§cИспользование: /premiumgive <игрок> <дни>");
            return true;
        }

        Player target = Bukkit.getPlayer(a[0]);
        if (target == null) {
            s.sendMessage("§cИгрок не найден!");
            return true;
        }

        int days;
        try {
            days = Integer.parseInt(a[1]);
            if (days <= 0) {
                s.sendMessage("§cКоличество дней должно быть больше 0!");
                return true;
            }
        } catch (NumberFormatException e) {
            s.sendMessage("§cНеверное количество дней!");
            return true;
        }

        GoldRush.getInstance().getDB().setPremium(target.getUniqueId(), days);
        GoldRush.getInstance().getTab().updateDisplayName(target);
        GoldRush.getInstance().getNameTagManager().updateNameTag(target);

        target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&x&F&F&1&2&1&2&l★ ВАМ ВЫДАН ПРЕМИУМ ПРОПУСК! ★"));
        target.sendMessage("§aСрок: " + days + " дней");
        target.sendMessage("§aТеперь ваш ник светится в TAB!");

        s.sendMessage("§aПремиум выдан " + target.getName() + " на " + days + " дней!");
        return true;
    }
}