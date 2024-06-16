package top.mrxiaom.sweetmail.utils.comp;

import com.google.common.collect.Lists;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;
import top.mrxiaom.sweetmail.utils.Util;

import java.util.List;

public class PAPI {
    private static boolean isEnabled = false;
    public static void init() {
        isEnabled = Util.isPresent("me.clip.placeholderapi.PlaceholderAPI");
    }
    public static String setPlaceholders(OfflinePlayer player, String s) {
        if (!isEnabled) return player == null ? s : s.replace("%player_name%", String.valueOf(player.getName()));
        return PlaceholderAPI.setPlaceholders(player, s);
    }
    public static List<String> setPlaceholders(OfflinePlayer player, List<String> s) {
        if (!isEnabled) return player == null ? s : Lists.newArrayList(String.join("\n", s).replace("%player_name%", String.valueOf(player.getName())).split("\n"));
        return PlaceholderAPI.setPlaceholders(player, s);
    }
}
