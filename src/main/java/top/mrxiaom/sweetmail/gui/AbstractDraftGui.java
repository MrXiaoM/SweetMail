package top.mrxiaom.sweetmail.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.InventoryView;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.config.MenuDraftConfig;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;
import top.mrxiaom.sweetmail.func.DraftManager;

public abstract class AbstractDraftGui extends AbstractPluginHolder implements IGui {
    protected Player player;
    protected MenuDraftConfig config;
    protected DraftManager.Draft draft;
    public AbstractDraftGui(SweetMail plugin, Player player) {
        super(plugin);
        this.player = player;
        this.config = MenuDraftConfig.inst();
        this.draft = DraftManager.inst().getDraft(player);
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public void onDrag(InventoryView view, InventoryDragEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void onClose(InventoryView view) {

    }
}
