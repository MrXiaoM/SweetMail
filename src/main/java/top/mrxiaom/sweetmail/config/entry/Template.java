package top.mrxiaom.sweetmail.config.entry;

import com.google.common.collect.Lists;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import top.mrxiaom.sweetmail.IMail;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.attachments.IAttachment;
import top.mrxiaom.sweetmail.config.TemplateConfig;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.utils.Args;
import top.mrxiaom.sweetmail.utils.Pair;
import top.mrxiaom.sweetmail.utils.Result;
import top.mrxiaom.sweetmail.utils.Util;

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
            if (!variable.type.isValid(value)) {
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
        long outdateTime = Util.toTimestamp(now()) + (this.outdateTimeSeconds * 1000L);

        return Result.success(new Mail(uuid, IMail.SERVER_SENDER, senderDisplay, icon, receiverIds, title, content, attachments, outdateTime));
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
            if (defaultValue != null && !type.isValid(defaultValue)) {
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
        String outdateTimeString = config.getString("outdate-time", "0");
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
