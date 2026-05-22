package ru.goldRush.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.goldRush.GoldRush;

public class InvseeCommand implements org.bukkit.command.CommandExecutor {

    public boolean onCommand(org.bukkit.command.CommandSender s, org.bukkit.command.Command c, String l, String[] a) {
        if (!(s instanceof Player p)) {
            s.sendMessage("§cТолько для игроков!");
            return true;
        }

        if (!p.isOp() && !GoldRush.getInstance().getMods().isMod(p)) {
            p.sendMessage("§cУ вас нет прав!");
            return true;
        }

        if (a.length < 1) {
            p.sendMessage("§cИспользование: /invsee <ник>");
            return true;
        }

        Player target = Bukkit.getPlayer(a[0]);
        if (target == null) {
            p.sendMessage("§cИгрок не найден!");
            return true;
        }

        p.openInventory(target.getInventory());
        p.sendMessage("§aПросмотр инвентаря " + target.getName());
        return true;
    }
}