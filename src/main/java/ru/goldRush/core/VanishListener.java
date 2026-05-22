package ru.goldRush.core;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.goldRush.GoldRush;

public class VanishListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (GoldRush.getInstance().getVanish().isVanished(p)) {
            for (Player pl : org.bukkit.Bukkit.getOnlinePlayers()) {
                pl.showPlayer(GoldRush.getInstance(), p);
            }
        }
    }
}