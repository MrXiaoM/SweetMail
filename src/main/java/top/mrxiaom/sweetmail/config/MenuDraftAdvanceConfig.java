package top.mrxiaom.sweetmail.config;

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
import top.mrxiaom.sweetmail.func.DraftManager;
import top.mrxiaom.sweetmail.gui.AbstractDraftGui;
import top.mrxiaom.sweetmail.utils.comp.PAPI;

import static top.mrxiaom.sweetmail.utils.Pair.replace;

public class MenuDraftAdvanceConfig extends AbstractMenuConfig<MenuDraftAdvanceConfig.Gui> {
    Icon iconBack;
    public MenuDraftAdvanceConfig(SweetMail plugin) {
        super(plugin, "menus/draft_advance.yml");
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
        switch (key) {
            case "返": {
                iconBack = loadedIcon;
                break;
            }
        }
    }

    @Override
    public Inventory createInventory(Gui gui, Player target) {
        return Bukkit.createInventory(null, inventory.length, replace(PAPI.setPlaceholders(target, title)));
    }

    @Override
    protected ItemStack tryApplyMainIcon(Gui gui, String key, Player target, int iconIndex) {
        DraftManager manager = DraftManager.inst();
        DraftManager.Draft draft = manager.getDraft(target);
        switch (key) {
            case "返": {
                return iconBack.generateIcon(target);
            }
        }
        return null;
    }

    public static MenuDraftAdvanceConfig inst() {
        return get(MenuDraftAdvanceConfig.class).orElseThrow(IllegalStateException::new);
    }

    public class Gui extends AbstractDraftGui {
        protected MenuDraftAdvanceConfig config;
        public Gui(SweetMail plugin, Player player) {
            super(plugin, player);
            config = MenuDraftAdvanceConfig.inst();
        }


        @Override
        public Inventory newInventory() {
            Inventory inv = config.createInventory(this, player);
            config.applyIcons(this, inv, player);
            return inv;
        }

        @Override
        @SuppressWarnings({"deprecation"})
        public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
            Character c = config.getSlotKey(slot);
            if (c == null) return;
            event.setCancelled(true);

            switch (String.valueOf(c)) {
                case "返": {
                    if (click.isLeftClick() && !click.isShiftClick()) {
                        MenuDraftConfig draft = MenuDraftConfig.inst();
                        plugin.getGuiManager().openGui(draft.new Gui(plugin, player));
                    }
                    return;
                }
                default: {
                    config.handleClick(player, click, c);
                }
            }
        }
    }
}
