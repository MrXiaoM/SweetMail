package top.mrxiaom.sweetmail.utils;

import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataOutput;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.utils.comp.IA;
import top.mrxiaom.sweetmail.utils.comp.Mythic;
import top.mrxiaom.sweetmail.utils.comp.PAPI;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

import static top.mrxiaom.sweetmail.utils.Pair.replace;

@SuppressWarnings({"unused"})
public class Util {
    private static BukkitAudiences adventure;
    private static MiniMessage miniMessage;
    public static Map<String, OfflinePlayer> players = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public static Map<String, OfflinePlayer> playersByUUID = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public static void init(JavaPlugin plugin) {
        adventure = BukkitAudiences.builder(plugin).build();
        miniMessage = MiniMessage.builder().build();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                if (player.getName() != null) {
                    players.put(player.getName(), player);
                    players.put(player.getUniqueId().toString(), player);
                }
            }
        });
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent e) {
                players.put(e.getPlayer().getName(), e.getPlayer());
                players.put(e.getPlayer().getUniqueId().toString(), e.getPlayer());
            }
        }, plugin);
        PAPI.init();
        IA.init();
        Mythic.load();
        ItemStackUtil.init();
    }

    public static Component miniMessage(String s) {
        return s == null ? Component.empty() :  miniMessage.deserialize(legacyToMiniMessage(s));
    }

    public static void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        adventure.player(player).showTitle(Title.title(
                miniMessage(title), miniMessage(subTitle), Title.Times.times(
                        Duration.ofMillis(fadeIn * 50L),
                        Duration.ofMillis(stay * 50L),
                        Duration.ofMillis(fadeOut * 50L)
                )
        ));
    }

    public static void openBook(Player player, ItemStack book) {
        if (book.getType().equals(Material.WRITTEN_BOOK)) return;
        player.openBook(book); // TODO: 兼容 1.8 等 player 没有 openBook 的版本
    }

    @SafeVarargs
    public static void runCommands(Player player, List<String> list, Pair<String, Object>... replacements) {
        for (String s : ColorHelper.parseColor(PAPI.setPlaceholders(player, replace(list, replacements)))) {
            if (s.startsWith("[console]")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.substring(9));
            }
            if (s.startsWith("[player]")) {
                Bukkit.dispatchCommand(player, s.substring(8));
            }
            if (s.startsWith("[message]")) {
                player.sendMessage(s.substring(9));
            }
        }
    }

    public static ByteArrayDataOutput newDataOutput() {
        return new ByteArrayDataOutputStream(new ByteArrayOutputStream());
    }

    public static String getPlayerName(String s) {
        if (SweetMail.getInstance().isOnlineMode()) {
            OfflinePlayer offline = playersByUUID.get(s);
            return offline == null ? s : offline.getName() == null ? s : offline.getName();
        }
        return s;
    }

    public static List<OfflinePlayer> getOfflinePlayers() {
        return Lists.newArrayList(players.values());
    }

    public static Optional<OfflinePlayer> getOfflinePlayer(String name) {
        return Optional.ofNullable(players.get(name));
    }

    public static Optional<OfflinePlayer> getOfflinePlayerByNameOrUUID(String s) {
        return Optional.ofNullable((SweetMail.getInstance().isOnlineMode() ? playersByUUID : players).get(s));
    }

    public static Optional<Player> getOnlinePlayer(String name) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) return Optional.of(player);
        }
        return Optional.empty();
    }

    public static Optional<Player> getOnlinePlayerByNameOrUUID(String name) {
        boolean online = SweetMail.getInstance().isOnlineMode();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (online) {
                if (player.getUniqueId().toString().equals(name)) return Optional.of(player);
            } else {
                if (player.getName().equalsIgnoreCase(name)) return Optional.of(player);
            }
        }
        return Optional.empty();
    }

    public static Player getAnyPlayerOrNull() {
        Iterator<? extends Player> i = Bukkit.getOnlinePlayers().iterator();
        return i.hasNext() ? i.next() : null;
    }

    public static Optional<Double> parseDouble(String s) {
        if (s == null) return Optional.empty();
        try {
            return Optional.of(Double.parseDouble(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static Optional<Integer> parseInt(String s) {
        if (s == null) return Optional.empty();
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static Optional<Long> parseLong(String s) {
        if (s == null) return Optional.empty();
        try {
            return Optional.of(Long.parseLong(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static <T extends Enum<?>> T valueOr(Class<T> c, String s, T def) {
        if (s == null) return def;
        for (T t : c.getEnumConstants()) {
            if (t.name().equalsIgnoreCase(s)) return t;
        }
        return def;
    }

    public static boolean isPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public static String legacyToMiniMessage(String legacy) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] chars = legacy.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (!isColorCode(chars[i])) {
                stringBuilder.append(chars[i]);
                continue;
            }
            if (i + 1 >= chars.length) {
                stringBuilder.append(chars[i]);
                continue;
            }
            switch (chars[i+1]) {
                case '0': stringBuilder.append("<black>"); break;
                case '1': stringBuilder.append("<dark_blue>"); break;
                case '2': stringBuilder.append("<dark_green>"); break;
                case '3': stringBuilder.append("<dark_aqua>"); break;
                case '4': stringBuilder.append("<dark_red>"); break;
                case '5': stringBuilder.append("<dark_purple>"); break;
                case '6': stringBuilder.append("<gold>"); break;
                case '7': stringBuilder.append("<gray>"); break;
                case '8': stringBuilder.append("<dark_gray>"); break;
                case '9': stringBuilder.append("<blue>"); break;
                case 'a': stringBuilder.append("<green>"); break;
                case 'b': stringBuilder.append("<aqua>"); break;
                case 'c': stringBuilder.append("<red>"); break;
                case 'd': stringBuilder.append("<light_purple>"); break;
                case 'e': stringBuilder.append("<yellow>"); break;
                case 'f': stringBuilder.append("<white>"); break;
                case 'r': stringBuilder.append("<r><!i>"); break;
                case 'l': stringBuilder.append("<b>"); break;
                case 'm': stringBuilder.append("<st>"); break;
                case 'o': stringBuilder.append("<i>"); break;
                case 'n': stringBuilder.append("<u>"); break;
                case 'k': stringBuilder.append("<o>"); break;
                case 'x': {
                    if (i + 13 >= chars.length
                            || !isColorCode(chars[i+2])
                            || !isColorCode(chars[i+4])
                            || !isColorCode(chars[i+6])
                            || !isColorCode(chars[i+8])
                            || !isColorCode(chars[i+10])
                            || !isColorCode(chars[i+12])) {
                        stringBuilder.append(chars[i]);
                        continue;
                    }
                    stringBuilder
                            .append("<#")
                            .append(chars[i+3])
                            .append(chars[i+5])
                            .append(chars[i+7])
                            .append(chars[i+9])
                            .append(chars[i+11])
                            .append(chars[i+13])
                            .append(">");
                    i += 12;
                    break;
                }
                default: {
                    stringBuilder.append(chars[i]);
                    continue;
                }
            }
            i++;
        }
        return stringBuilder.toString();
    }
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isColorCode(char c) {
        return c == '§' || c == '&';
    }

    public static class ByteArrayDataOutputStream implements ByteArrayDataOutput {
        final DataOutput output;
        final ByteArrayOutputStream byteArrayOutputStream;

        ByteArrayDataOutputStream(ByteArrayOutputStream byteArrayOutputStream) {
            this.byteArrayOutputStream = byteArrayOutputStream;
            this.output = new DataOutputStream(byteArrayOutputStream);
        }

        public void write(int b) {
            try {
                this.output.write(b);
            } catch (IOException var3) {
                throw new AssertionError(var3);
            }
        }

        public void write(@NotNull byte[] b) {
            try {
                this.output.write(b);
            } catch (IOException var3) {
                throw new AssertionError(var3);
            }
        }

        public void write(@NotNull byte[] b, int off, int len) {
            try {
                this.output.write(b, off, len);
            } catch (IOException var5) {
                throw new AssertionError(var5);
            }
        }

        public void writeBoolean(boolean v) {
            try {
                this.output.writeBoolean(v);
            } catch (IOException var3) {
                throw new AssertionError(var3);
            }
        }

        public void writeByte(int v) {
            try {
                this.output.writeByte(v);
            } catch (IOException var3) {
                throw new AssertionError(var3);
            }
        }

        public void writeBytes(@NotNull String s) {
            try {
                this.output.writeBytes(s);
            } catch (IOException var3) {
                throw new AssertionError(var3);
            }
        }

        public void writeChar(int v) {
            try {
                this.output.writeChar(v);
            } catch (IOException var3) {
                throw new AssertionError(var3);
            }
        }

        public void writeChars(@NotNull String s) {
            try {
                this.output.writeChars(s);
            } catch (IOException var3) {
                throw new AssertionError(var3);
            }
        }

        public void writeDouble(double v) {
            try {
                this.output.writeDouble(v);
            } catch (IOException var4) {
                throw new AssertionError(var4);
            }
        }

        public void writeFloat(float v) {
            try {
                this.output.writeFloat(v);
            } catch (IOException var3) {
                throw new AssertionError(var3);
            }
        }

        public void writeInt(int v) {
            try {
                this.output.writeInt(v);
            } catch (IOException var3) {
                throw new AssertionError(var3);
            }
        }

        public void writeLong(long v) {
            try {
                this.output.writeLong(v);
            } catch (IOException var4) {
                throw new AssertionError(var4);
            }
        }

        public void writeShort(int v) {
            try {
                this.output.writeShort(v);
            } catch (IOException var3) {
                throw new AssertionError(var3);
            }
        }

        public void writeUTF(@NotNull String s) {
            try {
                this.output.writeUTF(s);
            } catch (IOException var3) {
                throw new AssertionError(var3);
            }
        }

        @NotNull
        public byte[] toByteArray() {
            return this.byteArrayOutputStream.toByteArray();
        }
    }
}
