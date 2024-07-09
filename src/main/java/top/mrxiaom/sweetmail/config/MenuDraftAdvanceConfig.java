package top.mrxiaom.sweetmail.config;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.commands.CommandMain;
import top.mrxiaom.sweetmail.database.entry.IAttachment;
import top.mrxiaom.sweetmail.func.DraftManager;
import top.mrxiaom.sweetmail.gui.GuiDraft;
import top.mrxiaom.sweetmail.gui.GuiDraftAdvance;
import top.mrxiaom.sweetmail.utils.ItemStackUtil;
import top.mrxiaom.sweetmail.utils.Pair;
import top.mrxiaom.sweetmail.utils.Util;
import top.mrxiaom.sweetmail.utils.comp.PAPI;

import static top.mrxiaom.sweetmail.utils.Pair.replace;

public class MenuDraftAdvanceConfig extends AbstractMenuConfig<GuiDraftAdvance> {
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
    public Inventory createInventory(GuiDraftAdvance gui, Player target) {
        return Bukkit.createInventory(null, inventory.length, replace(PAPI.setPlaceholders(target, title)));
    }

    @Override
    protected ItemStack tryApplyMainIcon(GuiDraftAdvance gui, String key, Player target, int iconIndex) {
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
}
