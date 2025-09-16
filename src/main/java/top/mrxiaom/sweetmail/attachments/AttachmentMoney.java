package top.mrxiaom.sweetmail.attachments;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.Messages;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.config.gui.MenuAddAttachmentConfig;
import top.mrxiaom.sweetmail.config.gui.MenuDraftConfig;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.depend.PAPI;
import top.mrxiaom.sweetmail.func.DraftManager;
import top.mrxiaom.sweetmail.func.data.Draft;
import top.mrxiaom.sweetmail.utils.ChatPrompter;
import top.mrxiaom.sweetmail.utils.ItemStackUtil;
import top.mrxiaom.sweetmail.utils.Pair;
import top.mrxiaom.sweetmail.utils.Util;

import java.util.List;

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
        SweetMail.getInstance().economy().giveMoney(player, money);
    }

    @Override
    public void onClaimed(Mail mail, Player player) {
        String message = Internal.attachmentMoneyClaimedMessage;
        if (!message.isEmpty()) {
            Util.sendMessage(player, Pair.replace(message, Pair.of("%money%", money)));
        }
    }

    @Override
    public ItemStack generateDraftIcon(Player target) {
        ItemStack item = ItemStackUtil.getItem(target, Messages.Draft.attachments__money__icon.str());
        ItemStackUtil.setItemDisplayName(item, toString());
        List<String> loreRemove = Internal.getLoreRemove(target);
        List<String> moneyLore = PAPI.setPlaceholders(target, Messages.Draft.attachments__money__lore.list());
        if (!moneyLore.isEmpty() || !loreRemove.isEmpty()) {
            List<String> lore = ItemStackUtil.getItemLoreAsMiniMessage(item);
            lore.addAll(moneyLore);
            lore.addAll(loreRemove);
            ItemStackUtil.setItemLore(item, replace(lore, Pair.of("%money%", money)));
        }
        return item;
    }

    @Override
    public ItemStack generateIcon(Player target) {
        ItemStack item = ItemStackUtil.getItem(target, Messages.Draft.attachments__money__icon.str());
        ItemStackUtil.setItemDisplayName(item, toString());
        List<String> moneyLore = PAPI.setPlaceholders(target, Messages.Draft.attachments__money__lore.list());
        if (!moneyLore.isEmpty()) {
            List<String> lore = ItemStackUtil.getItemLoreAsMiniMessage(item);
            lore.addAll(moneyLore);
            ItemStackUtil.setItemLore(item, replace(lore, Pair.of("%money%", money)));
        }
        return item;
    }

    @Override
    public String toString() {
        return Messages.Draft.attachments__money__name.str(Pair.of("%money%", money));
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
                            Messages.Draft.attachments__money__add__prompt_tips.str(),
                            Messages.Draft.attachments__money__add__prompt_cancel.str(),
                            str -> {
                                double money = Util.parseDouble(str).orElse(0.0);
                                if (money <= 0) {
                                    Messages.Draft.attachments__money__add__fail.tm(player);
                                    back.run();
                                    return;
                                }
                                if (!plugin.economy().takeMoney(player, money)) {
                                    Messages.Draft.attachments__money__add__not_enough.tm(player);
                                    back.run();
                                    return;
                                }
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
