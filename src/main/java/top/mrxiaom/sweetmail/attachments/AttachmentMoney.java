package top.mrxiaom.sweetmail.attachments;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.config.gui.MenuAddAttachmentConfig;
import top.mrxiaom.sweetmail.config.gui.MenuDraftConfig;
import top.mrxiaom.sweetmail.func.DraftManager;
import top.mrxiaom.sweetmail.func.data.Draft;
import top.mrxiaom.sweetmail.utils.ChatPrompter;
import top.mrxiaom.sweetmail.utils.ItemStackUtil;
import top.mrxiaom.sweetmail.utils.Pair;
import top.mrxiaom.sweetmail.utils.Util;

import java.util.List;

import static top.mrxiaom.sweetmail.func.AbstractPluginHolder.t;
import static top.mrxiaom.sweetmail.utils.Pair.replace;

public class AttachmentMoney implements IAttachment {
    public static final String PERM = "sweetmail.attachment.money";
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
        SweetMail.getInstance().getEconomy().giveMoney(player, money);
    }

    @Override
    public ItemStack generateDraftIcon(Player target) {
        ItemStack item = ItemStackUtil.getItem(Internal.moneyIcon);
        ItemStackUtil.setItemDisplayName(item, toString());
        List<String> loreRemove = Internal.getLoreRemove(target);
        if (!Internal.moneyLore.isEmpty() || !loreRemove.isEmpty()) {
            List<String> lore = ItemStackUtil.getItemLore(item);
            lore.addAll(Internal.moneyLore);
            lore.addAll(loreRemove);
            ItemStackUtil.setItemLore(item, replace(lore, Pair.of("%money%", money)));
        }
        return item;
    }

    @Override
    public ItemStack generateIcon(Player target) {
        ItemStack item = ItemStackUtil.getItem(Internal.moneyIcon);
        ItemStackUtil.setItemDisplayName(item, toString());
        if (!Internal.moneyLore.isEmpty()) {
            List<String> lore = ItemStackUtil.getItemLore(item);
            lore.addAll(Internal.moneyLore);
            ItemStackUtil.setItemLore(item, replace(lore, Pair.of("%money%", money)));
        }
        return item;
    }

    @Override
    public String toString() {
        return Internal.moneyName.replace("%money%", String.valueOf(money));
    }

    @Override
    public String serialize() {
        return "money:" + money;
    }

    @Override
    public boolean isLegal() {
        return money > 0;
    }

    public static void register() {
        IAttachment.registerAttachment(AttachmentMoney.class, PERM,
                Internal::attachmentMoney,
                (player) -> { // addGui
                    SweetMail plugin = SweetMail.getInstance();
                    Runnable back = () -> MenuAddAttachmentConfig.inst().new Gui(plugin, player).open();
                    ChatPrompter.prompt(
                            plugin, player,
                            Internal.addMoneyPromptTips, Internal.addMoneyPromptCancel,
                            str -> {
                                double money = Util.parseDouble(str).orElse(0.0);
                                if (money <= 0) {
                                    t(player, Internal.addMoneyFail);
                                    back.run();
                                    return;
                                }
                                if (!plugin.getEconomy().has(player, money)) {
                                    t(player, Internal.addMoneyNotEnough);
                                    back.run();
                                    return;
                                }
                                plugin.getEconomy().takeMoney(player, money);
                                AttachmentMoney attachment = AttachmentMoney.build(money);
                                Draft draft = DraftManager.inst().getDraft(player);
                                draft.attachments.add(attachment);
                                draft.save();
                                MenuDraftConfig.inst().new Gui(plugin, player).open();
                            }, back);
                    return null;
                },
                (s) -> { // deserializer
                    if (s.startsWith("money:")) {
                        Double money = Util.parseDouble(s.substring(6)).orElse(null);
                        if (money != null) {
                            return new AttachmentMoney(money);
                        }
                    }
                    return null;
                });
    }
}
