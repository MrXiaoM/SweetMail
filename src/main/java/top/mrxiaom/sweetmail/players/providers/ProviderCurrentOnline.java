package top.mrxiaom.sweetmail.players.providers;

import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.Nullable;
import top.mrxiaom.sweetmail.players.AbstractPlayerListProvider;
import top.mrxiaom.sweetmail.players.IPlayerList;
import top.mrxiaom.sweetmail.players.builtin.PlayerListCurrentOnline;

public class ProviderCurrentOnline extends AbstractPlayerListProvider {

    @Override
    public @Nullable IPlayerList fromString(String str) {
        if (str.equalsIgnoreCase("current online")) {
            return PlayerListCurrentOnline.INSTANCE;
        }
        return null;
    }

    @Override
    public @Nullable IPlayerList fromConfig(ConfigurationSection config) {
        if ("online".equalsIgnoreCase(config.getString("type"))) {
            return PlayerListCurrentOnline.INSTANCE;
        }
        return null;
    }
}
