package top.mrxiaom.sweetmail.commands;

import com.google.common.collect.Lists;
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
import top.mrxiaom.sweetmail.config.gui.MenuDraftConfig;
import top.mrxiaom.sweetmail.config.gui.MenuInBoxConfig;
import top.mrxiaom.sweetmail.config.gui.MenuOutBoxConfig;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;
import top.mrxiaom.sweetmail.func.TimerManager;
import top.mrxiaom.sweetmail.func.data.TimedDraft;
import top.mrxiaom.sweetmail.utils.ChatPrompter;
import top.mrxiaom.sweetmail.utils.Pair;
import top.mrxiaom.sweetmail.utils.Util;

import java.util.ArrayList;
import java.util.List;

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
    private static final List<String> listAdminArg0 = Lists.newArrayList("draft", "inbox", "outbox", "admin", "reload");
    private static final List<String> listArg1Admin = Lists.newArrayList("inbox", "outbox");
    private static final List<String> listArgInBox = Lists.newArrayList("all", "unread");
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
            if (admin && args[0].equalsIgnoreCase("admin")) {
                return startsWith(listArg1Admin, args[1]);
            }
            if (args[0].equalsIgnoreCase("inbox") && sender.hasPermission(PERM_BOX_OTHER)) {
                return startsWith(listArgInBox, args[1]);
            }
            if (args[0].equalsIgnoreCase("outbox") && sender.hasPermission(PERM_BOX_OTHER)) {
                return null;
            }
        }
        if (args.length == 3) {
            if (admin && args[0].equalsIgnoreCase("admin")) {
                if (args[1].equalsIgnoreCase("inbox")) {
                    return startsWith(listArgInBox, args[2]);
                }
                if (args[1].equalsIgnoreCase("outbox")) {
                    return null;
                }
                if (args[1].equalsIgnoreCase("timed") || args[1].equalsIgnoreCase("cancel")) {
                    return startsWith(TimerManager.inst().getQueueIds(), args[2]);
                }
            }
            if (args[0].equalsIgnoreCase("inbox") && sender.hasPermission(PERM_BOX_OTHER)) {
                return null;
            }
        }
        if (args.length == 4) {
            if (admin && args[0].equalsIgnoreCase("admin")) {
                if (args[1].equalsIgnoreCase("inbox")) {
                    return null;
                }
            }
        }
        return emptyList;
    }

    public List<String> startsWith(List<String> list, String s) {
        String s1 = s.toLowerCase();
        List<String> stringList = new ArrayList<>(list);
        stringList.removeIf(it -> !it.toLowerCase().startsWith(s1));
        return stringList;
    }
}
