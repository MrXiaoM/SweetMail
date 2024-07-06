package top.mrxiaom.sweetmail.utils;

import com.google.common.io.ByteArrayDataOutput;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.sweetmail.utils.comp.IA;
import top.mrxiaom.sweetmail.utils.comp.Mythic;
import top.mrxiaom.sweetmail.utils.comp.PAPI;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

import static top.mrxiaom.sweetmail.utils.Pair.replace;

@SuppressWarnings({"unused"})
public class Util {
    public static Map<String, OfflinePlayer> players = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public static void init(JavaPlugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                if (player.getName() != null) {
                    players.put(player.getName(), player);
                }
            }
        });
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent e) {
                players.put(e.getPlayer().getName(), e.getPlayer());
            }
        }, plugin);
        PAPI.init();
        IA.init();
        Mythic.load();
        ItemStackUtil.init();
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
        return getOnlinePlayer(s).map(HumanEntity::getName).orElse(s);
    }

    public static Optional<OfflinePlayer> getOfflinePlayer(String name) {
        return Optional.ofNullable(players.get(name));
    }

    public static Optional<Player> getOnlinePlayer(String name) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) return Optional.of(player);
        }
        return Optional.empty();
    }

    public static List<Player> getOnlinePlayers(List<UUID> uuidList) {
        List<Player> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (uuidList.contains(player.getUniqueId())) players.add(player);
        }
        return players;
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
