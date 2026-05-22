package ru.goldRush.core;

import org.bukkit.*;
import org.bukkit.entity.Player;
import ru.goldRush.GoldRush;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class QuestManager {
    private Map<UUID, DailyQuest> quests = new HashMap<>();
    private List<QuestTemplate> templates = new ArrayList<>();

    public QuestManager() {
        // Дефолтные шаблоны
        templates.add(new QuestTemplate("kill", "ZOMBIE", "Убить зомби", 10, 50));
        templates.add(new QuestTemplate("kill", "SKELETON", "Убить скелетов", 8, 50));
        templates.add(new QuestTemplate("kill", "CREEPER", "Убить крипера", 5, 70));
        templates.add(new QuestTemplate("mine", "COAL_ORE", "Добыть угля", 32, 30));
        templates.add(new QuestTemplate("mine", "IRON_ORE", "Добыть железа", 16, 60));
        templates.add(new QuestTemplate("mine", "GOLD_ORE", "Добыть золота", 8, 100));
        templates.add(new QuestTemplate("break", "OAK_LOG", "Сломать дуба", 40, 40));
        templates.add(new QuestTemplate("break", "STONE", "Сломать камня", 64, 30));
        templates.add(new QuestTemplate("fish", "ANY", "Поймать рыбы", 15, 70));
        templates.add(new QuestTemplate("walk", "ANY", "Пройди метров", 5000, 80));
    }

    public void startDailyReset() {
        Bukkit.getScheduler().runTaskTimer(GoldRush.getInstance(), () -> {
            long day = System.currentTimeMillis() / 86400000;
            for (Player p : Bukkit.getOnlinePlayers()) {
                DailyQuest q = quests.get(p.getUniqueId());
                if (q == null || q.day != day) {
                    generateNewQuest(p.getUniqueId(), p);
                }
            }
        }, 0L, 72000L);
    }

    public void generateNewQuest(UUID uuid, Player p) {
        if (templates.isEmpty()) return;
        ThreadLocalRandom r = ThreadLocalRandom.current();
        QuestTemplate t = templates.get(r.nextInt(templates.size()));

        int amount = t.amount + r.nextInt(-3, 4);
        if (amount < 1) amount = 1;
        int reward = t.reward + r.nextInt(-15, 16);
        if (reward < 10) reward = 10;

        DailyQuest quest = new DailyQuest(t.type, t.target, t.name, amount, reward, 0, false, System.currentTimeMillis() / 86400000);
        quests.put(uuid, quest);

        if (p != null && p.isOnline()) {
            p.sendMessage(ChatColor.GOLD + "═══════════════════════");
            p.sendMessage(ChatColor.GOLD + "★ НОВОЕ ЗАДАНИЕ!");
            p.sendMessage(ChatColor.YELLOW + t.name + " §7(" + amount + ")");
            p.sendMessage(ChatColor.GREEN + "Награда: +" + reward + " монет");
            p.sendMessage(ChatColor.GOLD + "═══════════════════════");
        }
    }

    public void handle(Player p, String type, String target, int amt) {
        UUID u = p.getUniqueId();
        DailyQuest q = quests.get(u);
        if (q == null) { generateNewQuest(u, p); q = quests.get(u); }
        if (q == null || q.completed) return;
        if (!q.type.equals(type)) return;
        if (!q.target.equals(target) && !q.target.equals("ANY")) return;

        q.progress += amt;
        if (q.progress >= q.amount) {
            q.completed = true;
            GoldRush.getInstance().getEco().add(u, q.reward);
            p.sendTitle(ChatColor.GOLD + "ЗАДАНИЕ ВЫПОЛНЕНО!", ChatColor.YELLOW + "+" + q.reward + " монет", 10, 40, 10);
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        } else {
            int percent = (q.progress * 100) / q.amount;
            if (percent == 50 || percent == 90 || q.progress == q.amount - 1) {
                p.sendMessage(ChatColor.GREEN + "📜 " + q.name + ": §e" + q.progress + "/" + q.amount + " §7(" + percent + "%)");
            }
        }
    }

    public void show(Player p) {
        DailyQuest q = quests.get(p.getUniqueId());
        if (q == null) { generateNewQuest(p.getUniqueId(), p); q = quests.get(p.getUniqueId()); }
        if (q == null) return;

        p.sendMessage(ChatColor.GOLD + "═════════ ЕЖЕДНЕВНОЕ ЗАДАНИЕ ════════");
        p.sendMessage(ChatColor.YELLOW + q.name);
        p.sendMessage(ChatColor.GRAY + "Прогресс: §f" + q.progress + "/" + q.amount);
        p.sendMessage(ChatColor.GREEN + "Награда: +" + q.reward + " монет" + (q.completed ? " §a✓ ВЫПОЛНЕНО!" : ""));
        p.sendMessage(ChatColor.GOLD + "══════════════════════════════════");
    }

    public static class DailyQuest {
        public String type, target, name;
        public int amount, progress, reward;
        public boolean completed;
        public long day;
        public DailyQuest(String t, String tg, String n, int a, int r, int p, boolean c, long d) {
            type=t; target=tg; name=n; amount=a; reward=r; progress=p; completed=c; day=d;
        }
    }
    static class QuestTemplate {
        String type, target, name;
        int amount, reward;
        QuestTemplate(String t, String tg, String n, int a, int r) { type=t; target=tg; name=n; amount=a; reward=r; }
    }
}