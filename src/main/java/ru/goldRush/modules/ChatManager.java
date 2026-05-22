package ru.goldRush.modules;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.goldRush.GoldRush;

public class ChatManager implements Listener {

    public ChatManager() {
        Bukkit.getPluginManager().registerEvents(this, GoldRush.getInstance());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        e.setCancelled(true);
        Player p = e.getPlayer();
        String msg = e.getMessage();

        String ratingStars = GoldRush.getInstance().getRatingManager().getRatingStars(p.getUniqueId());
        String nameColor = GoldRush.getInstance().getTab().getPlayerNameColor(p);
        String coloredName = ratingStars + " " + nameColor + p.getName();

        String consoleMsg = (msg.startsWith("!") ? "[G] " : "[L] ") + p.getName() + ": " + msg;
        Bukkit.getConsoleSender().sendMessage(consoleMsg);

        if (msg.startsWith("!")) {
            String globalMsg = msg.substring(1);
            String format = "§8&l[§6Г§8&l] " + coloredName + "§f: " + globalMsg;
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', format));
        } else {
            String format = "§8&l[§7Л§8&l] " + coloredName + "§f: " + msg;
            for (Player nearby : Bukkit.getOnlinePlayers()) {
                if (nearby.getLocation().distance(p.getLocation()) <= 50) {
                    nearby.sendMessage(ChatColor.translateAlternateColorCodes('&', format));
                }
            }
        }
    }
}