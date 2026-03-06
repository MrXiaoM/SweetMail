package top.mrxiaom.sweetmail.players;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 用于提供玩家列表的接口
 */
public interface IPlayerList {
    @NotNull List<OfflinePlayer> getPlayers();
    default boolean isLagTask() {
        return false;
    }

    @NotNull String toLegacyString();
    void toConfig(@NotNull ConfigurationSection config);
}
