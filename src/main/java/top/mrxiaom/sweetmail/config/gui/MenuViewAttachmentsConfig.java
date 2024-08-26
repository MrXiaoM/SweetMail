package top.mrxiaom.sweetmail.config.gui;

import org.bukkit.Bukkit;
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
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.gui.IGui;
import top.mrxiaom.sweetmail.utils.comp.PAPI;

import static top.mrxiaom.sweetmail.utils.Pair.replace;

public class MenuViewAttachmentsConfig extends AbstractMenuConfig<MenuViewAttachmentsConfig.Gui> {
    Icon iconBack;
    public MenuViewAttachmentsConfig(SweetMail plugin) {
        super(plugin, "menus/view_attachments.yml");
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
    protected void loadMainIcon(ConfigurationSection section, String key, Icon loadedIcon) {
        if (key.equals("返")) {
            iconBack = loadedIcon;
        }
    }

    @Override
    public Inventory createInventory(Gui gui, Player target) {
        return Bukkit.createInventory(null, inventory.length, replace(PAPI.setPlaceholders(target, title)));
    }

    @Override
    protected ItemStack tryApplyMainIcon(Gui gui, String key, Player target, int iconIndex) {
        Mail mail = gui.mail;
        switch (key) {
            case "附": {
                if (iconIndex < 0 || iconIndex >= mail.attachments.size()) return null;
                IAttachment attachment = mail.attachments.get(iconIndex);
                return attachment.generateIcon(target);
            }
            case "返": {
                return iconBack.generateIcon(target);
            }
        }
        return null;
    }

    public static MenuViewAttachmentsConfig inst() {
        return get(MenuViewAttachmentsConfig.class).orElseThrow(IllegalStateException::new);
    }

    public class Gui implements IGui {
        private final IGui parent;
        private final Player player;
        private final Mail mail;
        public Gui(IGui parent, Player player, Mail mail) {
            this.parent = parent;
            this.player = player;
            this.mail = mail;
        }

        @Override
        public Player getPlayer() {
            return player;
        }

        @Override
        public Inventory newInventory() {
            Inventory inv = createInventory(this, player);
            applyIcons(this, inv, player);
            return inv;
        }

        @Override
        public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
            Character c = getSlotKey(slot);
            if (c == null) return;
            event.setCancelled(true);

            switch (String.valueOf(c)) {
                case "返": {
                    if (click.isLeftClick() && !click.isShiftClick()) {
                        parent.open();
                    }
                }
                break;
                case "附":
                    break;
                default:
                    handleClick(player, click, c);
            }
        }
    }
}
