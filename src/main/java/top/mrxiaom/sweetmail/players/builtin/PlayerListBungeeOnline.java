package top.mrxiaom.sweetmail.players.builtin;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.NonNull;
import top.mrxiaom.sweetmail.func.DraftManager;
import top.mrxiaom.sweetmail.players.IPlayerList;
import top.mrxiaom.sweetmail.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class PlayerListBungeeOnline implements IPlayerList {
    public static final PlayerListBungeeOnline INSTANCE = new PlayerListBungeeOnline();
    private PlayerListBungeeOnline() {}

    @Override
    public @NonNull List<OfflinePlayer> getPlayers() {
        List<OfflinePlayer> list = new ArrayList<>();
        List<String> playerNames = DraftManager.inst().getAllPlayers();
        for (String name : playerNames) {
            OfflinePlayer player = Util.getOfflinePlayer(name).orElse(null);
            if (player == null || player.getName() == null) continue;
            list.add(player);
        }
        return list;
    }

    @Override
    public @NonNull String toLegacyString() {
        return "current online bungeecord";
    }

    @Override
    public void toConfig(@NonNull ConfigurationSection config) {
        config.set("type", "bungee-online");
    }
}
