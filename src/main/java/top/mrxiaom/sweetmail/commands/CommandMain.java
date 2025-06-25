package top.mrxiaom.sweetmail.commands;

import com.google.common.collect.Lists;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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
import top.mrxiaom.sweetmail.Messages;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static top.mrxiaom.sweetmail.utils.StringHelper.startsWith;
import static top.mrxiaom.sweetmail.utils.Util.toTimestamp;

public class CommandMain extends AbstractPluginHolder implements CommandExecutor, TabCompleter {
    public static final String PERM_ADMIN = "sweetmail.admin";
    public static final String PERM_DRAFT = "sweetmail.draft";
    public static final String PERM_DRAFT_OTHER = "sweetmail.draft.other";
    public static final String PERM_BOX = "sweetmail.box";
    public static final String PERM_BOX_OTHER = "sweetmail.box.other";
    private String defaultTimeSpan;
    public CommandMain(SweetMail plugin) {
        super(plugin);
        registerCommand("sweetmail", this);
        register();
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        defaultTimeSpan = config.getString("all-offline-players-default-timespan", "90d");
        if (parseFromTimeSpan(defaultTimeSpan) == null) {
            warn("[config.yml] 配置中的 all-offline-players-default-timespan 无法解析");
        }
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
                    if (timedDraft == null) return t(sender, plugin.prefix() + Messages.Command.timed__info__not_found.str());
                    String senderDisplay = timedDraft.draft.advSenderDisplay == null ? "" : timedDraft.draft.advSenderDisplay;
                    String mailSender = senderDisplay.isEmpty() ? timedDraft.draft.sender : IMail.SERVER_SENDER;
                    for (String s : Messages.Command.timed__info__display.list(
                            Pair.of("%id%", id),
                            Pair.of("%sender%", mailSender),
                            Pair.of("%senderDisplay%", senderDisplay),
                            Pair.of("%receiver%", timedDraft.draft.receiver),
                            Pair.of("%advReceivers%", timedDraft.draft.advReceivers))) {
                        t(sender, plugin.prefix() + s);
                    }
                    return true;
                }
                if (args.length >= 3 && "cancel".equalsIgnoreCase(args[1])) {
                    String id = args[2];
                    TimerManager manager = TimerManager.inst();
                    boolean result = manager.cancelQueue(id);
                    return t(sender, plugin.prefix() + (result ? Messages.Command.timed__cancel__success : Messages.Command.timed__cancel__fail).str());
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
            if ("send".equalsIgnoreCase(args[0]) && args.length >= 2 && admin) {
                Template template = TemplateConfig.inst().get(args[1]);
                if (template == null) {
                    return t(sender, "&e邮件模板 " + args[1] + " 不存在");
                }
                List<OfflinePlayer> players = getPlayers(sender, args[2]);
                if (players.isEmpty()) {
                    return t(sender, "&e输入的接收者表达式共筛选出了 0 位玩家");
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
            if ("players".equalsIgnoreCase(args[0]) && args.length >= 2 && admin) {
                List<OfflinePlayer> players = getPlayers(sender, args[1]);
                if (players.isEmpty()) {
                    return t(sender, "&a接收者表达式&e " + args[1] + " &a共计算出&c 0 &e名玩家");
                }
                if (sender instanceof Player && args.length >= 3
                        && (args[2].equals("--book") || args[2].equals("-b"))
                ) {
                    // 通过书与笔展示
                    List<Component> pages = new ArrayList<>();
                    int i = 0;
                    while (i < players.size()) {
                        List<String> lines = new ArrayList<>();
                        lines.add("共 " + players.size() + " 名玩家");
                        for (int j = 0; j < 10 && i < players.size(); j++, i++) {
                            OfflinePlayer player = players.get(i);
                            String uuid = player.getUniqueId().toString();
                            String name = player.getName() == null ? "[null]" : player.getName();
                            lines.add("- <hover:show_text:" + uuid + ">" + name + "</hover>");
                        }
                        pages.add(MiniMessageConvert.miniMessage(String.join("\n", lines)));
                    }
                    Util.openBook((Player) sender, Book.builder()
                            .title(Component.text("SweetMail"))
                            .author(Component.text("SweetMail"))
                            .pages(pages)
                            .build());
                } else {
                    // 通过聊天展示
                    t(sender, "&a接收者表达式&e " + args[1] + " &a共计算出&e " + players.size() + " &e位玩家，前&e 16 &a位名单如下:");
                    for (OfflinePlayer player : players) {
                        t(sender, "&7- &f" + player.getName());
                    }
                }
                return true;
            }
            if ("reload".equalsIgnoreCase(args[0]) && admin) {
                if (args.length > 1 && "database".equalsIgnoreCase(args[1])) {
                    plugin.getMailDatabase().reload();
                    t(sender, plugin.prefix() + Messages.Command.reload_database.str());
                    return true;
                }
                plugin.reloadConfig();
                t(sender, plugin.prefix() + Messages.Command.reload.str());
                return true;
            }
        }
        Messages.help__player.tm(sender);
        if (admin) {
            Messages.help__admin.tm(sender);
        }
        return true;
    }

    public List<OfflinePlayer> getPlayers(CommandSender sender, String str) {
        List<OfflinePlayer> list = new ArrayList<>();
        if (str.startsWith("@")) {
            int startIndex = str.indexOf('[');
            int endIndex = str.lastIndexOf(']');
            String type;
            Map<String, String> parameters = new HashMap<>();
            if (startIndex != -1 && endIndex != -1) {
                type = str.substring(1, startIndex).toLowerCase();
                String[] input = str.substring(startIndex + 1, endIndex).split(",");
                for (String s : input) {
                    if (s.isEmpty()) continue;
                    String[] split = s.split("=", 2);
                    if (split.length == 1) {
                        parameters.put(split[0], "");
                    } else {
                        parameters.put(split[0], split[1]);
                    }
                }
            } else {
                type = str.substring(1).toLowerCase();
            }
            switch (type) {
                case "all": {
                    LocalDateTime fromTime, toTime;
                    if (parameters.containsKey("timespan") || parameters.containsKey("ts")) {
                        String strTimeSpan = parameters.getOrDefault("timespan", parameters.get("ts"));
                        fromTime = parseFromTimeSpan(strTimeSpan);
                        toTime = LocalDateTime.now();
                    } else if (parameters.containsKey("from")) {
                        fromTime = parseDateTime(parameters.get("from"));
                        if (parameters.containsKey("to")) {
                            toTime = parseDateTime(parameters.get("to"));
                        } else {
                            toTime = LocalDateTime.now();
                        }
                    } else {
                        fromTime = parseFromTimeSpan(defaultTimeSpan);
                        toTime = LocalDateTime.now();
                    }
                    if (fromTime != null && toTime != null) {
                        long from = toTimestamp(fromTime), to = toTimestamp(toTime);
                        for (OfflinePlayer player : Util.getOfflinePlayers()) {
                            if (player == null || player.getName() == null) continue;
                            long last = player.getLastPlayed();
                            if (last >= from && last <= to) {
                                list.add(player);
                            }
                        }
                    }
                    break;
                }
                case "self":
                case "me": {
                    if (sender instanceof Player) {
                        list.add((Player) sender);
                    }
                    break;
                }
                case "online": {
                    if (parameters.containsKey("bc")) {
                        List<String> playerNames = DraftManager.inst().getAllPlayers();
                        for (String name : playerNames) {
                            OfflinePlayer player = Util.getOfflinePlayer(name).orElse(null);
                            if (player == null || player.getName() == null) continue;
                            list.add(player);
                        }
                        break;
                    }
                    list.addAll(Bukkit.getOnlinePlayers());
                    break;
                }
                default:
                    break;
            }
        } else {
            for (String s : str.split(",")) {
                Util.getOfflinePlayer(s).ifPresent(list::add);
            }
        }
        return list;
    }

    @Nullable
    public static LocalDateTime parseDateTime(String s) {
        String[] split = s.split("_", 2);
        String[] dateSplit = split[0].split("-", 3);
        Integer year, month, date;
        if (dateSplit.length == 2) {
            year = LocalDate.now().getYear();
            month = Util.parseInt(dateSplit[0]).orElse(null);
            date = Util.parseInt(dateSplit[1]).orElse(null);
        } else if (dateSplit.length == 3) {
            year = Util.parseInt(dateSplit[0]).orElse(null);
            month = Util.parseInt(dateSplit[1]).orElse(null);
            date = Util.parseInt(dateSplit[2]).orElse(null);
        } else {
            return null;
        }
        if (year == null || month == null || date == null) return null;
        LocalDate localDate = LocalDate.of(year, month, date);
        LocalTime localTime = parseLocalTimeOrZero(split.length > 1 ? split[1] : null);
        return localDate.atTime(localTime);
    }

    @NotNull
    public static LocalTime parseLocalTimeOrZero(String str) {
        if (str == null) return LocalTime.of(0, 0, 0);
        String[] split = str.split(":", 3);
        int hour = split.length > 0 ? Util.parseInt(split[0]).orElse(0) : 0;
        int minute = split.length > 1 ? Util.parseInt(split[1]).orElse(0) : 0;
        int second = split.length > 2 ? Util.parseInt(split[2]).orElse(0) : 0;
        return LocalTime.of(hour, minute, second);
    }

    @Nullable
    public static LocalDateTime parseFromTimeSpan(String s) {
        long seconds = 0L;
        String[] split = s.replaceAll("([dhms])", "$1 ").split(" ");
        for (String str : split) {
            int last = str.length() - 1;
            if (last == -1) continue;
            char unit = str.charAt(last);
            Integer num = Util.parseInt(str.substring(0, last)).orElse(null);
            if (num == null) continue;
            switch (unit) {
                case 'd': seconds += num * 86400L; break;
                case 'h': seconds += num * 3600L; break;
                case 'm': seconds += num * 60L; break;
                case 's': seconds += num; break;
                default: break;
            }
        }
        if (seconds == 0L) {
            return null;
        }
        return LocalDateTime.now().minusSeconds(seconds);
    }

    private static final List<String> emptyList = Lists.newArrayList();
    private static final List<String> listArg0 = Lists.newArrayList("draft", "inbox", "outbox");
    private static final List<String> listAdminArg0 = Lists.newArrayList("draft", "inbox", "outbox", "admin", "save", "send", "players", "reload");
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
                if ("players".equalsIgnoreCase(args[0])) {
                    return null;
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
}
