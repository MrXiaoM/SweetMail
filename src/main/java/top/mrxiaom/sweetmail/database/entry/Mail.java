package top.mrxiaom.sweetmail.database.entry;

import com.google.gson.*;
import net.kyori.adventure.inventory.Book;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.attachments.IAttachment;
import top.mrxiaom.sweetmail.depend.PAPI;
import top.mrxiaom.sweetmail.func.NoticeManager;
import top.mrxiaom.sweetmail.utils.ItemStackUtil;
import top.mrxiaom.sweetmail.utils.Util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class Mail {
    public final String uuid;
    public final String sender;
    public final String senderDisplay;
    public final String icon;
    public final List<String> receivers;
    public final String title;
    public final List<String> content;
    public final List<IAttachment> attachments;
    public final boolean enablePlaceholders;
    public final long outdateTime;

    public Mail(
            String uuid,
            String sender,
            String senderDisplay,
            String icon,
            List<String> receivers,
            String title,
            List<String> content,
            List<IAttachment> attachments,
            long outdateTime
    ) {
        this(uuid, sender, senderDisplay, icon, receivers, title, content, attachments, false, outdateTime);
    }
    public Mail(
            String uuid,
            String sender,
            String senderDisplay,
            String icon,
            List<String> receivers,
            String title,
            List<String> content,
            List<IAttachment> attachments,
            boolean enablePlaceholders,
            long outdateTime
    ) {
        this.uuid = uuid;
        this.sender = sender;
        this.senderDisplay = senderDisplay;
        this.icon = icon;
        this.receivers = receivers;
        this.title = title;
        this.content = content;
        this.attachments = attachments;
        this.enablePlaceholders = enablePlaceholders;
        this.outdateTime = outdateTime;
    }

    @Deprecated
    public ItemStack generateIcon() {
        return generateIcon(null);
    }

    public ItemStack generateIcon(Player player) {
        return ItemStackUtil.getItem(player, icon);
    }

    @Deprecated
    public Book generateBook() {
        return generateBook(null);
    }
    public Book generateBook(Player player) {
        String author = senderDisplay == null || senderDisplay.isEmpty()
                ? Util.getPlayerName(sender)
                : senderDisplay;
        List<String> content = new ArrayList<>();
        if (enablePlaceholders) {
            content.addAll(PAPI.setPlaceholders(player, this.content));
        } else {
            content.addAll(this.content);
        }
        return Util.legacyBook(PAPI.setPlaceholders(player, content), author);
    }

    public void noticeSent() {
        NoticeManager.inst().noticeNew(receivers);
    }

    public boolean isOutdated() {
        return outdateTime > 0 && Util.toTimestamp(LocalDateTime.now()) >= outdateTime;
    }

    public String serialize() {
        JsonObject json = new JsonObject();

        json.addProperty("uuid", uuid);
        json.addProperty("sender", sender);
        json.addProperty("senderDisplay", senderDisplay);
        json.addProperty("icon", icon);
        json.addProperty("title", title);

        JsonArray receiversArray = new JsonArray();
        for (String s : receivers) {
            receiversArray.add(new JsonPrimitive(s));
        }
        json.add("receivers", receiversArray);

        JsonArray contentArray = new JsonArray();
        for (String s : content) {
            contentArray.add(new JsonPrimitive(s));
        }
        json.add("content", contentArray);

        JsonArray attachmentsArray = new JsonArray();
        for (IAttachment attachment : attachments) {
            attachmentsArray.add(new JsonPrimitive(attachment.serialize()));
        }
        json.add("attachments", attachmentsArray);
        json.addProperty("outdateTime", outdateTime);

        return json.toString();
    }

    @SuppressWarnings({"deprecation"})
    private static <T> T deserialize(String jsonString, MailDeserializer<String, String, String, String, List<String>, String, List<String>, List<IAttachment>, Boolean, Long, T> func) {
        // 不要更改旧版 gson 用法，低版本 Minecraft 并没有新版本 gson
        JsonObject json = new JsonParser().parse(jsonString).getAsJsonObject();

        String uuid = getString(json, "uuid");
        String sender = getString(json, "sender");
        String senderDisplay = getString(json, "senderDisplay");
        String icon = getString(json, "icon");
        String title = getString(json, "title");
        boolean enablePlaceholders = getBoolean(json, "enablePlaceholders", false);
        Long outdate = getLong(json, "outdateTime", null);
        long outdateTime = outdate == null ? 0 : outdate;

        List<String> receivers = getList(json, "receivers");
        List<String> content = getList(json, "content");
        List<IAttachment> attachments = getList(json, "attachments", IAttachment::deserialize);
        return func.create(uuid, sender, senderDisplay, icon, receivers, title, content, attachments, enablePlaceholders, outdateTime);
    }

    public static Mail deserialize(String s) {
        return deserialize(s, Mail::new);
    }

    public static MailWithStatus deserialize(String s, LocalDateTime time, boolean read, boolean used) {
        MailWithStatus mail = deserialize(s, MailWithStatus::new);
        mail.time = time;
        mail.read = read;
        mail.used = used;
        return mail;
    }

    private static String getString(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (element == null) throw new JsonParseException("Can't find " + key);
        return element.getAsString();
    }

    @SuppressWarnings("SameParameterValue")
    private static Boolean getBoolean(JsonObject json, String key, @Nullable Boolean defaultValue) {
        JsonElement element = json.get(key);
        return element == null ? defaultValue : Boolean.valueOf(element.getAsBoolean());
    }

    @SuppressWarnings("SameParameterValue")
    private static Long getLong(JsonObject json, String key, @Nullable Long defaultValue) {
        JsonElement element = json.get(key);
        return element == null ? defaultValue : Long.valueOf(element.getAsLong());
    }

    private static List<String> getList(JsonObject json, String key) {
        return getList(json, key, it -> it);
    }
    private static <T> List<T> getList(JsonObject json, String key, Function<String, T> transformer) {
        JsonElement element = json.get(key);
        if (element == null || !element.isJsonArray()) throw new JsonParseException("Can't find list " + key);
        List<T> list = new ArrayList<>();
        JsonArray array = element.getAsJsonArray();
        for (JsonElement s : array) {
            T apply = transformer.apply(s.getAsString());
            if (apply != null) {
                list.add(apply);
            }
        }
        return Collections.unmodifiableList(list);
    }

    @FunctionalInterface
    private interface MailDeserializer<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> {
        R create(T1 var1, T2 var2, T3 var3, T4 var4, T5 var5, T6 var6, T7 var7, T8 var8, T9 var9, T10 var10);
    }
}
