package top.mrxiaom.sweetmail.func.basic;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.gui.IGui;
import top.mrxiaom.sweetmail.utils.Util;

public class BaseHolder implements InventoryHolder {
    private @NotNull final SweetMail plugin;
    private @NotNull final Player player;
    private @NotNull IGui gui;
    private @NotNull Component title;
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

    public void updateGui(@NotNull IGui gui, Component title) {
        this.gui = gui;
        this.title = title;
    }

    public void setInventory(@NotNull Inventory inventory) {
        this.inventory = inventory;
    }

    public void openInventory() {
        InventoryView opened = player.getOpenInventory();
        if (view && opened.getTopInventory().getType().isCreatable()) {
            String downSampling = LegacyComponentSerializer.legacySection().serialize(title);
            opened.setTitle(downSampling);
            plugin.getScheduler().runNextTick((t) -> Util.updateInventory(player));
        } else {
            player.openInventory(getInventory());
        }
    }
}
