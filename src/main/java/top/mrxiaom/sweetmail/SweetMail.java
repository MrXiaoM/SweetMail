package top.mrxiaom.sweetmail;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.attachments.AttachmentCommand;
import top.mrxiaom.sweetmail.attachments.AttachmentItem;
import top.mrxiaom.sweetmail.attachments.AttachmentMoney;
import top.mrxiaom.sweetmail.attachments.IAttachment;
import top.mrxiaom.sweetmail.database.MailDatabase;
import top.mrxiaom.sweetmail.database.entry.*;
import top.mrxiaom.sweetmail.depend.Placeholder;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;
import top.mrxiaom.sweetmail.func.DraftManager;
import top.mrxiaom.sweetmail.func.TimerManager;
import top.mrxiaom.sweetmail.func.basic.GuiManager;
import top.mrxiaom.sweetmail.func.basic.TextHelper;
import top.mrxiaom.sweetmail.func.data.Draft;
import top.mrxiaom.sweetmail.utils.ClassLoaderWrapper;
import top.mrxiaom.sweetmail.utils.EconomyHolder;
import top.mrxiaom.sweetmail.utils.StringHelper;
import top.mrxiaom.sweetmail.utils.Util;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static top.mrxiaom.sweetmail.func.AbstractPluginHolder.reloadAllConfig;

public class SweetMail extends JavaPlugin implements Listener, TabCompleter, PluginMessageListener {
    private static SweetMail instance;

    public static SweetMail getInstance() {
        return instance;
    }
    public static void warn(Throwable t) {
        getInstance().warn(StringHelper.stackTraceToString(t));
    }
    public void info(String s) {
        getLogger().log(Level.INFO, s);
    }
    public void warn(String s) {
        getLogger().log(Level.WARNING, s);
    }
    public void warn(String msg, Throwable t) {
        getLogger().log(Level.WARNING, msg, t);
    }
    private TextHelper textHelper = null;
    private GuiManager guiManager = null;
    private MailDatabase database = null;
    private EconomyHolder economy;
    private final ClassLoaderWrapper classLoader;
    public final FoliaLib foliaLib;
    public SweetMail() {
        this.classLoader = new ClassLoaderWrapper((URLClassLoader) getClassLoader());
        this.foliaLib = new FoliaLib(this);
        loadLibraries();
    }

