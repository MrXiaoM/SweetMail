package top.mrxiaom.sweetmail.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultEconomy implements IEconomy {
    public final Economy economy;
    private VaultEconomy(Economy economy) {
        this.economy = economy;
    }

    @Override
    public String getName() {
        return economy.getName();
    }

    @Override
    public boolean has(OfflinePlayer player, double money) {
        return get(player) >= money;
    }

    @Override
    public double get(OfflinePlayer player) {
        return economy.getBalance(player);
    }

    @Override
    public boolean takeMoney(OfflinePlayer player, double money) {
        if (has(player, money)) {
            return economy.withdrawPlayer(player, money).transactionSuccess();
        }
        return false;
    }

    @Override
    public void giveMoney(OfflinePlayer player, double money) {
        economy.depositPlayer(player, money);
    }

    public static VaultEconomy inst() {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (economyProvider == null) return null;
        return new VaultEconomy(economyProvider.getProvider());
    }
}
