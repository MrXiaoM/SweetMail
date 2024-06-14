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
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;
import top.mrxiaom.sweetmail.gui.GuiDraft;
import top.mrxiaom.sweetmail.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class CommandMain extends AbstractPluginHolder implements CommandExecutor, TabCompleter {
    public static final String PERM_ADMIN = "sweetmail.admin";
    public static final String PERM_DRAFT = "sweetmail.draft";
    public static final String PERM_BOX = "sweetmail.box";
    public static final String PERM_BOX_OTHER = "sweetmail.box.other";
    private List<String> helpPlayer;
    private List<String> helpAdmin;
    public CommandMain(SweetMail plugin) {
        super(plugin);
        registerCommand("sweetmail", this);
        register();
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        helpPlayer = config.getStringList("help.player");
        helpAdmin = config.getStringList("help.admin");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        boolean admin = sender.hasPermission(PERM_ADMIN);
        if (args.length > 0) {
            if ("admin".equalsIgnoreCase(args[0]) && admin) {
                if (args.length >= 4 && "inbox".equalsIgnoreCase(args[1])) {
                    if (!(sender instanceof Player)) {
                        return true;
                    }
                    Player player = (Player) sender;
                    String type = args[2];
                    String target = args[3];
                    // TODO: 打开别人的收件箱
                    return true;
                }
                if (args.length >= 3 && "outbox".equalsIgnoreCase(args[1])) {
                    if (!(sender instanceof Player)) {
                        return true;
                    }
                    Player player = (Player) sender;
                    String target = args[2];
                    // TODO: 打开别人的发件箱
                    return true;
                }
            }
            if ("draft".equalsIgnoreCase(args[0]) && sender.hasPermission(PERM_DRAFT)) {
                if (!(sender instanceof Player)) {
                    return true;
                }
                Player player = (Player) sender;
                plugin.getGuiManager().openGui(new GuiDraft(plugin, player));
                return true;
            }
            if ("inbox".equalsIgnoreCase(args[0]) && sender.hasPermission(PERM_BOX)) {
                String type = args.length >= 2 ? args[1] : "unread";
                if (args.length >= 3 && sender.hasPermission(PERM_BOX_OTHER)) {
                    Player target = Util.getOnlinePlayer(args[2]).orElse(null);
                    if (target == null) {
                        return true;
                    }
                    // TODO: 为别人打开收件箱
                    return true;
                }
                if (!(sender instanceof Player)) {
                    return true;
                }
                Player player = (Player) sender;
                // TODO: 为自己打开收件箱
                return true;
            }
            if ("outbox".equalsIgnoreCase(args[0]) && sender.hasPermission(PERM_BOX)) {
                if (args.length >= 2 && sender.hasPermission(PERM_BOX_OTHER)) {
                    Player target = Util.getOnlinePlayer(args[1]).orElse(null);
                    if (target == null) {
                        return true;
                    }
                    // TODO: 为别人打开发件箱
                    return true;
                }
                if (!(sender instanceof Player)) {
                    return true;
                }
                Player player = (Player) sender;
                // TODO: 为自己打开发件箱
                return true;
            }
            if ("reload".equalsIgnoreCase(args[0]) && admin) {
                // TODO: 重载配置文件
                return true;
            }
        }
        // TODO: 显示帮助命令
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
