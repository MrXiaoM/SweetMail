package top.mrxiaom.sweetmail.ext.email;

import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.sweetmail.IMail;
import top.mrxiaom.sweetmail.Messages;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.events.MailSentEvent;
import top.mrxiaom.sweetmail.ext.email.adapters.AdapterEmailer;
import top.mrxiaom.sweetmail.ext.email.adapters.IMailAdapter;
import top.mrxiaom.sweetmail.ext.email.adapters.NoAdapter;
import top.mrxiaom.sweetmail.ext.email.perm.IPermissionProvider;
import top.mrxiaom.sweetmail.ext.email.perm.NoProvider;
import top.mrxiaom.sweetmail.ext.email.perm.PermLuckPerms;
import top.mrxiaom.sweetmail.ext.email.perm.PermVault;
import top.mrxiaom.sweetmail.utils.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class SweetMailNotice extends JavaPlugin implements Listener {
    private IMailAdapter adapter;
    private IPermissionProvider perm;
    private EmailerTemplate emailerTemplate;

    @Override
    public void onEnable() {
        if (hasPlugin("Emailer")) {
            this.adapter = new AdapterEmailer(this);
        } else {
            this.adapter = NoAdapter.INSTANCE;
        }

        if (this.perm == null && hasPlugin("LuckPerms")) {
            LuckPerms luckPerms = getServiceProvider(LuckPerms.class);
            if (luckPerms != null) {
                this.perm = new PermLuckPerms(luckPerms);
            }
        }
        if (this.perm == null && hasPlugin("Vault")) {
            Permission vault = getServiceProvider(Permission.class);
            if (vault != null) {
                this.perm = new PermVault(vault);
            }
        }
        if (this.perm == null) {
            this.perm = NoProvider.INSTANCE;
            getLogger().warning("没有发现支持的权限插件，所有人都可以接收电子邮件");
        } else {
            getLogger().info("已挂钩权限插件 " + this.perm.getName());
        }

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    private static <T> T getServiceProvider(Class<T> type) {
        ServicesManager manager = Bukkit.getServicesManager();
        RegisteredServiceProvider<T> provider = manager.getRegistration(type);
        return provider == null ? null : provider.getProvider();
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
        Executor executor = runnable -> Bukkit.getScheduler().runTaskAsynchronously(this, runnable);
        CompletableFuture<Boolean> future;
        if (mail.sender.equals(IMail.SERVER_SENDER)) {
            future = CompletableFuture.completedFuture(true);
        } else {
            OfflinePlayer player = Util.getOfflinePlayerByNameOrUUID(mail.sender).orElse(null);
            if (player != null) {
                future = perm.has(player, "sweet.mail.notice.send.email", executor);
            } else {
                future = CompletableFuture.completedFuture(false);
            }
        }
        CompletableFuture<Void> future1 = future.thenAcceptAsync(it -> {
            if (!it) return;
            List<OfflinePlayer> players = new ArrayList<>();
            for (String receiverId : mail.receivers) {
                OfflinePlayer player = Util.getOfflinePlayerByNameOrUUID(receiverId).orElse(null);
                if (player != null) {
                    if (perm.has(player, "sweet.mail.notice.receive.email", executor).join()) {
                        players.add(player);
                    }
                }
            }
            adapter.sendMailNotice(mail, players);
        }, executor);
        executor.execute(future1::join);
    }
}
