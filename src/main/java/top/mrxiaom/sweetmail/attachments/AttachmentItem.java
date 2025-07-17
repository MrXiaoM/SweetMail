package top.mrxiaom.sweetmail.attachments;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.mrxiaom.sweetmail.Messages;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.gui.AbstractAddAttachmentGui;
import top.mrxiaom.sweetmail.utils.ItemStackUtil;
import top.mrxiaom.sweetmail.utils.Pair;
import top.mrxiaom.sweetmail.utils.Util;

import java.util.Collection;
import java.util.List;

public class AttachmentItem implements IAttachment {
    public static final String PERM = "sweetmail.attachment.item";
    private final ItemStack item;

    private AttachmentItem(ItemStack item) {
        this.item = item.clone();
    }

    /**
     * 构建一个物品附件
     * @param item 物品
     */
    public static AttachmentItem build(ItemStack item) {
        return new AttachmentItem(item);
    }

    @Override
    public void use(Player player) {
        ItemStack itemToAdd = item.clone();
        Collection<ItemStack> values = player.getInventory().addItem(itemToAdd).values();
        if (!values.isEmpty()) for (ItemStack i : values) {
            player.getWorld().dropItem(player.getLocation(), i);
        }
    }

    @Override
    public void onClaimed(Mail mail, Player player) {
        String message = Internal.attachmentItemClaimedMessage;
        if (!message.isEmpty()) {
            Util.sendMessage(player, Pair.replace(message, Pair.of("%item%", ItemStackUtil.miniTranslate(item))));
        }
    }

    @Override
    public ItemStack generateDraftIcon(Player target) {
        ItemStack item = this.item.clone();
        List<String> loreRemove = Internal.getLoreRemove(target);
        if (!loreRemove.isEmpty()) {
            List<String> lore = ItemStackUtil.getItemLoreAsMiniMessage(item);
            lore.addAll(loreRemove);
            ItemStackUtil.setItemLore(item, lore);
        }
        return item;
    }

    @Override
    public ItemStack generateIcon(Player target) {
        return item.clone();
    }

    public String getName() {
        return ItemStackUtil.miniTranslate(item);
    }

    @Override
    public String toString() {
        String name = getName();
        int amount = item.getAmount();
        if (amount <= 1) return Messages.Draft.attachments__item__display.str(Pair.of("%item%", name));
        return Messages.Draft.attachments__item__display_with_amount.str(Pair.of("%item%", name), Pair.of("%amount%", amount));
    }

    @Override
    public String serialize() {
        return "item:" + ItemStackUtil.itemStackToBase64(item);
    }

    @Override
    public boolean isLegal() {
        if (Internal.itemBanMaterials.contains(item.getType())) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String display = meta.hasDisplayName() ? meta.getDisplayName() : null;
            List<String> lore = meta.hasLore() ? meta.getLore() : null;
            if (display == null) {
                if (Internal.itemBanName.contains("")) return false;
            } else {
                if (Internal.itemBanName.contains("*")) return false;
                for (String s : Internal.itemBanName) {
                    if (s.isEmpty()) continue;
                    if (display.contains(s)) return false;
                }
            }
            if ((lore == null || lore.isEmpty())) {
                //noinspection RedundantIfStatement
                if (Internal.itemBanLore.contains("")) return false;
            } else {
                if (Internal.itemBanLore.contains("*")) return false;
                String allLore = String.join("\n", lore);
                for (String s : Internal.itemBanLore) {
                    if (s.isEmpty()) continue;
                    if (allLore.contains(s)) return false;
                }
            }
        }
        return true;
    }

    public static class Gui extends AbstractAddAttachmentGui {
        boolean selected = false;
        public Gui(Player player) {
            super(player);
        }

        @Override
        public Inventory newInventory() {
            return created = plugin.getGuiManager().getInventory(this, player, 9, getTitleText());
        }

        private String getTitleText() {
            return Messages.Draft.attachments__item__title.str();
        }

        @Override
        public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
            event.setCancelled(true);
            if (!click.isShiftClick() && click.isLeftClick() && action.equals(InventoryAction.PICKUP_ALL)) {
                if (currentItem != null && !currentItem.getType().equals(Material.AIR) && currentItem.getAmount() > 0) {
                    AttachmentItem attachment = AttachmentItem.build(currentItem);
                    if (!attachment.isLegal()) {
                        Messages.Draft.attachments__item__banned.tm(player);
                        return;
                    }
                    event.setCurrentItem(null);
                    addAttachment(attachment);
                    selected = true;
                    backToDraftGui();
                }
            }
        }

        @Override
        public void onClose(InventoryView view) {
            if (!selected) {
                backToSelectTypeGui();
            }
        }
    }

    public static void register() {
        IAttachment.registerAttachment(AttachmentItem.class, PERM,
                Internal::attachmentItem, Gui::new,
                (s) -> { // deserializer
                    if (s.startsWith("item:")) {
                        ItemStack item = ItemStackUtil.itemStackFromBase64(s.substring(5));
                        if (item != null) {
                            return new AttachmentItem(item);
                        }
                    }
                    return null;
                });
    }
}
