package top.mrxiaom.sweetmail.ext.email;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.utils.Util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class EmailerTemplate {
    private final String subject, html, contentLine;

    public EmailerTemplate(String subject, String html, String contentLine) {
        this.subject = subject;
        this.html = html;
        this.contentLine = contentLine;
    }

    public String getSubject(Mail mail) {
        String sender = mail.senderDisplay.trim().isEmpty()
                ? Util.getPlayerName(mail.sender) : mail.senderDisplay;
        return subject.replace("%title%", mail.title)
                .replace("%sender%", sender);
    }

    public String save(JavaPlugin plugin, Mail mail) {
        String path = "generated/" + mail.uuid + ".html";
        File file = new File(plugin.getDataFolder(), path);
        Util.mkdirs(file.getParentFile());
        String sender = mail.senderDisplay.trim().isEmpty()
                ? Util.getPlayerName(mail.sender) : mail.senderDisplay;
        List<String> content = new ArrayList<>();

        for (String page : mail.content) {
            for (String s : page.split("\n")) {
                content.add(contentLine.replace("%text%", s));
            }
        }
        String toSave = html.replace("%title%", mail.title)
                .replace("%sender%", sender)
                .replace("%content%", String.join("\n", content.toString()));
        try (OutputStream output = Files.newOutputStream(file.toPath());
             OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8)) {
            writer.append(toSave);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "保存邮件(" + mail.uuid + ")的HTML文件时出现异常", e);
            return null;
        }

        return "../../" + plugin.getDataFolder().getName() + "/" + path;
    }

    public static EmailerTemplate load(SweetMailNotice plugin, FileConfiguration config) {
        String subject = config.getString("emailer-template.subject", "SweetMail: %title%");
        String htmlPath = config.getString("emailer-template.html", "");
        File htmlFile = new File(plugin.getDataFolder(), htmlPath);
        String html;
        try (InputStream input = Files.newInputStream(htmlFile.toPath());
             InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[1024];
            int len;
            if ((len = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, len);
            }
            html = sb.toString();
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "加载配置 emailer-template 时出错", e);
            return null;
        }
        String contentLine = config.getString("emailer-template.content-line", "<p>%text%</p>");
        return new EmailerTemplate(subject, html, contentLine);
    }
}
