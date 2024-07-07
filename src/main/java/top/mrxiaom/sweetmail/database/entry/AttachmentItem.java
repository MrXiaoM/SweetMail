package top.mrxiaom.sweetmail.database.entry;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.mrxiaom.sweetmail.utils.ItemStackUtil;

import java.util.Collection;
import java.util.List;

public class AttachmentItem implements IAttachment {
    ItemStack item;

    public AttachmentItem(ItemStack item) {
        this.item = item;
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

    @Override
    public String toString() {
        if (item.getItemMeta() != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName() && !meta.getDisplayName().isEmpty()) return meta.getDisplayName();
        }
        return "<translate:" + item.getTranslationKey() + ">";
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
