package top.mrxiaom.sweetmail.attachments;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.utils.ItemStackUtil;
import top.mrxiaom.sweetmail.utils.Pair;
import top.mrxiaom.sweetmail.utils.Util;

import java.util.List;

import static top.mrxiaom.sweetmail.utils.Pair.replace;

public class AttachmentMoney implements IAttachment {
    private final double money;

    private AttachmentMoney(double money) {
        this.money = money;
    }

    /**
     * 构建一个Vault金币附件
     * @param money 金币数额
     */
    public static AttachmentMoney build(double money) {
        if (money <= 0) throw new IllegalArgumentException("money is less then or equals to zero");
        return new AttachmentMoney(money);
    }

    @Override
    public void use(Player player) {
        Economy economy = SweetMail.getInstance().getEconomy();
        economy.depositPlayer(player, money);
    }

    @Override
    public ItemStack generateDraftIcon(Player target) {
        ItemStack item = ItemStackUtil.getItem(Text.moneyIcon);
        ItemStackUtil.setItemDisplayName(item, toString());
        if (!Text.moneyLore.isEmpty() || !Text.loreRemove.isEmpty()) {
            List<String> lore = ItemStackUtil.getItemLore(item);
            lore.addAll(Text.moneyLore);
            lore.addAll(Text.loreRemove);
            ItemStackUtil.setItemLore(item, replace(lore, Pair.of("%money%", money)));
        }
        return item;
    }

    @Override
    public ItemStack generateIcon(Player target) {
        ItemStack item = ItemStackUtil.getItem(Text.moneyIcon);
        ItemStackUtil.setItemDisplayName(item, toString());
        if (!Text.moneyLore.isEmpty()) {
            List<String> lore = ItemStackUtil.getItemLore(item);
            lore.addAll(Text.moneyLore);
            ItemStackUtil.setItemLore(item, replace(lore, Pair.of("%money%", money)));
        }
        return item;
    }

    @Override
    public String toString() {
        return Text.moneyName.replace("%money%", String.valueOf(money));
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
