package ru.goldRush.core;

import ru.goldRush.GoldRush;
import java.util.UUID;

public class Economy {
    public int get(UUID u) { return GoldRush.getInstance().getDB().balance(u); }
    public void set(UUID u, int v) { GoldRush.getInstance().getDB().setBalance(u, v); }
    public void add(UUID u, int v) { set(u, get(u) + v); }
    public boolean take(UUID u, int v) {
        if (get(u) >= v) { set(u, get(u) - v); return true; }
        return false;
    }
}