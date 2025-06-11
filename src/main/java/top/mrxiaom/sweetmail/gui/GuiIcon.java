package top.mrxiaom.sweetmail.gui;

import de.tr7zw.changeme.nbtapi.NBT;
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
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.config.gui.MenuDraftConfig;
import top.mrxiaom.sweetmail.depend.PAPI;
import top.mrxiaom.sweetmail.func.data.MailIcon;
import top.mrxiaom.sweetmail.utils.ItemStackUtil;
import top.mrxiaom.sweetmail.utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiIcon extends AbstractDraftGui {
    private final String title;
    private final Map<Integer, String> iconKeyMap = new HashMap<>();
    private int size;
    private boolean idle = true;
    public GuiIcon(SweetMail plugin, Player player, String title) {
        super(plugin, player);
        this.title = title;
    }

    @Override
    public Inventory newInventory() {
        iconKeyMap.clear();
        List<Pair<String, MailIcon>> pairs = new ArrayList<>();
        for (Map.Entry<String, MailIcon> entry : draft.manager.getMailIcons().entrySet()) {
            if (player.hasPermission("sweetmail.icon." + entry.getKey())) {
                pairs.add(Pair.of(entry));
            }
        }
        size = Math.min(54, ((pairs.size() / 9) + 1) * 9);
        String titleStr = getTitleText();
        created = plugin.getGuiManager().getInventory(this, player, size, titleStr);
        for (int i = 0, j = 0; i < pairs.size(); i++) {
            Pair<String, MailIcon> pair = pairs.get(i);
            MailIcon icon = pair.getValue();
            ItemStack item;
            try {
                item = ItemStackUtil.getItem(getPlayer(), icon.item);
            } catch (Throwable t) {
                warn(t.getMessage());
                continue;
            }
            if (icon.display != null && !pair.getKey().equals(icon.display)) {
                ItemStackUtil.setItemDisplayName(item, "&r" + icon.display);
            }
            if (draft.iconKey.equals(pair.getKey())) {
                ItemStackUtil.setGlow(item);
                List<String> lore = ItemStackUtil.getItemLoreAsMiniMessage(item);
                lore.addAll(PAPI.setPlaceholders(player, Messages.Draft.selected_icon_lore.list()));
                ItemStackUtil.setItemLore(item, lore);
            }
            NBT.modify(item, nbt -> {
                nbt.setBoolean(ItemStackUtil.FLAG, true);
            });
            iconKeyMap.put(j, pair.getKey());
            created.setItem(j, item);
            j++;
        }
        return created;
    }

    private String getTitleText() {
        return PAPI.setPlaceholders(player, title);
    }

    @Override
    public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
        event.setCancelled(true);
        MenuDraftConfig menu = MenuDraftConfig.inst();
        if (slot == -1) {
            idle = false;
            menu.new Gui(plugin, player).open();
            return;
        }
        if (click.isLeftClick() && !click.isShiftClick()) {
            if (slot >= 0 && slot < size) {
                String key = iconKeyMap.get(slot);
                if (key != null) {
                    draft.iconKey = key;
                    draft.save();
                    idle = false;
                    menu.new Gui(plugin, player).open();
                    return;
                }
            }
            if (slot >= size && player.hasPermission("sweetmail.icon.custom")) {
                if (currentItem != null && !currentItem.getType().equals(Material.AIR)) {
                    String type;
                    ItemMeta meta = currentItem.getItemMeta();
                    if (meta != null && ItemStackUtil.hasCustomModelData(currentItem)) {
                        type = "!" + currentItem.getType().name().toUpperCase() + "#" +  ItemStackUtil.getCustomModelData(currentItem);
                    } else {
                        type = "!" + currentItem.getType().name().toUpperCase();
                    }
                    draft.iconKey = type;
                    draft.save();
                    idle = false;
                    menu.new Gui(plugin, player).open();
                }
            }
        }
    }

    @Override
    public void onClose(InventoryView view) {
        if (idle) {
            MenuDraftConfig.inst().new Gui(plugin, player).open();
        }
    }
}
