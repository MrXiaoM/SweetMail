package top.mrxiaom.sweetmail.database.entry;

import com.google.gson.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import top.mrxiaom.sweetmail.utils.ItemStackUtil;

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
    public Mail(String uuid, String sender, String senderDisplay, String icon, List<String> receivers, String title, List<String> content, List<IAttachment> attachments) {
        this.uuid = uuid;
        this.sender = sender;
        this.senderDisplay = senderDisplay;
        this.icon = icon;
        this.receivers = receivers;
        this.title = title;
        this.content = content;
        this.attachments = attachments;
    }

    public ItemStack generateIcon() {
        return ItemStackUtil.getItem(icon);
    }

    public ItemStack generateBook() {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta rawMeta = item.getItemMeta();
        if (rawMeta instanceof BookMeta) {
            BookMeta meta = (BookMeta) rawMeta;
            meta.setTitle(title);
            meta.setPages(content);
            meta.setAuthor(senderDisplay == null || senderDisplay.isEmpty() ? sender : senderDisplay);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void noticeSent() {
        // TODO: 提醒本服接收者查看邮件
        // TODO: 通过BC提醒其它服区的接收者查看邮件
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
            receiversArray.add(s);
        }
        json.add("receivers", receiversArray);

        JsonArray contentArray = new JsonArray();
        for (String s : content) {
            contentArray.add(s);
        }
        json.add("content", contentArray);

        JsonArray attachmentsArray = new JsonArray();
        for (IAttachment attachment : attachments) {
            attachmentsArray.add(attachment.serialize());
        }
        json.add("attachments", attachmentsArray);

        return json.toString();
    }

    private static <T> T deserialize(String s, Func8<String, String, String, String, List<String>, String, List<String>, List<IAttachment>, T> func) {
        JsonObject json = new JsonParser().parse(s).getAsJsonObject();

        String uuid = getString(json, "uuid");
        String sender = getString(json, "sender");
        String senderDisplay = getString(json, "senderDisplay");
        String icon = getString(json, "icon");
        String title = getString(json, "title");

        List<String> receivers = getList(json, "receivers");
        List<String> content = getList(json, "content");
        List<IAttachment> attachments = getList(json, "attachments", IAttachment::deserialize);
        return func.apply(uuid, sender, senderDisplay, icon, receivers, title, content, attachments);
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
    public interface Func8<T1, T2, T3, T4, T5, T6, T7, T8, R> {
        R apply(T1 var1, T2 var2, T3 var3, T4 var4, T5 var5, T6 var6, T7 var7, T8 var8);
    }
}
