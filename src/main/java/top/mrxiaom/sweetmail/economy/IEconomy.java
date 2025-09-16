package top.mrxiaom.sweetmail.economy;

import org.bukkit.OfflinePlayer;

@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "UnusedReturnValue"})
public interface IEconomy {
    String getName();
    boolean has(OfflinePlayer player, double money);
    double get(OfflinePlayer player);
    boolean takeMoney(OfflinePlayer player, double money);
    void giveMoney(OfflinePlayer player, double money);
}
