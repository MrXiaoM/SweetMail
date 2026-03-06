package top.mrxiaom.sweetmail.players.builtin;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.NonNull;
import top.mrxiaom.sweetmail.players.IPlayerList;
import top.mrxiaom.sweetmail.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class PlayerListRaw implements IPlayerList {
    private final List<String> names;
    public PlayerListRaw(List<String> names) {
        this.names = names;
    }

    @Override
    public @NonNull List<OfflinePlayer> getPlayers() {
        List<OfflinePlayer> list = new ArrayList<>();
        for (String s : names) {
            OfflinePlayer player = Util.getOfflinePlayer(s).orElse(null);
            if (player == null || player.getName() == null) continue;
            list.add(player);
        }
        return list;
    }

    @Override
    public @NonNull String toLegacyString() {
        return "players " + String.join(", ", names);
    }

    @Override
    public void toConfig(@NonNull ConfigurationSection config) {
        config.set("type", "players");
        config.set("names", names);
    }
}
