package top.mrxiaom.sweetmail.attachments;

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
        if (!Text.loreRemove.isEmpty()) {
            List<String> lore = ItemStackUtil.getItemLore(item);
            lore.addAll(Text.loreRemove);
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
        if (amount <= 1) return replace(Text.itemDisplay, Pair.of("%item%", name));
        return replace(Text.itemDisplayWithAmount, Pair.of("%item%", name), Pair.of("%amount%", amount));
    }

    @Override
    public String serialize() {
        return "item:" + ItemStackUtil.itemStackToBase64(item);
    }

    public static IAttachment deserialize(String s) {
        if (s.startsWith("item:")) {
            ItemStack item = ItemStackUtil.itemStackFromBase64(s.substring(5));
            if (item != null) {
                return new AttachmentItem(item);
            }
        }
        return null;
    }
}
