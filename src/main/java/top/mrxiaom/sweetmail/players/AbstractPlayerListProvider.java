package top.mrxiaom.sweetmail.players;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractPlayerListProvider {
    public int priority() {
        return 1000;
    }

    @Nullable
    public IPlayerList fromString(String str) {
        return null;
    }

    @Nullable
    public IPlayerList fromConfig(ConfigurationSection config) {
        return null;
    }
}
