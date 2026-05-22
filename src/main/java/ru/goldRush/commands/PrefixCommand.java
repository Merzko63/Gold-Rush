package ru.goldRush.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ru.goldRush.GoldRush;

public class PrefixCommand implements org.bukkit.command.CommandExecutor {

    public boolean onCommand(org.bukkit.command.CommandSender s, org.bukkit.command.Command c, String l, String[] a) {
        if (!(s instanceof Player p)) {
            s.sendMessage("§cТолько для игроков!");
            return true;
        }

        if (!p.isOp() && !GoldRush.getInstance().getMods().isMod(p)) {
            p.sendMessage("§cУ вас нет прав!");
            return true;
        }

        if (a.length == 0) {
            p.sendMessage("§cИспользование: /pr <цвет/код>");
            p.sendMessage("§7Примеры: &a (зелёный), &c (красный), &b (голубой), &6 (золотой)");
            p.sendMessage("§7Можно комбинировать: &a&l (зелёный жирный)");
            p.sendMessage("§7Специальные цвета: &l (жирный), &o (курсив), &n (подчёркнутый)");
            return true;
        }

        String prefix = a[0];
        if (!prefix.matches("^(&[0-9a-fk-or])+$")) {
            p.sendMessage("§cНеверный формат! Используйте цветовые коды: &a, &c, &6 и т.д.");
            return true;
        }

        GoldRush.getInstance().getDB().setPrefix(p.getUniqueId(), prefix);
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "§aПрефикс установлен: " + prefix + p.getName()));

        GoldRush.getInstance().getTab().updateAllPlayers();

        return true;
    }
}