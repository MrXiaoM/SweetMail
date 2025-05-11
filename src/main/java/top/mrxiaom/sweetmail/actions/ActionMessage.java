package top.mrxiaom.sweetmail.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.depend.PAPI;
import top.mrxiaom.sweetmail.utils.Pair;
import top.mrxiaom.sweetmail.utils.Util;

import java.util.List;

public class ActionMessage implements IAction {
    public static final IActionProvider PROVIDER = s -> {
        if (s.startsWith("[message]")) {
            return new ActionMessage(s.substring(9));
        }
        if (s.startsWith("message:")) {
            return new ActionMessage(s.substring(8));
        }
        return null;
    };
    public final String message;
    public ActionMessage(String message) {
        this.message = message;
    }

    @Override
    public void run(Player player, @Nullable List<Pair<String, Object>> replacements) {
        String s = Pair.replace(message, replacements);
        Util.sendMessage(player, PAPI.setPlaceholders(player, s));
    }
}
