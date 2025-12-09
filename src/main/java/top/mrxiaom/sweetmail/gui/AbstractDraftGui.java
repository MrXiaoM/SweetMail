package top.mrxiaom.sweetmail.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;
import top.mrxiaom.sweetmail.func.DraftManager;
import top.mrxiaom.sweetmail.func.data.Draft;

public abstract class AbstractDraftGui extends AbstractPluginHolder implements IGui {
    protected final Player player;
    protected final Draft draft;
    protected final Runnable reopen = () -> plugin.getScheduler().runNextTick((t_) -> open());
    protected Inventory created;
    public AbstractDraftGui(SweetMail plugin, Player player) {
        super(plugin);
        this.player = player;
        this.draft = DraftManager.inst().getDraft(player);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return created;
    }

    public Draft getDraft() {
        return draft;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
