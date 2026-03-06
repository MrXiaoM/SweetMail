package top.mrxiaom.sweetmail.players.providers;

import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.Nullable;
import top.mrxiaom.sweetmail.players.AbstractPlayerListProvider;
import top.mrxiaom.sweetmail.players.IPlayerList;
import top.mrxiaom.sweetmail.players.builtin.PlayerListRaw;

import java.util.ArrayList;
import java.util.List;

public class ProviderRaw extends AbstractPlayerListProvider {

    @Override
    public @Nullable IPlayerList fromString(String str) {
        if (str.startsWith("players ")) {
            String input = str.substring(8);
            List<String> names = new ArrayList<>();
            if (input.contains(",")) {
                for (String s : input.split(",")) {
                    names.add(s.trim());
                }
            } else {
                names.add(input.trim());
            }
            return new PlayerListRaw(names);
        }
        return null;
    }

    @Override
    public @Nullable IPlayerList fromConfig(ConfigurationSection config) {
        if ("players".equalsIgnoreCase(config.getString("type"))) {
            List<String> names = config.getStringList("names");
            if (!names.isEmpty()) {
                return new PlayerListRaw(names);
            }
        }
        return null;
    }
}
