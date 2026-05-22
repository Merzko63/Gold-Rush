package ru.goldRush.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.goldRush.GoldRush;
import java.util.*;

public class ModManager {
    private Set<UUID> mods = new HashSet<>();
    private Set<UUID> adminMode = new HashSet<>();

    public ModManager() {
        List<String> list = GoldRush.getInstance().getConfig().getStringList("mods");
        for (String uuid : list) {
            try { mods.add(UUID.fromString(uuid)); } catch (Exception e) {}
        }
    }

    public void add(Player p) {
        mods.add(p.getUniqueId());
        save();
        p.sendMessage("§aВы стали модератором!");
        Bukkit.broadcast("§6★ " + p.getName() + " назначен модератором!", "goldrush.notify");
    }

    public void remove(Player p) {
        mods.remove(p.getUniqueId());
        adminMode.remove(p.getUniqueId());
        save();
        p.sendMessage("§cВы больше не модератор!");
    }

    public boolean isMod(Player p) { return mods.contains(p.getUniqueId()) || p.isOp(); }

    public List<String> getList() {
        List<String> list = new ArrayList<>();
        for (UUID u : mods) {
            Player p = Bukkit.getPlayer(u);
            list.add(p != null ? p.getName() + " (онлайн)" : u.toString().substring(0, 8));
        }
        return list;
    }

    public void toggle(Player p) {
        UUID u = p.getUniqueId();
        if (adminMode.contains(u)) {
            adminMode.remove(u);
            for (Player pl : Bukkit.getOnlinePlayers()) pl.showPlayer(GoldRush.getInstance(), p);
            p.sendMessage("§cАдмин-режим выключен");
        } else {
            adminMode.add(u);
            for (Player pl : Bukkit.getOnlinePlayers()) {
                if (!isMod(pl)) pl.hidePlayer(GoldRush.getInstance(), p);
            }
            p.sendMessage("§aАдмин-режим включён (невидим)");
        }
    }

    private void save() {
        List<String> list = new ArrayList<>();
        for (UUID u : mods) list.add(u.toString());
        GoldRush.getInstance().getConfig().set("mods", list);
        GoldRush.getInstance().saveConfig();
    }
}