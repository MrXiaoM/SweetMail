package top.mrxiaom.sweetmail.func.basic;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;
import top.mrxiaom.sweetmail.gui.IGui;
import top.mrxiaom.sweetmail.utils.ItemStackUtil;
import top.mrxiaom.sweetmail.utils.MiniMessageConvert;
import top.mrxiaom.sweetmail.utils.Util;

public class GuiManager extends AbstractPluginHolder implements Listener {
    private final boolean hasOffHand = MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_9_R1);
    public GuiManager(SweetMail plugin) {
        super(plugin);
        registerEvents(this);
        register();
    }

    public Inventory getInventory(IGui gui, Player player, int size, String titleStr) {
        Component title = MiniMessageConvert.miniMessage(titleStr);
        BaseHolder holder = new BaseHolder(plugin, player, gui, title);
        holder.view = false;
        holder.setInventory(plugin.getInventoryFactory().create(holder, size, titleStr));
        return holder.getInventory();
    }

    public void openGui(IGui gui) {
        Player player = gui.getPlayer();
        if (player == null) return;
        Inventory inv = gui.newInventory();
        if (inv != null && inv.getHolder() instanceof BaseHolder) {
            player.openInventory(inv);
        } else {
            player.closeInventory();
            warn("试图为玩家 " + player.getName() + " 打开界面 " + gui.getClass().getName() + " 时，程序返回了 null，或者界面未使用 BaseHolder");
        }
    }

    public void onDisable() {
        HandlerList.unregisterAll(this);
        for (Player player : Bukkit.getOnlinePlayers()) {
            InventoryView view = player.getOpenInventory();
            if (view.getTopInventory().getHolder() instanceof BaseHolder) {
                IGui opened = ((BaseHolder) view.getTopInventory().getHolder()).getGui();
                opened.onClose(view);
                player.closeInventory();
                Util.sendTitle(player, "§e请等等", "§f管理员正在热更新插件", 10, 30, 10);
            }
        }
    }

    public boolean hasFlag(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;
        return NBT.get(item, nbt -> {
            return nbt.hasTag(ItemStackUtil.FLAG);
        });
    }

    @Nullable
    public IGui getOpeningGui(Player player) {
        InventoryView view = player.getOpenInventory();
        if (view.getTopInventory().getHolder() instanceof BaseHolder) {
            return ((BaseHolder) view.getTopInventory().getHolder()).getGui();
        }
        return null;
    }

    @SuppressWarnings({"deprecation"})
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (player.hasMetadata("NPC")) return;

        if (getOpeningGui(player) == null && hasFlag(e.getItem())) {
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDrop(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        if (player.hasMetadata("NPC")) return;

        if (getOpeningGui(player) == null && hasFlag(e.getItemDrop().getItemStack())) {
            e.setCancelled(true);
            e.getItemDrop().remove();
        }
    }

    @SuppressWarnings({"deprecation"})
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPickup(PlayerPickupItemEvent e) {
        Player player = e.getPlayer();
        if (player.hasMetadata("NPC")) return;

        if (getOpeningGui(player) == null && hasFlag(e.getItem().getItemStack())) {
            e.setCancelled(true);
            e.getItem().remove();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (player.hasMetadata("NPC")) return;

        IGui gui = getOpeningGui(player);
        if (gui != null) {
            gui.onClose(player.getOpenInventory());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled() || !(event.getWhoClicked() instanceof Player)) return;
        if (event.getWhoClicked().hasMetadata("NPC")) return;

        InventoryView view = event.getView();
        InventoryHolder holder = view.getTopInventory().getHolder();
        if (holder instanceof BaseHolder) {
            ((BaseHolder) holder).getGui().onClick(
                    event.getAction(), event.getClick(), event.getSlotType(),
                    event.getRawSlot(), event.getCurrentItem(), event.getCursor(),
                    view, event);
        } else if (hasFlag(event.getCurrentItem())) {
            event.setCurrentItem(null);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.isCancelled() || !(event.getWhoClicked() instanceof Player)) return;
        if (event.getWhoClicked().hasMetadata("NPC")) return;

        InventoryView view = event.getView();
        InventoryHolder holder = view.getTopInventory().getHolder();
        if (holder instanceof BaseHolder) {
            ((BaseHolder) holder).getGui().onDrag(view, event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        if (event.getPlayer().hasMetadata("NPC")) return;

        InventoryView view = event.getView();
        InventoryHolder holder = view.getTopInventory().getHolder();
        if (holder instanceof BaseHolder) {
            ((BaseHolder) holder).getGui().onClose(view);
        }
    }
}
