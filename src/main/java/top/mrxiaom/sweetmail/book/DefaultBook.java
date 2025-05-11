package top.mrxiaom.sweetmail.book;

import net.kyori.adventure.inventory.Book;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;
import top.mrxiaom.sweetmail.func.data.Draft;
import top.mrxiaom.sweetmail.gui.IGui;
import top.mrxiaom.sweetmail.utils.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DefaultBook extends AbstractPluginHolder implements IBook, Listener {
    private boolean enableReturnWhenMove = false;
    public Map<UUID, Listener> listeners = new HashMap<>();
    public DefaultBook(SweetMail plugin) {
        super(plugin);
        registerEvents(this);
        register();
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        enableReturnWhenMove = config.getBoolean("book.return-when-move");
    }

    @Override
    public void openBook(Player player, Draft draft) {
        IGui gui = plugin.getGuiManager().getOpeningGui(player);
        Book book = Util.legacyBook(draft.content, player.getName());
        Util.openBook(player, book);
        afterOpenBook(gui);
    }

    @Override
    public void openBook(Player player, Mail mail) {
        IGui gui = plugin.getGuiManager().getOpeningGui(player);
        Util.openBook(player, mail.generateBook());
        afterOpenBook(gui);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        handleListenerUpdate(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        handleListenerUpdate(e.getPlayer().getUniqueId());
    }

    private void handleListenerUpdate(UUID uuid) {
        Listener old = listeners.remove(uuid);
        if (old != null) {
            HandlerList.unregisterAll(old);
        }
    }

    private void afterOpenBook(IGui gui) {
        if (gui == null || !enableReturnWhenMove) return;
        UUID uuid = gui.getPlayer().getUniqueId();
        handleListenerUpdate(uuid);
        Listener listener = new Listener() {
            final long checkStartTime = System.currentTimeMillis() + 1000L;
            boolean done = false;
            @EventHandler
            public void onMove(PlayerMoveEvent e) {
                if (e.isCancelled() || System.currentTimeMillis() < checkStartTime) return;
                if (done) {
                    HandlerList.unregisterAll(this);
                    return;
                }
                if (e.getPlayer().getUniqueId().equals(uuid)) {
                    done = true;
                    HandlerList.unregisterAll(this);
                    gui.open();
                }
            }
        };
        Bukkit.getPluginManager().registerEvents(listener, plugin);
        listeners.put(uuid, listener);
    }
}
