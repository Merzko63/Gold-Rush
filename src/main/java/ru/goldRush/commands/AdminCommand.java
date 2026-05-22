package ru.goldRush.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.goldRush.GoldRush;

public class AdminCommand implements org.bukkit.command.CommandExecutor {
    public boolean onCommand(org.bukkit.command.CommandSender s, org.bukkit.command.Command c, String l, String[] a) {
        if (!(s instanceof Player p)) return true;
        var mods = GoldRush.getInstance().getMods();

        if (c.getName().equalsIgnoreCase("admin")) {
            if (!p.isOp() && !mods.isMod(p)) p.sendMessage("§cНет прав");
            else mods.toggle(p);
        }
        else if (c.getName().equalsIgnoreCase("mod")) {
            if (!p.isOp()) p.sendMessage("§cНет прав");
            else if (a.length < 2) p.sendMessage("§c/mod add/remove/list <ник>");
            else if (a[0].equalsIgnoreCase("add")) { Player t = Bukkit.getPlayer(a[1]); if (t != null) mods.add(t); }
            else if (a[0].equalsIgnoreCase("remove")) { Player t = Bukkit.getPlayer(a[1]); if (t != null) mods.remove(t); }
            else if (a[0].equalsIgnoreCase("list")) p.sendMessage("§6Модеры: " + String.join(", ", mods.getList()));
        }
        else if (c.getName().equalsIgnoreCase("invsee") && (p.isOp() || mods.isMod(p))) {
            if (a.length < 1) p.sendMessage("§c/invsee <ник>");
            else { Player t = Bukkit.getPlayer(a[0]); if (t != null) p.openInventory(t.getInventory()); }
        }
        else if (c.getName().equalsIgnoreCase("ip") && (p.isOp() || mods.isMod(p))) {
            if (a.length < 1) p.sendMessage("§c/ip <ник>");
            else { String ip = GoldRush.getInstance().getDB().getIp(a[0]); if (ip == null) p.sendMessage("§cНе найден"); else { p.sendMessage("§6IP: §e" + ip); p.sendMessage("§6Аккаунты: §e" + String.join(", ", GoldRush.getInstance().getDB().getAccountsByIp(ip))); } }
        }
        return true;
    }
}