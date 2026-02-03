package top.mrxiaom.sweetmail.utils;

import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataOutput;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTReflectionUtil;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import de.tr7zw.changeme.nbtapi.utils.nmsmappings.ReflectionMethod;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.sweetmail.Messages;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.depend.ItemsAdder;
import top.mrxiaom.sweetmail.depend.Mythic;
import top.mrxiaom.sweetmail.depend.PAPI;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Consumer;

public class Util {
    private static BukkitAudiences adventure;
    public static final Map<String, OfflinePlayer> players = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public static final Map<String, OfflinePlayer> playersByUUID = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public static void init(SweetMail plugin) {
        try {
            adventure = BukkitAudiences.builder(plugin).build();
            MiniMessageConvert.init();
        } catch (LinkageError e) {
            plugin.warn(plugin.getName() + " 的 adventure 相关库似乎出现了依赖冲突问题，请参考以下链接进行解决");
            plugin.warn("https://plugins.mcio.dev/elopers/base/resolver-override");
            throw e;
        }
        plugin.getScheduler().runTaskAsync(() -> {
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                if (player.getName() != null) {
                    players.put(player.getName(), player);
                    playersByUUID.put(player.getUniqueId().toString().replace("-", ""), player);
                }
            }
        });
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent e) {
                players.put(e.getPlayer().getName(), e.getPlayer());
                playersByUUID.put(e.getPlayer().getUniqueId().toString().replace("-", ""), e.getPlayer());
            }
        }, plugin);
        PAPI.init();
        ItemsAdder.init();
        Mythic.load();
        ItemStackUtil.init();
    }

    public static List<Character> toCharList(String s) {
        List<Character> list = new ArrayList<>();
        char[] array = s.toCharArray();
        for (char c : array) {
            list.add(c);
        }
        return list;
    }

    public static Duration parseDuration(String s) {
        try {
            s = s.toUpperCase().replace("D","DT");
            if (!s.contains("DT")) s = "T" + s;
            if (s.endsWith("T")) s = s.substring(0, s.length() - 1);
            return Duration.parse("P" + s);
        } catch (DateTimeParseException ignored){
            return null;
        }
    }

    public static String consumeString(String[] array, int startIndex) {
        if (startIndex >= array.length) return "";
        StringBuilder sb = new StringBuilder(array[startIndex]);
        for (int i = startIndex + 1; i < array.length; i++) {
            sb.append(" ").append(array[i]);
        }
        return sb.toString();
    }

    public static boolean mkdirs(File folder) {
        return folder.mkdirs();
    }

    public static void onDisable() {
        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
    }

    public static Component miniMessage(String s) {
        return MiniMessageConvert.miniMessage(s);
    }

    public static String miniMessage(Component s) {
        return MiniMessageConvert.miniMessage(s);
    }

    public static List<Component> toMiniMessage(List<String> list) {
        List<Component> components = new ArrayList<>();
        for (String s : list) {
            components.add(MiniMessageConvert.miniMessage(s));
        }
        return components;
    }

    public static Audience adventure(CommandSender sender) {
        // Paper
        if (sender instanceof Audience) {
            return (Audience) sender;
        }
        return adventure.sender(sender);
    }

    public static void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        adventure(player).showTitle(Title.title(
                miniMessage(title), miniMessage(subTitle), Title.Times.times(
                        Duration.ofMillis(fadeIn * 50L),
                        Duration.ofMillis(stay * 50L),
                        Duration.ofMillis(fadeOut * 50L)
                )
        ));
    }

    public static Book legacyBook(List<String> pages, String author) {
        List<Component> bookPages = pages.isEmpty()
                ? Lists.newArrayList(Component.empty())
                : Util.toMiniMessage(pages);
        return Book.builder()
                .title(Component.text("SweetMail"))
                .author(Component.text(author))
                .pages(bookPages)
                .build();
    }

    public static void openBook(Player player, Book book) {
        player.closeInventory();
        adventure(player).openBook(book);
    }

    @SuppressWarnings({"deprecation", "ConstantValue"})
    public static void openBookLegacy(Player player, Book book) {
        player.closeInventory();
        ItemStack bookItem = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta m = bookItem.getItemMeta();
        if (m instanceof BookMeta) {
            BookMeta meta = (BookMeta) m;
            LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
            meta.setTitle(legacy.serialize(book.title()));
            meta.setAuthor(legacy.serialize(book.author()));
            for (Component page : book.pages()) {
                meta.addPage(legacy.serialize(page));
            }
            bookItem.setItemMeta(meta);
            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_8_R3)) {
                player.openBook(bookItem);
            } else {
                if (player.getGameMode().equals(GameMode.CREATIVE)) {
                    ItemStack itemInHand = player.getItemInHand();
                    if (itemInHand != null && !itemInHand.getType().equals(Material.AIR)) {
                        Messages.legacy__1_7_10__need_empty_hand.tm(player);
                        return;
                    }
                }
                Util_v1_7_R4.openBook(SweetMail.getInstance(), player, bookItem);
                Messages.legacy__1_7_10__need_right_click.tm(player);
            }
        }
    }

    public static void sendMessage(CommandSender sender, String message) {
        adventure(sender).sendMessage(miniMessage(message));
    }

    public static void sendMessage(CommandSender sender, Component message) {
        adventure(sender).sendMessage(message);
    }

    public static void sendActionBar(Player player, String message) {
        adventure(player).sendActionBar(miniMessage(message));
    }

    @SuppressWarnings({"UnstableApiUsage"})
    public static void updateInventory(Player player) {
        player.updateInventory();
    }

    public static long toTimestamp(LocalDateTime time) {
        ZoneId zoneId = ZoneId.systemDefault();
        return time.toEpochSecond(zoneId.getRules().getOffset(Instant.EPOCH)) * 1000L;
    }

    public static LocalDateTime fromTimestamp(long timestampMills) {
        long seconds = timestampMills / 1000L;
        ZoneId zoneId = ZoneId.systemDefault();
        Instant instant = Instant.ofEpochSecond(seconds);
        return LocalDateTime.ofInstant(instant, zoneId.getRules().getOffset(Instant.EPOCH));
    }

    public static ByteArrayDataOutput newDataOutput() {
        return new ByteArrayDataOutputStream(new ByteArrayOutputStream());
    }

    public static String getPlayerName(String s) {
        if (SweetMail.getInstance().isOnlineMode()) {
            OfflinePlayer offline = playersByUUID.get(s);
            return offline == null || offline.getName() == null ? s : offline.getName();
        }
        return s;
    }

    public static List<String> getOfflinePlayers(String input) {
        List<String> list = new ArrayList<>();
        String s = input.toLowerCase();
        for (String player : players.keySet()) {
            if (player.toLowerCase().startsWith(s)) {
                list.add(player);
            }
        }
        return list;
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
                if (player.getUniqueId().toString().replace("-", "").equals(name)) return Optional.of(player);
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

    public static ItemStack modify(ItemStack item, Consumer<ReadWriteNBT> consumer) {
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_8_R3)) {
            NBT.modify(item, consumer::accept);
            return item;
        } else {
            Object nmsItem;
            nmsItem = ReflectionMethod.ITEMSTACK_NMSCOPY.run(null, item);
            NBTContainer nbt = NBTReflectionUtil.convertNMSItemtoNBTCompound(nmsItem);
            NBTCompound tag = nbt.getOrCreateCompound("tag");
            consumer.accept(tag);
            nmsItem = NBTReflectionUtil.convertNBTCompoundtoNMSItem(nbt);
            return  (ItemStack) ReflectionMethod.ITEMSTACK_BUKKITMIRROR.run(null, nmsItem);
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
