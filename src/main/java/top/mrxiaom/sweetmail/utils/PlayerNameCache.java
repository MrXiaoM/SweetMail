package top.mrxiaom.sweetmail.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import top.mrxiaom.sweetmail.SweetMail;

import java.util.*;

public class PlayerNameCache implements Listener {
    private final SweetMail plugin;
    private final Set<String> playerNameCache = new HashSet<>();
    private boolean playerNameCacheLoaded = false;
    public PlayerNameCache(SweetMail plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        playerNameCache.add(e.getPlayer().getName());
    }

    public void clear() {
        playerNameCache.clear();
    }

    public void reset() {
        clear();
        playerNameCacheLoaded = false;
    }

    public List<String> getOfflinePlayerNames(String input, int limit) {
        if (!playerNameCacheLoaded) {
            playerNameCacheLoaded = true;
            // 异步延迟加载，首次获取不到结果没关系，onTabComplete 会持续请求这个方法，总有获取得到的时候
            plugin.getScheduler().runTaskAsync(() -> {
                for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
                    String name = p.getName();
                    if (name != null) {
                        playerNameCache.add(name);
                    }
                }
            });
        }
        List<String> result = new ArrayList<>();
        for (String name : playerNameCache) {
            if (name.toLowerCase().startsWith(input)) {
                result.add(name);
                if (limit > 0 && result.size() >= limit) {
                    break;
                }
            }
        }
        return result;
    }

    public Set<String> cached() {
        return Collections.unmodifiableSet(playerNameCache);
    }
}
