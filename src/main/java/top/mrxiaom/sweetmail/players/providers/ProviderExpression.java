package top.mrxiaom.sweetmail.players.providers;

import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.Nullable;
import top.mrxiaom.sweetmail.players.AbstractPlayerListProvider;
import top.mrxiaom.sweetmail.players.IPlayerList;
import top.mrxiaom.sweetmail.players.builtin.PlayerListExpression;

public class ProviderExpression extends AbstractPlayerListProvider {
    @Override
    public @Nullable IPlayerList fromString(String str) {
        if (str.startsWith("eval offline ")) {
            String expression = str.substring(13);
            return new PlayerListExpression(true, expression);
        }
        if (str.startsWith("eval online ")) {
            String expression = str.substring(12);
            return new PlayerListExpression(false, expression);
        }
        return null;
    }

    @Override
    public @Nullable IPlayerList fromConfig(ConfigurationSection config) {
        String type = config.getString("type");
        if ("eval".equalsIgnoreCase(type) || "expression".equalsIgnoreCase(type)) {
            boolean includeOffline = config.getBoolean("include-offline", false);
            String expression = config.getString("expression");
            if (expression != null) {
                new PlayerListExpression(includeOffline, expression);
            }
        }
        return null;
    }
}
