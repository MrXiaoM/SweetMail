package top.mrxiaom.sweetmail.attachments;

import com.google.common.collect.Lists;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.mrxiaom.sweetmail.utils.ItemStackUtil;
import top.mrxiaom.sweetmail.utils.Pair;

import java.util.Collection;
import java.util.List;

import static top.mrxiaom.sweetmail.utils.Pair.replace;

public class AttachmentItem implements IAttachment {
    private final ItemStack item;

    private AttachmentItem(ItemStack item) {
        this.item = item;
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
        Collection<ItemStack> values = player.getInventory().addItem(item).values();
        if (!values.isEmpty()) for (ItemStack i : values) {
            player.getWorld().dropItem(player.getLocation(), i);
        }
    }

    @Override
    public ItemStack generateDraftIcon(Player target) {
        ItemStack item = this.item.clone();
        if (!Internal.loreRemove.isEmpty()) {
            List<String> lore = ItemStackUtil.getItemLore(item);
            lore.addAll(Internal.loreRemove);
            ItemStackUtil.setItemLore(item, lore);
        }
        return item;
    }

    @Override
    public ItemStack generateIcon(Player target) {
        return item;
    }

    public String getName() {
        if (item.getItemMeta() != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName() && !meta.getDisplayName().isEmpty()) return meta.getDisplayName();
        }
        return ItemStackUtil.miniTranslate(item);
    }

    @Override
    public String toString() {
        String name = getName();
        int amount = item.getAmount();
        if (amount <= 1) return replace(Internal.itemDisplay, Pair.of("%item%", name));
        return replace(Internal.itemDisplayWithAmount, Pair.of("%item%", name), Pair.of("%amount%", amount));
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

    public static void register() {
        IAttachment.registerAttachment(AttachmentItem.class,
                // TODO: 从语言配置读取图标
                (player) -> ItemStackUtil.buildItem(Material.ITEM_FRAME, "物品附件", Lists.newArrayList()),
                (player) -> { throw new NotImplementedException("TODO"); },
                (s) -> {
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
