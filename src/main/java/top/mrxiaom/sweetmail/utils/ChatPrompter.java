package top.mrxiaom.sweetmail.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class ChatPrompter implements Listener {
    private static final Set<UUID> processing = new HashSet<>();
    Player player;
    Consumer<String> success;
    Runnable fail;
    String cancelPrompt;
    private ChatPrompter() {
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        processing.remove(player.getUniqueId());
        if (e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
            e.setCancelled(true);
            HandlerList.unregisterAll(this);
            String message = e.getMessage();
            e.setMessage("");
            e.setFormat("");

            if (message.equalsIgnoreCase(cancelPrompt)) {
                if (fail != null) fail.run();
            } else {
                if (success != null) success.accept(message);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTeleport(PlayerTeleportEvent e) {
        processing.remove(player.getUniqueId());
        if (e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
            HandlerList.unregisterAll(this);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        processing.remove(player.getUniqueId());
        if (e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
            HandlerList.unregisterAll(this);
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent e) {
        processing.remove(player.getUniqueId());
        if (e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
            HandlerList.unregisterAll(this);
        }
    }

    public static void prompt(JavaPlugin plugin, Player player, String tips, String cancelPrompt, Consumer<String> successCallback, Runnable failCallback) {
        if (processing.contains(player.getUniqueId())) return;
        ChatPrompter prompter = new ChatPrompter();
        prompter.player = player;
        prompter.cancelPrompt = cancelPrompt;
        prompter.success = successCallback;
        prompter.fail = failCallback;
        ColorHelper.t(player, tips);
        Bukkit.getPluginManager().registerEvents(prompter, plugin);
        processing.add(player.getUniqueId());
    }
}
