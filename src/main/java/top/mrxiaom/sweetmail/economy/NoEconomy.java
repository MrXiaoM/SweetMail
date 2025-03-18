package top.mrxiaom.sweetmail.economy;

import org.bukkit.OfflinePlayer;

public class NoEconomy implements IEconomy {
    public static final NoEconomy INSTANCE = new NoEconomy();
    private NoEconomy() {}
    @Override
    public boolean has(OfflinePlayer player, double money) {
        return true;
    }

    @Override
    public String getName() {
        return "none";
    }

    @Override
    public double get(OfflinePlayer player) {
        return 0;
    }

    @Override
    public void takeMoney(OfflinePlayer player, double money) {
    }

    @Override
    public void giveMoney(OfflinePlayer player, double money) {
    }
}
