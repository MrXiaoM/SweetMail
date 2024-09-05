package top.mrxiaom.sweetmail.utils;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyHolder {
    public final Economy economy;
    private EconomyHolder(Economy economy) {
        this.economy = economy;
    }

    public boolean has(OfflinePlayer player, double money) {
        return get(player) >= money;
    }

    public double get(OfflinePlayer player) {
        return economy.getBalance(player);
    }

    public void takeMoney(OfflinePlayer player, double money) {
        economy.withdrawPlayer(player, money);
    }

    public void giveMoney(OfflinePlayer player, double money) {
        economy.depositPlayer(player, money);
    }

    public static EconomyHolder inst() {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (economyProvider == null) return null;
        return new EconomyHolder(economyProvider.getProvider());
    }
}
