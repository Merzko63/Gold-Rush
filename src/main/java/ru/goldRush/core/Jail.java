package ru.goldRush.core;

import org.bukkit.*;
import org.bukkit.entity.Player;
import ru.goldRush.GoldRush;
import java.util.*;

public class Jail {
    private Location loc;
    private Map<UUID, Long> jailed = new HashMap<>();
    private Map<UUID, Location> prev = new HashMap<>();

    public void setLocation(Location l) {
        loc = l;
        GoldRush.getInstance().getConfig().set("jail", l.getWorld().getName() + "," + l.getX() + "," + l.getY() + "," + l.getZ());
        GoldRush.getInstance().saveConfig();
        // Загружаем из конфига при запуске
        if (GoldRush.getInstance().getConfig().contains("jail")) {
            String[] data = GoldRush.getInstance().getConfig().getString("jail").split(",");
            World w = Bukkit.getWorld(data[0]);
            if (w != null) loc = new Location(w, Double.parseDouble(data[1]), Double.parseDouble(data[2]), Double.parseDouble(data[3]));
        }
    }

    public void jail(Player p, int min, String reason) {
        if (loc == null) { p.sendMessage("§cТюрьма не установлена!"); return; }
        prev.put(p.getUniqueId(), p.getLocation());
        p.teleport(loc);
        jailed.put(p.getUniqueId(), min > 0 ? System.currentTimeMillis() + min * 60000L : -1);
        p.sendMessage(ChatColor.RED + "§lВЫ В ТЮРЬМЕ! " + ChatColor.GRAY + reason);
        if (min > 0) p.sendMessage(ChatColor.GRAY + "Освобождение через " + min + " мин.");
        else p.sendMessage(ChatColor.RED + "Вы здесь навсегда!");
    }

    public boolean unjail(Player p) {
        if (!jailed.containsKey(p.getUniqueId())) return false;
        p.teleport(prev.getOrDefault(p.getUniqueId(), Bukkit.getWorlds().get(0).getSpawnLocation()));
        jailed.remove(p.getUniqueId());
        prev.remove(p.getUniqueId());
        p.sendMessage(ChatColor.GREEN + "§lВы освобождены!");
        return true;
    }

    public boolean isJailed(UUID u) {
        Long time = jailed.get(u);
        if (time == null) return false;
        if (time == -1) return true;
        if (time > System.currentTimeMillis()) return true;
        jailed.remove(u);
        return false;
    }

    public long getRemaining(UUID u) {
        Long time = jailed.get(u);
        if (time == null) return 0;
        if (time == -1) return -1;
        return (time - System.currentTimeMillis()) / 60000;
    }
}