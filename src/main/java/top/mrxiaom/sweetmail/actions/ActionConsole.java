package top.mrxiaom.sweetmail.actions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.depend.PAPI;
import top.mrxiaom.sweetmail.utils.Pair;
import top.mrxiaom.sweetmail.utils.Util;

import java.util.List;

public class ActionConsole implements IAction {
    public static final IActionProvider PROVIDER = s -> {
        if (s.startsWith("[console]")) {
            return new ActionConsole(s.substring(9));
        }
        if (s.startsWith("console:")) {
            return new ActionConsole(s.substring(8));
        }
        return null;
    };
    public final String command;
    public ActionConsole(String command) {
        this.command = command;
    }

    @Override
    public void run(Player player, @Nullable List<Pair<String, Object>> replacements) {
        String s = Pair.replace(command, replacements);
        Util.dispatchCommand(Bukkit.getConsoleSender(), PAPI.setPlaceholders(player, s));
    }
}
