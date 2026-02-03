package top.mrxiaom.sweetmail.actions;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.utils.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ActionProviders {
    private static final List<IActionProvider> actionProviders = new ArrayList<>();
    private ActionProviders() {}

    public static List<IAction> loadActions(ConfigurationSection section, String key) {
        return loadActions(section, new String[]{key});
    }

    public static List<IAction> loadActions(ConfigurationSection section, String... keys) {
        List<String> list = new ArrayList<>();
        for (String key : keys) {
            list.addAll(section.getStringList(key));
        }
        if (list.isEmpty()) return new ArrayList<>();
        return loadActions(list);
    }

    public static List<IAction> loadActions(List<String> list) {
        List<IAction> actions = new ArrayList<>();
        for (String s : list) {
            for (IActionProvider provider : actionProviders) {
                IAction action = provider.provide(s);
                if (action != null) {
                    actions.add(action);
                }
            }
        }
        return actions;
    }

    public static void registerActionProvider(IActionProvider provider) {
        actionProviders.add(provider);
        actionProviders.sort(Comparator.comparingInt(IActionProvider::priority));
    }

    public static void run(SweetMail plugin, Player player, List<IAction> actions) {
        run0(plugin, player, actions, null, 0);
    }

    public static void run(SweetMail plugin, Player player, List<IAction> actions, List<Pair<String, Object>> replacements) {
        run0(plugin, player, actions, replacements, 0);
    }

    private static void run0(SweetMail plugin, Player player, List<IAction> actions, List<Pair<String, Object>> replacements, int startIndex) {
        for (int i = startIndex; i < actions.size(); i++) {
            IAction action = actions.get(i);
            action.run(player);
            long delay = action.delayAfterRun();
            if (delay > 0) {
                int index = i + 1;
                plugin.getScheduler().runTaskLater(() -> run0(plugin, player, actions, replacements, index), delay);
                return;
            }
        }
    }
}
