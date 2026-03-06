package top.mrxiaom.sweetmail.players.builtin;

import com.ezylang.evalex.Expression;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.NonNull;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.depend.PAPI;
import top.mrxiaom.sweetmail.players.IPlayerList;
import top.mrxiaom.sweetmail.utils.Util;

import java.util.List;

public class PlayerListExpression implements IPlayerList {
    private final SweetMail plugin = SweetMail.getInstance();
    private final boolean includeOffline;
    private final String expression;
    public PlayerListExpression(boolean includeOffline, String expression) {
        this.includeOffline = includeOffline;
        this.expression = expression;
    }

    @Override
    public @NonNull List<OfflinePlayer> getPlayers() {
        List<OfflinePlayer> list = includeOffline
                ? Util.getOfflinePlayers()
                : Lists.newArrayList(Bukkit.getOnlinePlayers());
        list.removeIf(player -> {
            if (player == null) return true;
            String name = player.getName();
            if (name == null) return true;
            String str = PAPI.setPlaceholders(player, expression);
            try {
                return new Expression(str).evaluate().getBooleanValue() != Boolean.TRUE;
            } catch (Exception e) {
                plugin.warn("为玩家 " + name + " 解析表达式 " + str + " 时出现异常: " + e.getMessage());
                return true;
            }
        });
        return list;
    }

    @Override
    public boolean isLagTask() {
        return true;
    }

    @Override
    public @NonNull String toLegacyString() {
        return "eval " + (includeOffline ? "offline" : "online") + " " + expression;
    }

    @Override
    public void toConfig(@NonNull ConfigurationSection config) {
        config.set("type", "eval");
        config.set("include-offline", includeOffline);
        config.set("expression", expression);
    }
}
