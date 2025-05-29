package top.mrxiaom.sweetmail.func.basic;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;
import top.mrxiaom.sweetmail.gui.IGui;
import top.mrxiaom.sweetmail.utils.ItemStackUtil;
import top.mrxiaom.sweetmail.utils.MiniMessageConvert;
import top.mrxiaom.sweetmail.utils.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuiManager extends AbstractPluginHolder implements Listener {
    final Map<UUID, IGui> playersGui = new HashMap<>();
    private final boolean hasOffHand = MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_9_R1);
    private boolean supportSetTitle = checkSupportSetTitle();
    public GuiManager(SweetMail plugin) {
        super(plugin);
        registerEvents(this);
        register();
    }

    private boolean checkSupportSetTitle() {
        try {
            InventoryView.class.getDeclaredMethod("setTitle", String.class);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        supportSetTitle = checkSupportSetTitle()
                && config.getBoolean("gui.switch-smoothly", true);
    }

    public Inventory getInventory(IGui gui, Player player, int size, String titleStr) {
        InventoryView view = player.getOpenInventory();
        Inventory inv = view.getTopInventory();
        Component title = MiniMessageConvert.miniMessage(titleStr);
        if (supportSetTitle // 要求服务端支持，且在插件配置开启了选项
                && inv.getSize() == size // 要求界面大小相同
                && inv.getHolder() instanceof BaseHolder // 要求之前使用同样方法生成的界面
        ) {
            BaseHolder holder = (BaseHolder) inv.getHolder();
            holder.view = true;
            holder.updateGui(gui, title);
            view.setTitle(LegacyComponentSerializer.legacySection().serialize(title));
            // 清空界面内容，接着用
            for (int i = 0; i < inv.getSize(); i++) {
                inv.setItem(i, null);
            }
            return inv;
        } else {
            BaseHolder holder = new BaseHolder(plugin, player, gui, title);
            holder.view = false;
            holder.setInventory(plugin.getInventoryFactory().create(holder, size, titleStr));
            return holder.getInventory();
        }
    }

    public void openGui(IGui gui) {
        Player player = gui.getPlayer();
        if (player == null) return;
        Inventory inv = gui.newInventory();
        if (inv != null) {
            if (inv.getHolder() instanceof BaseHolder) {
                BaseHolder holder = (BaseHolder) inv.getHolder();
                if (!holder.view) {
                    player.closeInventory();
                } else {
                    holder.getGui().onClose(player.getOpenInventory());
                }
                playersGui.put(player.getUniqueId(), gui);
                holder.openInventory();
            } else {
                player.closeInventory();
                playersGui.put(player.getUniqueId(), gui);
                player.openInventory(inv);
            }
        } else {
            player.closeInventory();
            warn("试图为玩家 " + player.getName() + " 打开界面 " + gui.getClass().getName() + " 时，程序返回了 null");
        }
    }

    public void onDisable() {
        for (Map.Entry<UUID, IGui> entry : playersGui.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) continue;
            entry.getValue().onClose(player.getOpenInventory());
            player.closeInventory();
            Util.sendTitle(player, "§e请等等", "§f管理员正在热更新插件", 10, 30, 10);
        }
        playersGui.clear();
    }

    public boolean hasFlag(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;
        return NBT.get(item, nbt -> {
            return nbt.hasTag(ItemStackUtil.FLAG);
        });
    }

    @Nullable
    public IGui getOpeningGui(Player player) {
        return playersGui.get(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (player.hasMetadata("NPC")) return;
        IGui remove = playersGui.remove(player.getUniqueId());
        if (remove != null) {
            remove.onClose(player.getOpenInventory());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled() || !(event.getWhoClicked() instanceof Player)) return;
        if (event.getWhoClicked().hasMetadata("NPC")) return;
        IGui openedGui = playersGui.get(event.getWhoClicked().getUniqueId());
        if (openedGui != null) {
            openedGui.onClick(event.getAction(), event.getClick(), event.getSlotType(),
                    event.getRawSlot(), event.getCurrentItem(), event.getCursor(), event.getView(), event);
        } else if (hasFlag(event.getCurrentItem())) {
            event.setCurrentItem(null);
        }
    }

    @SuppressWarnings({"deprecation"})
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (player.hasMetadata("NPC")) return;
        if (!playersGui.containsKey(player.getUniqueId())) {
            if (hasFlag(e.getItem())) {
                e.setUseItemInHand(Event.Result.DENY);
                if (hasOffHand) {
                    if (e.getHand() != null) {
                        player.getInventory().setItem(e.getHand(), null);
                    }
                } else {
                    player.setItemInHand(null);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDrop(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        if (player.hasMetadata("NPC")) return;
        if (!playersGui.containsKey(player.getUniqueId())) {
            if (hasFlag(e.getItemDrop().getItemStack())) {
                e.setCancelled(true);
                e.getItemDrop().remove();
            }
        }
    }

    @SuppressWarnings({"deprecation"})
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPickup(PlayerPickupItemEvent e) {
        Player player = e.getPlayer();
        if (player.hasMetadata("NPC")) return;
        if (!playersGui.containsKey(player.getUniqueId())) {
            if (hasFlag(e.getItem().getItemStack())) {
                e.setCancelled(true);
                e.getItem().remove();
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.isCancelled() || !(event.getWhoClicked() instanceof Player)) return;
        if (event.getWhoClicked().hasMetadata("NPC")) return;
        IGui openedGui = playersGui.get(event.getWhoClicked().getUniqueId());
        if (openedGui != null) {
            openedGui.onDrag(event.getView(), event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        if (event.getPlayer().hasMetadata("NPC")) return;
        IGui openedGui = playersGui.remove(event.getPlayer().getUniqueId());
        if (openedGui != null) {
            openedGui.onClose(event.getView());
        }
    }
}
