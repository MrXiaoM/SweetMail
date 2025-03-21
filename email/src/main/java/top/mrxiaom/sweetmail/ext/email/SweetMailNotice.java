package top.mrxiaom.sweetmail.ext.email;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.sweetmail.Messages;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.events.MailSentEvent;
import top.mrxiaom.sweetmail.ext.email.adapters.AdapterEmailer;
import top.mrxiaom.sweetmail.ext.email.adapters.IMailAdapter;
import top.mrxiaom.sweetmail.ext.email.adapters.NoAdapter;
import top.mrxiaom.sweetmail.utils.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SweetMailNotice extends JavaPlugin implements Listener {
    private IMailAdapter adapter;
    private EmailerTemplate emailerTemplate;

    @Override
    public void onEnable() {
        if (hasPlugin("Emailer")) {
            this.adapter = new AdapterEmailer(this);
        } else {
            this.adapter = NoAdapter.INSTANCE;
        }

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    public EmailerTemplate getEmailerTemplate() {
        return emailerTemplate;
    }

    @Override
    public void reloadConfig() {
        saveDefaultConfig();
        super.reloadConfig();
        File emailerFolder = new File(getDataFolder(), "emailer");
        if (!emailerFolder.exists()) {
            Util.mkdirs(emailerFolder);
            saveResource("emailer/mail.html", true);
        }
        FileConfiguration config = getConfig();
        EmailerTemplate template = EmailerTemplate.load(this, config);
        if (template != null) {
            emailerTemplate = template;
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender.isOp()) {
            reloadConfig();
            Messages.Command.reload.tm(sender);
        }
        return true;
    }

    @SuppressWarnings("SameParameterValue")
    private boolean hasPlugin(String pluginName) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMailSend(MailSentEvent e) {
        Mail mail = e.getMail();
        List<OfflinePlayer> players = new ArrayList<>();
        for (String receiverId : mail.receivers) {
            Util.getOfflinePlayerByNameOrUUID(receiverId).ifPresent(players::add);
        }
        adapter.sendMailNotice(mail, players);
    }
}
