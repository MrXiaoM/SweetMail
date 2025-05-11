package top.mrxiaom.sweetmail.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.utils.Pair;

import java.util.List;

public interface IAction {
    default long delayAfterRun() {
        return 0L;
    }
    default void run(Player player) {
        run(player, null);
    }
    void run(Player player, @Nullable List<Pair<String, Object>> replacements);
}
