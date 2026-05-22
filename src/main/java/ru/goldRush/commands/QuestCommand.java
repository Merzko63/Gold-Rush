package ru.goldRush.commands;

import org.bukkit.entity.Player;
import ru.goldRush.GoldRush;

public class QuestCommand implements org.bukkit.command.CommandExecutor {
    public boolean onCommand(org.bukkit.command.CommandSender s, org.bukkit.command.Command c, String l, String[] a) {
        if (!(s instanceof Player p)) return true;
        if (c.getName().equalsIgnoreCase("quest")) GoldRush.getInstance().getQuests().show(p);
        return true;
    }
}