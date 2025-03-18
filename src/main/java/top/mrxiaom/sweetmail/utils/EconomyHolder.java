package top.mrxiaom.sweetmail.utils;

import org.bukkit.OfflinePlayer;
import top.mrxiaom.sweetmail.SweetMail;

@Deprecated
@SuppressWarnings({"DeprecatedIsStillUsed", "unused"})
public class EconomyHolder {
    private final SweetMail plugin;
    public EconomyHolder(SweetMail plugin) {
        this.plugin = plugin;
    }

    public boolean has(OfflinePlayer player, double money) {
        return plugin.economy().has(player, money);
    }

    public double get(OfflinePlayer player) {
        return plugin.economy().get(player);
    }

    public void takeMoney(OfflinePlayer player, double money) {
        plugin.economy().takeMoney(player, money);
    }

    public void giveMoney(OfflinePlayer player, double money) {
        plugin.economy().giveMoney(player, money);
    }

    public static EconomyHolder inst() {
        return null;
    }
}
