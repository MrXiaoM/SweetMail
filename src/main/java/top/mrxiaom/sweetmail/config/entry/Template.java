package top.mrxiaom.sweetmail.config.entry;

import com.google.common.collect.Lists;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import top.mrxiaom.sweetmail.IMail;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.attachments.IAttachment;
import top.mrxiaom.sweetmail.config.TemplateConfig;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.func.DraftManager;
import top.mrxiaom.sweetmail.func.data.Draft;
import top.mrxiaom.sweetmail.func.data.MailIcon;
import top.mrxiaom.sweetmail.utils.Args;
import top.mrxiaom.sweetmail.utils.Pair;
import top.mrxiaom.sweetmail.utils.Result;
import top.mrxiaom.sweetmail.utils.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static top.mrxiaom.sweetmail.utils.Pair.replace;

public class Template {
    public final String id;
    public final Map<String, MailVariable> variables;
    public final String senderDisplay;
    public final String icon;
    public final String title;
    public final List<String> content;
    public final List<String> rawAttachments;
    public final long outdateTimeSeconds;

    public Template(String id, Map<String, MailVariable> variables, String senderDisplay, String icon, String title, List<String> content, List<String> rawAttachments, long outdateTimeSeconds) {
        this.id = id;
        this.variables = variables;
        this.senderDisplay = senderDisplay;
        this.icon = icon;
        this.title = title;
        this.content = content;
        this.rawAttachments = rawAttachments;
        this.outdateTimeSeconds = outdateTimeSeconds;
    }

    public Result<Mail> createMail(String uuid, OfflinePlayer receiver, Args args) {
        return createMail(uuid, Lists.newArrayList(receiver), args);
    }

    public Result<Mail> createMail(String uuid, List<OfflinePlayer> receivers, Args args) {
        SweetMail plugin = SweetMail.getInstance();
        List<Pair<String, Object>> replacements = new ArrayList<>();
        for (MailVariable variable : variables.values()) {
            String value = args.get(variable.name, variable.defaultValue);
            if (value == null) {
                return Result.fail("未输入参数 " + variable.name + "，且该变量没有默认值");
            }
            if (variable.type.isNotValid(value)) {
                return Result.fail("参数 " + variable.name + " 的值无效");
            }
            replacements.add(Pair.of("${" + variable.name + "}", value));
        }

        String senderDisplay = replace(this.senderDisplay, replacements);
        String icon = "!" + replace(this.icon, replacements);
        List<String> receiverIds = new ArrayList<>();
        for (OfflinePlayer receiver : receivers) {
            receiverIds.add(plugin.getPlayerKey(receiver));
        }
        String title = replace(this.title, replacements);
        List<String> content = new ArrayList<>(replace(this.content, replacements));
        List<IAttachment> attachments = new ArrayList<>();
        List<String> parsedAttachments = replace(this.rawAttachments, replacements);
        for (int i = 0; i < parsedAttachments.size(); i++) {
            String str = parsedAttachments.get(i);
            if (str.startsWith("!")) continue;
            IAttachment attachment = IAttachment.deserialize(str);
            if (attachment == null) {
                return Result.fail("附件 [" + i + "] 读取失败，替换变量后的条目: " + str);
            }
            attachments.add(attachment);
        }
        long outdateTime = this.outdateTimeSeconds > 0L
                ? (Util.toTimestamp(now()) + (this.outdateTimeSeconds * 1000L))
                : 0L;

        return Result.success(new Mail(uuid, IMail.SERVER_SENDER, senderDisplay, icon, receiverIds, title, content, attachments, outdateTime));
    }

