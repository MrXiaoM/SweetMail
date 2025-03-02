package top.mrxiaom.sweetmail.commands;

import com.google.common.collect.Lists;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.IMail;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.config.TemplateConfig;
import top.mrxiaom.sweetmail.config.entry.Template;
import top.mrxiaom.sweetmail.config.gui.MenuDraftConfig;
import top.mrxiaom.sweetmail.config.gui.MenuInBoxConfig;
import top.mrxiaom.sweetmail.config.gui.MenuOutBoxConfig;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;
import top.mrxiaom.sweetmail.func.DraftManager;
import top.mrxiaom.sweetmail.func.TimerManager;
import top.mrxiaom.sweetmail.func.data.Draft;
import top.mrxiaom.sweetmail.func.data.TimedDraft;
import top.mrxiaom.sweetmail.utils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CommandMain extends AbstractPluginHolder implements CommandExecutor, TabCompleter {
    public static final String PERM_ADMIN = "sweetmail.admin";
    public static final String PERM_DRAFT = "sweetmail.draft";
    public static final String PERM_DRAFT_OTHER = "sweetmail.draft.other";
    public static final String PERM_BOX = "sweetmail.box";
    public static final String PERM_BOX_OTHER = "sweetmail.box.other";
    private static String prefix;
    private List<String> helpPlayer;
    private List<String> helpAdmin;
    private String cmdReload;
    private String cmdReloadDatabase;
    private List<String> cmdTimedInfoDisplay;
    private String cmdTimedInfoNotFound;
    private String cmdTimedCancelSuccess;
    private String cmdTimedCancelFail;
    public CommandMain(SweetMail plugin) {
        super(plugin);
        registerCommand("sweetmail", this);
        register();
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        helpPlayer = config.getStringList("help.player");
        helpAdmin = config.getStringList("help.admin");
        prefix = config.getString("messages.prefix", "");
        cmdReload = config.getString("messages.command.reload", "");
        cmdReloadDatabase = config.getString("messages.command.reload-database", "");
        cmdTimedInfoDisplay = config.getStringList("messages.command.timed.info.display");
        cmdTimedInfoNotFound = config.getString("messages.command.timed.info.not-found", "");
        cmdTimedCancelSuccess = config.getString("messages.command.timed.cancel.success", "");
        cmdTimedCancelFail = config.getString("messages.command.timed.cancel.fail", "");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        boolean admin = sender.hasPermission(PERM_ADMIN);
        if (args.length > 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (ChatPrompter.isProcessing(player)) {
                    String content = String.join(" ", args);
                    ChatPrompter.submit(player, content);
                    return true;
                }
            }
            if ("admin".equalsIgnoreCase(args[0]) && admin) {
                if (args.length >= 4 && "inbox".equalsIgnoreCase(args[1])) {
                    if (!(sender instanceof Player)) {
                        return true;
                    }
                    Player player = (Player) sender;
                    String type = args[2];
                    String target = args[3];
                    if (!type.equals("unread") && !type.equals("all")) {
                        return true;
                    }
                    if (plugin.getGuiManager().getOpeningGui(player) != null) return true;
                    MenuInBoxConfig.inst()
                            .new Gui(plugin, player, target, type.equals("unread"))
                            .open();
                    return true;
                }
                if (args.length >= 3 && "outbox".equalsIgnoreCase(args[1])) {
                    if (!(sender instanceof Player)) {
                        return true;
                    }
                    Player player = (Player) sender;
                    String target = args[2];
                    if (plugin.getGuiManager().getOpeningGui(player) != null) return true;
                    MenuOutBoxConfig.inst()
                            .new Gui(plugin, player, target)
                            .open();
                    return true;
                }
                if (args.length >= 3 && "timed".equalsIgnoreCase(args[1])) {
                    String id = args[2];
                    TimerManager manager = TimerManager.inst();
                    TimedDraft timedDraft = manager.getQueue(id);
                    if (timedDraft == null) return t(sender, prefix + cmdTimedInfoNotFound);
                    String senderDisplay = timedDraft.draft.advSenderDisplay == null ? "" : timedDraft.draft.advSenderDisplay;
                    String mailSender = senderDisplay.isEmpty() ? timedDraft.draft.sender : IMail.SERVER_SENDER;
                    for (String s : Pair.replace(cmdTimedInfoDisplay,
                            Pair.of("%id%", id),
                            Pair.of("%sender%", mailSender),
                            Pair.of("%senderDisplay%", senderDisplay),
                            Pair.of("%receiver%", timedDraft.draft.receiver),
                            Pair.of("%advReceivers%", timedDraft.draft.advReceivers))) {
                        t(sender, prefix + s);
                    }
                    return true;
                }
                if (args.length >= 3 && "cancel".equalsIgnoreCase(args[1])) {
                    String id = args[2];
                    TimerManager manager = TimerManager.inst();
                    boolean result = manager.cancelQueue(id);
                    return t(sender, prefix + (result ? cmdTimedCancelSuccess : cmdTimedCancelFail));
                }
            }
            if ("draft".equalsIgnoreCase(args[0]) && sender.hasPermission(PERM_DRAFT)) {
                Player player;
                if (args.length >= 2 && sender.hasPermission(PERM_DRAFT_OTHER)) {
                    player = Util.getOnlinePlayer(args[2]).orElse(null);
                    if (player == null) {
                        return true;
                    }
                } else if (sender instanceof Player) {
                    player = (Player) sender;
                } else {
                    return true;
                }
                if (plugin.getGuiManager().getOpeningGui(player) != null) return true;
                MenuDraftConfig.inst()
                        .new Gui(plugin, player)
                        .open();
                return true;
            }
            if ("inbox".equalsIgnoreCase(args[0]) && sender.hasPermission(PERM_BOX)) {
                String type = args.length >= 2 ? args[1] : "unread";
                if (!type.equals("unread") && !type.equals("all")) {
                    return true;
                }
                Player player;
                if (args.length >= 3 && sender.hasPermission(PERM_BOX_OTHER)) {
                    player = Util.getOnlinePlayer(args[2]).orElse(null);
                    if (player == null) {
                        return true;
                    }
                } else if (sender instanceof Player) {
                    player = (Player) sender;
                } else {
                    return true;
                }
                if (plugin.getGuiManager().getOpeningGui(player) != null) return true;
                MenuInBoxConfig.inst()
                        .new Gui(plugin, player, player.getName(), type.equals("unread"))
                        .open();
                return true;
            }
            if ("outbox".equalsIgnoreCase(args[0]) && sender.hasPermission(PERM_BOX)) {
                Player player;
                if (args.length >= 2 && sender.hasPermission(PERM_BOX_OTHER)) {
                    player = Util.getOnlinePlayer(args[1]).orElse(null);
                    if (player == null) {
                        return true;
                    }
                } else if (sender instanceof Player) {
                    player = (Player) sender;
                } else {
                    return true;
                }
                if (plugin.getGuiManager().getOpeningGui(player) != null) return true;
                MenuOutBoxConfig.inst()
                        .new Gui(plugin, player, player.getName())
                        .open();
                return true;
            }
            if ("save".equalsIgnoreCase(args[0]) && admin) {
                if (sender instanceof Player) {
                    if (args.length != 2) {
                        return t(sender, "&e请输入文件名 &7(不需要.yml)");
                    }
                    Player player = (Player) sender;
                    Draft draft = DraftManager.inst().getDraft(player);
                    Template.save(player, draft, args[1]);
                    return t(player, "&a已执行邮件模板创建操作，详见服务器控制台");
                } else {
                    return t(sender, "&e只有玩家才能执行该命令");
                }
            }
            if ("send".equalsIgnoreCase(args[0]) && admin) {
                Template template = TemplateConfig.inst().get(args[1]);
                if (template == null) {
                    return t(sender, "&e邮件模板 " + args[1] + " 不存在");
                }
                List<OfflinePlayer> players = new ArrayList<>();
                for (String s : args[2].split(",")) {
                    OfflinePlayer player = Util.getOfflinePlayer(s).orElse(null);
                    if (player == null) {
                        return t(sender, "&e玩家 " + s + " 没有登录过这个子服");
                    }
                    players.add(player);
                }
                Result<Args> result = Args.parse(Util.consumeString(args, 3));
                if (result.getError() != null) {
                    String err = result.getError();
                    return t(sender, "&e参数错误 " + err);
                }
                Args params = result.getValue();
                String uuid = plugin.getMailDatabase().generateMailUUID();
                Result<Mail> mail = template.createMail(uuid, players, params);
                if (mail.getError() != null) {
                    String err = mail.getError();
                    return t(sender, "&e邮件发送失败: " + err);
                }
                plugin.getMailDatabase().sendMail(mail.getValue());
                String playerNames = players.stream()
                        .map(OfflinePlayer::getName)
                        .collect(Collectors.joining(", "));
                return t(sender, "&a成功向 " + playerNames + " 发送邮件模板 " + template.id + " " + params);
            }
            if ("reload".equalsIgnoreCase(args[0]) && admin) {
                if (args.length > 1 && "database".equalsIgnoreCase(args[1])) {
                    plugin.getMailDatabase().reload();
                    t(sender, prefix + cmdReloadDatabase);
                    return true;
                }
                plugin.reloadConfig();
                t(sender, prefix + cmdReload);
                return true;
            }
        }
        t(sender, helpPlayer);
        if (admin) {
            t(sender, helpAdmin);
        }
        return true;
    }

    private static final List<String> emptyList = Lists.newArrayList();
    private static final List<String> listArg0 = Lists.newArrayList("draft", "inbox", "outbox");
    private static final List<String> listAdminArg0 = Lists.newArrayList("draft", "inbox", "outbox", "admin", "save", "send", "reload");
    private static final List<String> listArg1Admin = Lists.newArrayList("inbox", "outbox");
    private static final List<String> listArgInBox = Lists.newArrayList("all", "unread");
    private static final List<String> listVarArgSend = Lists.newArrayList("键=值");
    private static final List<String> listArg1Save = Lists.newArrayList("<模板名>");
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        boolean admin = sender.hasPermission(PERM_ADMIN);
        if (args.length > 0 && sender instanceof Player && ChatPrompter.isProcessing((Player) sender)) {
            return args.length > 1 || args[0].length() < 3 ? emptyList : Util.getOfflinePlayers(args[0]);
        }
        if (args.length == 1) {
            return startsWith(admin ? listAdminArg0 : listArg0, args[0]);
        }
        if (args.length == 2) {
            if (admin) {
                if ("admin".equalsIgnoreCase(args[0])) {
                    return startsWith(listArg1Admin, args[1]);
                }
                if ("send".equalsIgnoreCase(args[0])) {
                    return startsWith(TemplateConfig.inst().keys(), args[1]);
                }
                if ("save".equalsIgnoreCase(args[0])) {
                    return listArg1Save;
                }
            }
            if ("inbox".equalsIgnoreCase(args[0]) && sender.hasPermission(PERM_BOX_OTHER)) {
                return startsWith(listArgInBox, args[1]);
            }
            if ("outbox".equalsIgnoreCase(args[0]) && sender.hasPermission(PERM_BOX_OTHER)) {
                return null;
            }
        }
        if (args.length == 3) {
            if (admin) {
                if ("admin".equalsIgnoreCase(args[0])) {
                    if ("inbox".equalsIgnoreCase(args[1])) {
                        return startsWith(listArgInBox, args[2]);
                    }
                    if ("outbox".equalsIgnoreCase(args[1])) {
                        return null;
                    }
                    if ("timed".equalsIgnoreCase(args[1])
                            || "cancel".equalsIgnoreCase(args[1])) {
                        return startsWith(TimerManager.inst().getQueueIds(), args[2]);
                    }
                }
                if ("send".equalsIgnoreCase(args[0])) {
                    return null;
                }
            }
            if ("inbox".equalsIgnoreCase(args[0]) && sender.hasPermission(PERM_BOX_OTHER)) {
                return null;
            }
        }
        if (args.length == 4) {
            if (admin) {
                if ("admin".equalsIgnoreCase(args[0])) {
                    if ("inbox".equalsIgnoreCase(args[1])) {
                        return null;
                    }
                }
            }
        }
        if (args.length > 3 && admin && "send".equalsIgnoreCase(args[0])) {
            return listVarArgSend;
        }
        return emptyList;
    }

    public List<String> startsWith(Collection<String> list, String s) {
        String s1 = s.toLowerCase();
        List<String> stringList = new ArrayList<>(list);
        stringList.removeIf(it -> !it.toLowerCase().startsWith(s1));
        return stringList;
    }
}
