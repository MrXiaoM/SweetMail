package top.mrxiaom.sweetmail.gui;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.utils.ItemStackUtil;
import top.mrxiaom.sweetmail.utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiIcon extends AbstractDraftGui {
    String title;
    Map<Integer, String> iconKeyMap = new HashMap<>();
    int size;
    public GuiIcon(SweetMail plugin, Player player, String title) {
        super(plugin, player);
        this.title = title;
    }

    @Override
    public Inventory newInventory() {
        iconKeyMap.clear();
        List<Pair<String, String>> pairs = new ArrayList<>();
        for (Map.Entry<String, String> entry : draft.manager.getMailIcons().entrySet()) {
            if (player.hasPermission("sweetmail.icon." + entry.getKey())) {
                pairs.add(Pair.of(entry));
            }
        }
        size = Math.min(54, ((pairs.size() / 9) + 1) * 9);
        Inventory inv = Bukkit.createInventory(null, size, PlaceholderAPI.setPlaceholders(player, title));
        for (int i = 0; i < pairs.size(); i++) {
            Pair<String, String> pair = pairs.get(i);
            ItemStack item = ItemStackUtil.getItem(pair.getValue());
            if (draft.iconKey.equals(pair.getKey())) {
                ItemStackUtil.setGlow(item);
                List<String> lore = ItemStackUtil.getItemLore(item);
                // TODO: 添加“已选择”Lore
                ItemStackUtil.setItemLore(item, lore);
            }
            iconKeyMap.put(i, pair.getKey());
            inv.setItem(i, item);
        }
        return inv;
    }

    @Override
    public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
        event.setCancelled(true);
        if (slot == -1) {
            plugin.getGuiManager().openGui(new GuiDraft(plugin, player));
            return;
        }
        if (click.isLeftClick() && !click.isShiftClick()) {
            if (slot >= 0 && slot < size) {
                String key = iconKeyMap.get(slot);
                if (key != null) {
                    draft.iconKey = key;
                    draft.save();
                    plugin.getGuiManager().openGui(new GuiDraft(plugin, player));
                    return;
                }
            }
            if (slot >= size && player.hasPermission("sweetmail.icon.custom")) {
                if (currentItem != null && !currentItem.getType().isAir()) {
                    String type;
                    ItemMeta meta = currentItem.getItemMeta();
                    if (meta != null && meta.hasCustomModelData()) {
                        type = "!" + currentItem.getType().name().toUpperCase() + "#" + meta.getCustomModelData();
                    } else {
                        type = "!" + currentItem.getType().name().toUpperCase();
                    }
                    draft.iconKey = type;
                    draft.save();
                    plugin.getGuiManager().openGui(new GuiDraft(plugin, player));
                }
            }
        }
    }
}
