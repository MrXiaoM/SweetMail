package top.mrxiaom.sweetmail.database.entry;

import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.utils.ColorHelper;
import top.mrxiaom.sweetmail.utils.ItemStackUtil;
import top.mrxiaom.sweetmail.utils.Util;

public class AttachmentMoney implements IAttachment {
    double money;

    public AttachmentMoney(double money) {
        this.money = money;
    }

    @Override
    public void use(Player player) {
        Economy economy = SweetMail.getInstance().getEconomy();
        economy.depositPlayer(player, money);
    }

    @Override
    public ItemStack generateDraftIcon(Player target) {
        // TODO: 使用配置文件自定义显示图标
        ItemStack item = ItemStackUtil.getItem("GOLD_NUGGET");
        ItemStackUtil.setItemDisplayName(item, String.valueOf(money));
        return item;
    }

    @Override
    public ItemStack generateIcon(Player target) {
        // TODO: 使用配置文件自定义显示图标
        ItemStack item = ItemStackUtil.getItem("GOLD_NUGGET");
        ItemStackUtil.setItemDisplayName(item, String.valueOf(money));
        return item;
    }

    @Override
    public String serialize() {
        return "money:" + money;
    }

    public static IAttachment deserialize(String s) {
        if (s.startsWith("money:")) {
            Double money = Util.parseDouble(s.substring(6)).orElse(null);
            if (money != null) {
                return new AttachmentMoney(money);
            }
        }
        return null;
    }
}
