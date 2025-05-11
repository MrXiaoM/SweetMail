package top.mrxiaom.sweetmail.actions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.depend.PAPI;
import top.mrxiaom.sweetmail.utils.Pair;

import java.util.List;

public class ActionPlayer implements IAction {
    public static final IActionProvider PROVIDER = s -> {
        if (s.startsWith("[player]")) {
            return new ActionPlayer(s.substring(8));
        }
        if (s.startsWith("player:")) {
            return new ActionPlayer(s.substring(7));
        }
        return null;
    };
    public final String command;
    public ActionPlayer(String command) {
        this.command = command;
    }

    @Override
    public void run(Player player, @Nullable List<Pair<String, Object>> replacements) {
        String s = Pair.replace(command, replacements);
        Bukkit.dispatchCommand(player, PAPI.setPlaceholders(player, s));
    }
}
