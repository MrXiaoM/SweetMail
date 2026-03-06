package top.mrxiaom.sweetmail.players.providers;

import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.Nullable;
import top.mrxiaom.sweetmail.players.AbstractPlayerListProvider;
import top.mrxiaom.sweetmail.players.IPlayerList;
import top.mrxiaom.sweetmail.players.builtin.PlayerListInTime;
import top.mrxiaom.sweetmail.utils.Util;

public class ProviderInTime extends AbstractPlayerListProvider {

    private static Long parseDateTime(String str) {
        if (str == null) {
            return null;
        }
        Long fromFormat = PlayerListInTime.parseTime(str);
        if (fromFormat != null) {
            return fromFormat;
        }
        return Util.parseLong(str).orElse(null);
    }
    @Override
    public @Nullable IPlayerList fromString(String str) {
        if (str.startsWith("last played in ")) {
            // 从什么时间到现在，上过线的玩家
            Long timeRaw = parseDateTime(str.substring(15));
            if (timeRaw != null) {
                return new PlayerListInTime(timeRaw);
            }
        }
        if (str.startsWith("last played from ")) {
            // 在某段时间区间内，上过线的玩家
            String input = str.substring(17);
            String[] split = input.contains(" to ") ? input.split(" to ", 2) : new String[] { str };
            if (split.length == 2) {
                Long fromTime = parseDateTime(split[0]);
                Long toTime = parseDateTime(split[1]);
                if (fromTime != null && toTime != null) {
                    return new PlayerListInTime(fromTime, toTime);
                }
            }
        }
        return null;
    }

    @Override
    public @Nullable IPlayerList fromConfig(ConfigurationSection config) {
        if ("last-played".equalsIgnoreCase(config.getString("type"))) {
            String fromStr = config.getString("from");
            String toStr = config.getString("to");
            if (fromStr != null || toStr != null) {
                Long from = parseDateTime(fromStr);
                Long to = parseDateTime(toStr);
                if (fromStr != null && from == null) return null;
                if (toStr != null && to == null) return null;
                return new PlayerListInTime(from, to);
            }
        }
        return super.fromConfig(config);
    }
}
