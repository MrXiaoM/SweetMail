package top.mrxiaom.sweetmail.func.basic;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;
import top.mrxiaom.sweetmail.gui.IGui;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuiManager extends AbstractPluginHolder implements Listener {
    final Map<UUID, IGui> playersGui = new HashMap<>();

    public GuiManager(SweetMail plugin) {
        super(plugin);
        registerEvents(this);
        register();
    }

    public void openGui(IGui gui) {
        Player player = gui.getPlayer();
        if (player == null) return;
        player.closeInventory();
        playersGui.put(player.getUniqueId(), gui);
        Inventory inv = gui.newInventory();
        if (inv != null) player.openInventory(inv);
    }

    public void onDisable() {
        for (Map.Entry<UUID, IGui> entry : playersGui.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) continue;
            entry.getValue().onClose(player.getOpenInventory());
            player.closeInventory();
            player.sendTitle("§e请等等", "§f管理员正在热更新插件", 10, 30, 10);
        }
        playersGui.clear();
    }

    @Nullable
    public IGui getOpeningGui(Player player) {
        return playersGui.get(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        IGui remove = playersGui.remove(player.getUniqueId());
        if (remove != null) {
            remove.onClose(player.getOpenInventory());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (playersGui.containsKey(event.getWhoClicked().getUniqueId())) {
            playersGui.get(event.getWhoClicked().getUniqueId()).onClick(event.getAction(), event.getClick(), event.getSlotType(),
                    event.getRawSlot(), event.getCurrentItem(), event.getCursor(), event.getView(), event);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (playersGui.containsKey(event.getWhoClicked().getUniqueId())) {
            playersGui.get(event.getWhoClicked().getUniqueId()).onDrag(event.getView(), event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        IGui remove = playersGui.remove(event.getPlayer().getUniqueId());
        if (remove != null) {
            remove.onClose(event.getView());
        }
    }
}
