package ru.goldRush.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.goldRush.GoldRush;

public class JailCommand implements org.bukkit.command.CommandExecutor {
    public boolean onCommand(org.bukkit.command.CommandSender s, org.bukkit.command.Command c, String l, String[] a) {
        var jail = GoldRush.getInstance().getJail();
        var mods = GoldRush.getInstance().getMods();

        if (c.getName().equalsIgnoreCase("setjail")) {
            if (!(s instanceof Player p)) { s.sendMessage("§cТолько для игроков"); return true; }
            if (!p.isOp() && !mods.isMod(p)) p.sendMessage("§cНет прав");
            else { jail.setLocation(p.getLocation()); p.sendMessage("§aТюрьма установлена!"); }
        }
        else if (c.getName().equalsIgnoreCase("jail")) {
            if (!(s instanceof Player p)) { s.sendMessage("§cТолько для игроков"); return true; }
            if (!p.isOp() && !mods.isMod(p)) p.sendMessage("§cНет прав");
            else if (a.length < 1) p.sendMessage("§c/jail <ник> [минуты] [причина]");
            else { Player t = Bukkit.getPlayer(a[0]); if (t == null) p.sendMessage("§cНе найден");
            else { int min = a.length > 1 ? Integer.parseInt(a[1]) : 0; String reason = a.length > 2 ? String.join(" ", java.util.Arrays.copyOfRange(a, 2, a.length)) : "Нарушение"; jail.jail(t, min, reason); p.sendMessage("§a" + t.getName() + " в тюрьме!"); } }
        }
        else if (c.getName().equalsIgnoreCase("unjail")) {
            if (!(s instanceof Player p)) { s.sendMessage("§cТолько для игроков"); return true; }
            if (!p.isOp() && !mods.isMod(p)) p.sendMessage("§cНет прав");
            else { Player t = Bukkit.getPlayer(a[0]); if (t == null) p.sendMessage("§cНе найден"); else if (jail.unjail(t)) p.sendMessage("§aОсвобождён"); else p.sendMessage("§cНе в тюрьме"); }
        }
        else if (c.getName().equalsIgnoreCase("jailinfo")) {
            if (a.length < 1) s.sendMessage("§c/jailinfo <ник>");
            else { Player t = Bukkit.getPlayer(a[0]); if (t == null) s.sendMessage("§cНе найден");
            else if (!jail.isJailed(t.getUniqueId())) s.sendMessage("§aНе в тюрьме");
            else { long remain = jail.getRemaining(t.getUniqueId()); s.sendMessage("§6" + t.getName() + " §7в тюрьме, осталось: §e" + (remain == -1 ? "навсегда" : remain + " мин.")); } }
        }
        return true;
    }
}