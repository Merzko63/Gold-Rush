package ru.goldRush.modules;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.goldRush.GoldRush;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ServerBrandManager implements Listener {
    private final GoldRush plugin;
    private final String brandName;

    public ServerBrandManager(GoldRush plugin) {
        this.plugin = plugin;
        this.brandName = ChatColor.translateAlternateColorCodes('&', "&x&7&D&E&A&2&0&lGoldRush by merzko63");

        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, "minecraft:brand");
        Bukkit.getPluginManager().registerEvents(this, plugin);

        Bukkit.getServer().setMotd(ChatColor.translateAlternateColorCodes('&', "&x&7&D&E&A&2&0&lGoldRush by merzko63"));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        sendBrand(e.getPlayer());
    }

    public void sendBrand(Player p) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            writeString(out, this.brandName);
            byte[] data = b.toByteArray();
            p.sendPluginMessage(this.plugin, "minecraft:brand", data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeString(DataOutputStream out, String s) throws IOException {
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, b.length);
        out.write(b);
    }

    private void writeVarInt(DataOutputStream out, int value) throws IOException {
        while ((value & -128) != 0) {
            out.writeByte(value & 127 | 128);
            value >>>= 7;
        }
        out.writeByte(value);
    }
}