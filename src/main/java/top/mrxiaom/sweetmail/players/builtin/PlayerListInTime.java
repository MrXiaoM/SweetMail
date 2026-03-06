package top.mrxiaom.sweetmail.players.builtin;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import top.mrxiaom.sweetmail.players.IPlayerList;
import top.mrxiaom.sweetmail.utils.Util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class PlayerListInTime implements IPlayerList {
    private final @Nullable Long from, to;
    public PlayerListInTime(long from) {
        this.from = from;
        this.to = null;
    }
    public PlayerListInTime(@Nullable Long from, @Nullable Long to) {
        this.from = from;
        this.to = to;
    }

    @Override
    @SuppressWarnings("RedundantIfStatement")
    public @NonNull List<OfflinePlayer> getPlayers() {
        List<OfflinePlayer> list = Util.getOfflinePlayers();
        if (from != null || to != null) {
            getPlayers().removeIf(player -> {
                if (player == null || player.getName() == null) return true;
                long lastPlayed = player.getLastPlayed();
                if (from != null && lastPlayed < from) return true;
                if (to != null && lastPlayed >= to) return true;
                return false;
            });
        }
        return list;
    }

    @Override
    public boolean isLagTask() {
        return true;
    }

    @Override
    public @NonNull String toLegacyString() {
        if (from != null && to == null) {
            return "last played in " + format(from);
        }
        if (from != null /*&& to != null*/) {
            return "last played from " + format(from) + " to " + format(to);
        }
        return "";
    }

    @Override
    public void toConfig(@NonNull ConfigurationSection config) {
        config.set("type", "last-played");
        if (from != null) config.set("from", format(from));
        if (to != null) config.set("to", format(to));
    }

    @NotNull
    public static LocalTime parseLocalTimeOrZero(String str) {
        if (str == null) return LocalTime.of(0, 0, 0);
        String[] split = str.split(":", 3);
        int hour = split.length > 0 ? Util.parseInt(split[0]).orElse(0) : 0;
        int minute = split.length > 1 ? Util.parseInt(split[1]).orElse(0) : 0;
        int second = split.length > 2 ? Util.parseInt(split[2]).orElse(0) : 0;
        return LocalTime.of(hour, minute, second);
    }

    @Nullable
    public static Long parseTime(String s) {
        String[] split = s.split(" ", 2);
        String[] dateSplit = split[0].split("-", 3);
        if (dateSplit.length != 3) return null;
        Integer year = Util.parseInt(dateSplit[0]).orElse(null);
        Integer month = Util.parseInt(dateSplit[1]).orElse(null);
        Integer date = Util.parseInt(dateSplit[2]).orElse(null);
        if (year == null || month == null || date == null) return null;
        LocalDate localDate = LocalDate.of(year, month, date);
        LocalTime localTime = parseLocalTimeOrZero(split.length > 1 ? split[1] : null);
        LocalDateTime time = localDate.atTime(localTime);
        return Util.toTimestamp(time);
    }

    public static String format(long timestamp) {
        LocalDateTime dateTime = Util.fromTimestamp(timestamp);
        return dateTime.getYear() + "-" + dateTime.getMonthValue() + "-" + dateTime.getDayOfMonth()
                + " " + dateTime.getHour() + ":" + dateTime.getMinute() + ":" + dateTime.getSecond();
    }
}