    public PlatformScheduler getScheduler() {
        return foliaLib.getScheduler();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void loadLibraries() {
        File librariesFolder = new File(getDataFolder(), "libraries");
        if (!librariesFolder.exists()) {
            librariesFolder.mkdirs();
            return;
        }
        File[] files = librariesFolder.listFiles();
        if (files != null) for (File file : files) {
            if (file.isDirectory()) continue;
            try {
                URL url = file.toURI().toURL();
                this.classLoader.addURL(url);
                info("已加载依赖库 " + file.getName());
            } catch (Throwable t) {
                warn("无法加载依赖库 " + file.getName(), t);
            }
        }
    }

    public TextHelper text() {
        return textHelper;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public MailDatabase getMailDatabase() {
        return database;
    }

    @Nullable
    public EconomyHolder getEconomy() {
        return economy;
    }
    private String prefix;
    public String prefix() {
        return prefix;
    }
    private boolean onlineMode;
    public int bundleMaxSlots;
    public boolean isOnlineMode() {
        return onlineMode;
    }
    private boolean checkCMI, checkEssentials;
    public String getPlayerKey(OfflinePlayer player) {
        if (player == null) return null;
        return SweetMail.getInstance().isOnlineMode()
                ? player.getUniqueId().toString().replace("-", "")
                : player.getName();
    }

    @Override
    public void onLoad() {
        MinecraftVersion.replaceLogger(getLogger());
        MinecraftVersion.disableUpdateCheck();
        MinecraftVersion.disableBStats();
        MinecraftVersion.getVersion();
    }

    @Override
    public void onEnable() {
        Util.init(instance = this);

        loadHooks();
        loadFunctions();
        if (!database.ok()) return;
        loadBuiltInAttachments();
        reloadConfig();

        FileConfiguration config = getConfig();
        checkCMI = config.getBoolean("check-compatible.cmi", true);
        checkEssentials = config.getBoolean("check-compatible.essentials", true);
        Bukkit.getPluginManager().registerEvents(this, this);

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

        IMail.instance = new MailAPI();
        getLogger().info("SweetMail 加载完毕");
        if (getDescription().getVersion().endsWith("-unstable")) {
            warn("你正在运行 SweetMail 开发版或预览版，可能会存在一些问题。");
            warn("如有问题，请通过以下任一链接向作者反馈。");
            warn("  https://github.com/MrXiaoM/SweetMail/issues");
            warn("  https://www.minebbs.com/members/24586");
            warn("我已默认你已阅读这条消息，如果你不想在日志中看到，请编辑 plugin.yml，删除 -unstable");
        }
        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            checkCompatible(null);
        }
    }

    public void loadFunctions() {
        AbstractPluginHolder.loadModules(this);
        textHelper = new TextHelper(this);
        guiManager = new GuiManager(this);
        database = new MailDatabase(this).reload();
    }

    public void loadHooks() {
        if (!Util.isPresent("net.milkbowl.vault.economy.Economy")) {
            economy = null;
            warn("没有安装 Vault");
        } else {
            economy = EconomyHolder.inst();
            if (economy == null) {
                warn("已安装 Vault，未发现经济插件");
            } else {
                info("已安装 Vault，经济插件为 " + economy.economy.getName());
            }
        }
        if (Util.isPresent("me.clip.placeholderapi.expansion.PlaceholderExpansion")) {
            new Placeholder(this).register();
        }
    }

    private void loadBuiltInAttachments() {
        AttachmentItem.register();
        AttachmentCommand.register();
        if (economy != null) AttachmentMoney.register();
    }

    @EventHandler
    public void checkCompatible(ServerLoadEvent e) {
        if (checkCMI){
            Plugin plugin = Bukkit.getPluginManager().getPlugin("CMI");
            if (plugin instanceof JavaPlugin) {
                File plugins = getDataFolder().getParentFile();
                File alias = new File(plugins, "CMI/Settings/Alias.yml");
                YamlConfiguration config = YamlConfiguration.loadConfiguration(alias);
                if (config.getBoolean("Alias./mail.Enabled", false)) {
                    warn("============================================================");
                    warn("SweetMail 与 CMI 存在兼容性问题，本插件的 /mail 命令被 CMI 覆盖。");
                    warn("● 如果想禁用 CMI 的命令，编辑 /plugins/CMI/Settings/Alias.yml");
                    warn("● 如果想忽略这个警告，在 config.yml 设置 check-compatible.cmi: false");
                    warn("============================================================");
                }
            }
        }
        if (checkEssentials) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("Essentials");
            if (plugin instanceof JavaPlugin) {
                PluginCommand conflict = ((JavaPlugin) plugin).getCommand("mail");
                if (conflict != null) {
                    warn("============================================================");
                    warn("SweetMail 与 Essentials 存在兼容性问题，本插件的 /mail 命令被 Essentials 覆盖。");
                    warn("● 如果想禁用 Essentials 的命令，编辑插件 jar 内的 plugin.yml，删除 commands 下的 mail");
                    warn("● 如果想忽略这个警告，在 config.yml 设置 check-compatible.essentials: false");
                    warn("============================================================");
                }
            }
        }
    }

    @Override
    public void onDisable() {
        AbstractPluginHolder.callDisable();
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
        HandlerList.unregisterAll((Plugin) this);
        getScheduler().cancelAllTasks();
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
        bundleMaxSlots = config.getInt("bundle-max-slots", 7);
        info("插件当前在 " + (onlineMode ? "在线模式": "离线模式") + " 下运行");
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
            long outdateTime = draft.getOutdateTime();

            Mail mail = new Mail(uuid, sender, senderDisplay, icon, receivers, title, content, attachments, outdateTime);
            db.sendMail(mail);
            return Status.SUCCESS;
        }

        @Override
        protected String send(MailDraft draft, long timestamp) {
            TimerManager manager = TimerManager.inst();
            Draft generated = new Draft(DraftManager.inst(), draft.getSender());
            generated.advSenderDisplay = draft.getSenderDisplay();
            generated.iconKey = "!" + draft.getIcon();
            generated.title = draft.getTitle();
            generated.content = draft.getContent();
            for (IAttachment attachment : draft.getAttachments()) {
                if (attachment.isLegal()) {
                    generated.attachments.add(attachment);
                }
            }
            if (draft.getReceivers().isEmpty()) return null;
            if (draft.getReceivers().size() > 1) {
                if (!draft.getReceivers().get(0).equals("#advance#")) {
                    throw new IllegalArgumentException("定时发送不支持多个 receivers 的用法，请将第一个元素设为 #advance#，第二个元素设为泛接收者表达式");
                }
                generated.advReceivers = draft.getReceivers().get(1);
            } else {
                generated.receiver = draft.getReceivers().get(0);
            }
            return manager.sendInTime(generated, timestamp);
        }
    }
}
