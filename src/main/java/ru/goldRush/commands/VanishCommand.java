package ru.goldRush.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.goldRush.GoldRush;
import java.util.*;

public class VanishCommand implements org.bukkit.command.CommandExecutor {
    private Set<UUID> vanished = new HashSet<>();

    public boolean onCommand(org.bukkit.command.CommandSender s, org.bukkit.command.Command c, String l, String[] a) {
        if (!(s instanceof Player p)) {
            s.sendMessage("§cТолько для игроков!");
            return true;
        }

        if (!p.isOp() && !GoldRush.getInstance().getMods().isMod(p)) {
            p.sendMessage("§cУ вас нет прав!");
            return true;
        }

        UUID uuid = p.getUniqueId();

        if (vanished.contains(uuid)) {
            vanished.remove(uuid);
            for (Player pl : Bukkit.getOnlinePlayers()) {
                pl.showPlayer(GoldRush.getInstance(), p);
            }
            p.setCollidable(true);
            p.sendMessage("§cВаниш выключен! Тебя видят все игроки");
        } else {
            vanished.add(uuid);
            for (Player pl : Bukkit.getOnlinePlayers()) {
                if (!pl.isOp() && !GoldRush.getInstance().getMods().isMod(pl)) {
                    pl.hidePlayer(GoldRush.getInstance(), p);
                }
            }
            p.setCollidable(false);
            p.sendMessage("§aВаниш включён! Ты невидим для обычных игроков");
        }
        return true;
    }

    public boolean isVanished(Player p) {
        return vanished.contains(p.getUniqueId());
    }
}