    public static void save(Player player, Draft draft, String id) {
        SweetMail plugin = SweetMail.getInstance();
        YamlConfiguration sample = new YamlConfiguration();
        InputStream sampleFile = plugin.getResource("templates/example.yml");
        if (sampleFile != null) {
            try (InputStream in = sampleFile;
                 InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                sample.load(reader);
            } catch (IOException | InvalidConfigurationException ignored) {
            }
        }
        File file = new File(plugin.getDataFolder(), "templates/" + id + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        config.createSection("variables");
        config.set("mail.sender", draft.advSenderDisplay != null
                ? draft.advSenderDisplay
                : "系统消息");
        MailIcon icon = DraftManager.inst().getMailIcon(draft.iconKey);
        config.set("mail.icon", icon == null
                ? draft.iconKey.substring(1)
                : icon.item);
        config.set("mail.title", draft.title);
        List<String> contents = draft.content;
        if (contents.isEmpty()) {
            config.createSection("mail.contents");
        } else for (int i = 0; i < contents.size(); i++) {
            List<String> page = Lists.newArrayList(contents.get(i).split("\n"));
            config.set("mail.contents." + (i + 1), page);
        }
        List<String> rawAttachments = new ArrayList<>();
        for (IAttachment attachment : draft.attachments) {
            rawAttachments.add(attachment.serialize());
        }
        config.set("mail.attachments", rawAttachments);
        String outdateTime = draft.outdateDays > 0
                ? (draft.outdateDays + "d")
                : "infinite";
        config.set("mail.outdate-time", outdateTime);

        setComments(config, sample, "variables", "mail",
                "mail.sender",
                "mail.icon",
                "mail.title",
                "mail.contents",
                "mail.attachments",
                "mail.outdate-time");
        try {
            config.save(file);
            plugin.info("已保存玩家 " + player.getName() + " 的草稿为邮件模板 " + id);
        } catch (IOException e) {
            plugin.warn("保存草稿到模板 (" + id + ") 时出现一个异常", e);
        }
    }

    private static void setComments(YamlConfiguration config, YamlConfiguration sample, String... keys) {
        try {
            for (String key : keys) {
                config.setComments(key, sample.getComments(key));
            }
        } catch (LinkageError ignored) { // 1.8 not support comments
        }
    }

    public static Template load(TemplateConfig parent, YamlConfiguration config, String id) {
        ConfigurationSection section;

        Map<String, MailVariable> variables = new HashMap<>();
        section = config.getConfigurationSection("variables");
        if (section != null) for (String name : section.getKeys(false)) {
            MailVariable.Type type = Util.valueOr(MailVariable.Type.class, section.getString(name + ".type"), null);
            if (type == null) {
                parent.warn("[template/" + id + "] 变量 " + name + " 的 type 无效");
                continue;
            }
            String defaultValue = section.getString(name + ".default", null);
            if (defaultValue != null && type.isNotValid(defaultValue)) {
                parent.warn("[template/" + id + "] 变量 " + name + " 的默认值无效");
                continue;
            }
            variables.put(name, new MailVariable(type, name, defaultValue));
        }
        String sender = config.getString("mail.sender", "系统消息");
        String icon = config.getString("mail.icon", "PAPER");
        String title = config.getString("mail.title", "未命名邮件");
        List<String> content = new ArrayList<>();
        section = config.getConfigurationSection("mail.contents");
        if (section != null) for (String key : section.getKeys(false)) {
            List<String> list = section.getStringList(key);
            content.add(String.join("\n", list));
        }
        List<String> rawAttachments = config.getStringList("mail.attachments");
        String outdateTimeString = config.getString("mail.outdate-time", "0");
        long outdateTimeSeconds;
        if (outdateTimeString.equals("0") || outdateTimeString.equals("infinite")) {
            outdateTimeSeconds = 0L;
        } else {
            Duration duration = Util.parseDuration(outdateTimeString);
            if (duration == null) {
                parent.warn("[template/" + id + "] 设定的邮件附件到期时间无效，已设为永久");
                outdateTimeSeconds = 0L;
            } else {
                outdateTimeSeconds = duration.getSeconds();
            }
        }

        return new Template(id, variables, sender, icon, title, content, rawAttachments, outdateTimeSeconds);
    }
}
