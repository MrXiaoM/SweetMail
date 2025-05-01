package top.mrxiaom.sweetmail.config.gui;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.attachments.IAttachment;
import top.mrxiaom.sweetmail.config.AbstractMenuConfig;
import top.mrxiaom.sweetmail.gui.AbstractDraftGui;
import top.mrxiaom.sweetmail.gui.IGui;
import top.mrxiaom.sweetmail.depend.PAPI;

import java.util.HashMap;
import java.util.Map;

import static top.mrxiaom.sweetmail.utils.Pair.replace;

public class MenuAddAttachmentConfig extends AbstractMenuConfig<MenuAddAttachmentConfig.Gui> {
    Icon iconBack;
    public MenuAddAttachmentConfig(SweetMail plugin) {
        super(plugin, "menus/add_attachment.yml");
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        super.reloadConfig(cfg);
    }

    @Override
    protected void clearMainIcons() {
        iconBack = null;
    }

    @Override
    protected boolean loadMainIcon(ConfigurationSection section, String key, Icon loadedIcon) {
        if (key.equals("返")) {
            iconBack = loadedIcon;
            return true;
        }
        return false;
    }

    @Override
    public Inventory createInventory(Gui gui, Player target) {
        return plugin.getInventoryFactory().create(gui, inventory.length, replace(PAPI.setPlaceholders(target, title)));
    }

    @Override
    protected ItemStack tryApplyMainIcon(Gui gui, String key, Player target, int iconIndex) {
        switch (key) {
            case "附": {
                if (iconIndex < 0 || iconIndex >= gui.attachments.size()) return null;
                IAttachment.Internal.AttachmentInfo<?> info = gui.attachments.get(iconIndex);
                return info.icon.apply(target);
            }
            case "返": {
                return iconBack.generateIcon(gui, target);
            }
        }
        return null;
    }

    public static MenuAddAttachmentConfig inst() {
        return instanceOf(MenuAddAttachmentConfig.class);
    }

    public class Gui extends AbstractDraftGui {
        private final Map<Integer, IAttachment.Internal.AttachmentInfo<?>> attachments = new HashMap<>();
        public Gui(SweetMail plugin, Player player) {
            super(plugin, player);
        }

        @Override
        public Inventory newInventory() {
            int i = 0;
            attachments.clear();
            for (IAttachment.Internal.AttachmentInfo<?> info : IAttachment.getAttachments()) {
                if (player.hasPermission(info.permission)) {
                    attachments.put(i++, info);
                }
            }
            created = createInventory(this, player);
            applyIcons(this, created, player);
            return created;
        }

        @Override
        public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
            Character c = getSlotKey(slot);
            if (c == null) return;
            event.setCancelled(true);

            switch (String.valueOf(c)) {
                case "返": {
                    if (click.isLeftClick() && !click.isShiftClick()) {
                        MenuDraftConfig.inst().new Gui(plugin, player).open();
                    }
                    break;
                }
                case "附": {
                    int i = getKeyIndex(c, slot);
                    IAttachment.Internal.AttachmentInfo<?> info = attachments.get(i);
                    if (info != null && player.hasPermission(info.permission)) {
                        IGui gui = info.addGui.apply(player);
                        if (gui != null) {
                            gui.open();
                        } else {
                            player.closeInventory();
                        }
                    }
                    break;
                }
                default:
                    handleClick(player, click, c);
            }
        }
    }
}
