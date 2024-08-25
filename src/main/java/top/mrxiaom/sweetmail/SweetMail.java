package top.mrxiaom.sweetmail;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.sweetmail.attachments.AttachmentCommand;
import top.mrxiaom.sweetmail.attachments.AttachmentItem;
import top.mrxiaom.sweetmail.attachments.AttachmentMoney;
import top.mrxiaom.sweetmail.attachments.IAttachment;
import top.mrxiaom.sweetmail.database.MailDatabase;
import top.mrxiaom.sweetmail.database.entry.*;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;
import top.mrxiaom.sweetmail.func.basic.GuiManager;
import top.mrxiaom.sweetmail.func.basic.TextHelper;
import top.mrxiaom.sweetmail.utils.StringHelper;
import top.mrxiaom.sweetmail.utils.Util;

import java.util.ArrayList;
import java.util.List;

import static top.mrxiaom.sweetmail.func.AbstractPluginHolder.reloadAllConfig;

public class SweetMail extends JavaPlugin implements Listener, TabCompleter, PluginMessageListener {
    private static SweetMail instance;

    public static SweetMail getInstance() {
        return instance;
    }
    public static void warn(Throwable t) {
        warn(StringHelper.stackTraceToString(t));
    }
    public static void warn(String s) {
        getInstance().getLogger().warning(s);
    }
    private TextHelper textHelper = null;
    private GuiManager guiManager = null;
    private MailDatabase database = null;
    private Economy economy;

    public TextHelper text() {
        return textHelper;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public MailDatabase getMailDatabase() {
        return database;
    }

    @NotNull
    public Economy getEconomy() {
        return economy;
    }
    private String prefix;
    public String prefix() {
        return prefix;
    }
    private boolean onlineMode;
    public boolean isOnlineMode() {
        return onlineMode;
    }
    @Override
    public void onEnable() {
        Util.init(instance = this);

        loadHooks();
        loadFunctions();
        loadBuiltInAttachments();
        reloadConfig();

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

        IMail.instance = new MailAPI();
        getLogger().info("SweetMail 加载完毕");
    }

    public void loadFunctions() {
        AbstractPluginHolder.loadModules(this);
        textHelper = new TextHelper(this);
        guiManager = new GuiManager(this);
        database = new MailDatabase(this).reload();
    }

    public void loadHooks() {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
    }

    private void loadBuiltInAttachments() {
        IAttachment.deserializers.add(AttachmentItem::deserialize);
        IAttachment.deserializers.add(AttachmentCommand::deserialize);
        IAttachment.deserializers.add(AttachmentMoney::deserialize);
    }

    @Override
    public void onDisable() {
        AbstractPluginHolder.callDisable();
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
        HandlerList.unregisterAll((Plugin) this);
        Bukkit.getScheduler().cancelTasks(this);
        Util.onDisable();
    }

    @Override
    @SuppressWarnings({"all"})
    public void onPluginMessageReceived(String channel, @NotNull Player player, @NotNull byte[] message) {
        if (!channel.equals("BungeeCord")) return;
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();
        short len = in.readShort();
        byte[] bytes = new byte[len];
        in.readFully(bytes);
        AbstractPluginHolder.receiveFromBungee(subChannel, bytes);
    }

    @Override
    public void reloadConfig() {
        this.saveDefaultConfig();
        super.reloadConfig();

        FileConfiguration config = getConfig();
        this.prefix = config.getString("messages.prefix", "");
        String online = config.getString("online-mode", "auto").toLowerCase();
        switch (online) {
            case "true":
                onlineMode = true;
                break;
            case "false":
                onlineMode = false;
                break;
            case "auto":
            default:
                onlineMode = Bukkit.getOnlineMode();
                break;
        }
        getLogger().info("插件当前在 " + (onlineMode ? "在线模式": "离线模式") + " 下运行");
        reloadAllConfig(config);
    }

    private class MailAPI extends IMail {
        private MailAPI() {
        }
        @Override
        protected Status send(MailDraft draft) {
            List<String> receivers = draft.getReceivers();
            if (receivers.isEmpty()) {
                return Status.EMPTY_RECEIVER;
            }
            MailDatabase db = getMailDatabase();

            String uuid = db.generateMailUUID();
            String sender = draft.getSender();
            String senderDisplay = draft.getSenderDisplay();
            String icon = draft.getIcon();
            String title = draft.getTitle();
            List<String> content = draft.getContent();
            List<IAttachment> attachments = new ArrayList<>();
            for (IAttachment attachment : draft.getAttachments()) {
                if (attachment.isLegal()) {
                    attachments.add(attachment);
                }
            }

            Mail mail = new Mail(uuid, sender, senderDisplay, icon, receivers, title, content, attachments);
            db.sendMail(mail);
            return Status.SUCCESS;
        }
    }
}
