package top.mrxiaom.sweetmail.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.utils.Pair;

import java.util.List;

public class ActionClose implements IAction {
    public static final IActionProvider PROVIDER;
    public static final ActionClose INSTANCE;
    static {
        INSTANCE = new ActionClose();
        PROVIDER = s -> s.equals("[close]") || s.equals("close") ? INSTANCE : null;
    }
    private ActionClose() {
    }

    @Override
    public void run(Player player, @Nullable List<Pair<String, Object>> replacements) {
        player.closeInventory();
    }
}
