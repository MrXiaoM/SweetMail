package top.mrxiaom.sweetmail.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.depend.PAPI;
import top.mrxiaom.sweetmail.utils.Pair;
import top.mrxiaom.sweetmail.utils.Util;

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
        if (player != null) {
            String s =  PAPI.setPlaceholders(player, command);
            Util.dispatchCommand(player, Pair.replace(s, replacements));
        }
    }
}
