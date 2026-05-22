package ru.goldRush.commands;

import org.bukkit.entity.Player;
import ru.goldRush.GoldRush;

public class AuthCommand implements org.bukkit.command.CommandExecutor {
    public boolean onCommand(org.bukkit.command.CommandSender s, org.bukkit.command.Command c, String l, String[] a) {
        if (!(s instanceof Player p)) return true;
        String name = c.getName().toLowerCase();
        var db = GoldRush.getInstance().getDB();

        if (name.equals("register") || name.equals("reg")) {
            if (a.length != 1) p.sendMessage("§c/reg <пароль>");
            else
                if (db.exists(p.getUniqueId())) p.sendMessage("§cУже зарегистрирован");

            else {
                db.register(p.getUniqueId(), p.getName(), a[0], p.getAddress().getAddress().getHostAddress()); p.sendMessage("§aРегистрация успешна!");
            }
        }
        else if (name.equals("login") || name.equals("l")) {
            if (a.length != 1) p.sendMessage("§c/login <пароль>");
            else if (db.pass(p.getUniqueId(), a[0])) {
                db.updateIp(p.getUniqueId(), p.getAddress().getAddress().getHostAddress(), p.getName()); p.sendMessage("§aАвторизован!");
            }
            else p.sendMessage("§cНеверный пароль");
        }
        else if (name.equals("changepassword") || name.equals("chpass")) {
            if (a.length != 2) p.sendMessage("§c/chpass <старый> <новый>");
            else
                if (!db.pass(p.getUniqueId(), a[0])) p.sendMessage("§cНеверный старый пароль");

            else { db.exec("UPDATE users SET pass='" + a[1] + "' WHERE uuid='" + p.getUniqueId() + "'");
                p.sendMessage("§aПароль изменён!");
            }
        }
        return true;
    }
}