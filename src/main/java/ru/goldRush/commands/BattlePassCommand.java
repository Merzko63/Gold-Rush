package ru.goldRush.commands;

import org.bukkit.entity.Player;
import ru.goldRush.GoldRush;

public class BattlePassCommand implements org.bukkit.command.CommandExecutor {
    public boolean onCommand(org.bukkit.command.CommandSender s, org.bukkit.command.Command c, String l, String[] a) {
        if (!(s instanceof Player p)) return true;
        GoldRush.getInstance().getBattlePass().show(p);
        return true;
    }
}