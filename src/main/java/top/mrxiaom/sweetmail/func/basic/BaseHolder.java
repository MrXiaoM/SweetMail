package top.mrxiaom.sweetmail.func.basic;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.gui.IGui;

public class BaseHolder implements InventoryHolder {
    private final @NotNull SweetMail plugin;
    private final @NotNull Player player;
    private final @NotNull IGui gui;
    private final @NotNull Component title;
    private Inventory inventory;
    protected boolean view = false;
    public BaseHolder(
            @NotNull SweetMail plugin,
            @NotNull Player player,
            @NotNull IGui gui,
            @NotNull Component title
    ) {
        this.plugin = plugin;
        this.player = player;
        this.gui = gui;
        this.title = title;
    }

    @NotNull
    public SweetMail getPlugin() {
        return plugin;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public IGui getGui() {
        return gui;
    }

    @NotNull
    public Component getTitle() {
        return title;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(@NotNull Inventory inventory) {
        this.inventory = inventory;
    }
}
