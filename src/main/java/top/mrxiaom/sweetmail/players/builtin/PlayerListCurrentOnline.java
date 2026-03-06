package top.mrxiaom.sweetmail.players.builtin;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.NonNull;
import top.mrxiaom.sweetmail.players.IPlayerList;

import java.util.List;

public class PlayerListCurrentOnline implements IPlayerList {
    public static final PlayerListCurrentOnline INSTANCE = new PlayerListCurrentOnline();
    private PlayerListCurrentOnline() {}

    @Override
    public @NonNull List<OfflinePlayer> getPlayers() {
        return Lists.newArrayList(Bukkit.getOnlinePlayers());
    }

    @Override
    public @NonNull String toLegacyString() {
        return "current online";
    }

    @Override
    public void toConfig(@NonNull ConfigurationSection config) {
        config.set("type", "online");
    }
}
