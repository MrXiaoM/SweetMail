package top.mrxiaom.sweetmail.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.config.MenuDraftConfig;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;
import top.mrxiaom.sweetmail.func.DraftManager;

public abstract class AbstractDraftGui extends AbstractPluginHolder implements IGui {
    protected Player player;
    protected DraftManager.Draft draft;
    protected Runnable reopen = () ->
            Bukkit.getScheduler().runTask(plugin,
                    () -> plugin.getGuiManager().openGui(this));
    public AbstractDraftGui(SweetMail plugin, Player player) {
        super(plugin);
        this.player = player;
        this.draft = DraftManager.inst().getDraft(player);
    }

    public DraftManager.Draft getDraft() {
        return draft;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

}
