package top.mrxiaom.sweetmail.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.depend.PAPI;
import top.mrxiaom.sweetmail.utils.Pair;
import top.mrxiaom.sweetmail.utils.Util;

import java.util.List;

public class ActionActionBar implements IAction {
    public static final IActionProvider PROVIDER = s -> {
        if (s.startsWith("[actionbar]")) {
            return new ActionActionBar(s.substring(11));
        }
        if (s.startsWith("actionbar:")) {
            return new ActionActionBar(s.substring(10));
        }
        return null;
    };
    public final String message;
    public ActionActionBar(String message) {
        this.message = message;
    }

    @Override
    public void run(Player player, @Nullable List<Pair<String, Object>> replacements) {
        String s = Pair.replace(message, replacements);
        Util.sendActionBar(player, PAPI.setPlaceholders(player, s));
    }
}
