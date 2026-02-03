package top.mrxiaom.sweetmail.func;

import com.google.common.io.ByteArrayDataOutput;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import top.mrxiaom.sweetmail.Messages;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.database.entry.MailCountInfo;
import top.mrxiaom.sweetmail.utils.Util;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NoticeManager extends AbstractPluginHolder implements Listener {
    boolean noticeBungee;
    String noticeSenderKey;
    String noticeReceiverKey;
    public NoticeManager(SweetMail plugin) {
        super(plugin);
        registerEvents(this);
        registerBungee();
        register();
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        noticeBungee = config.getBoolean("bungeecord.enable", true);
        noticeSenderKey = config.getString("bungeecord.sender-key", "");
        noticeReceiverKey = config.getString("bungeecord.receiver-key", "");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (!player.hasPermission("sweetmail.notice")) return;
        plugin.getScheduler().runTaskAsync(() -> {
            MailCountInfo mailCountInfo = plugin.getMailDatabase().getInBoxCount(player, true);
            if (mailCountInfo.unreadCount > 0) {
                notice(player, Messages.Join.text.str(), mailCountInfo.unreadCount);
            }
        });
    }

    @Override
    public void receiveBungee(String subChannel, DataInputStream in) throws IOException {
        if (noticeBungee && subChannel.equals("SweetMail_Notice")) {
            String key = in.readUTF();
            if (!key.contains(noticeReceiverKey)) return;
            int length = in.readInt();
            List<Player> noticePlayers = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                Util.getOnlinePlayerByNameOrUUID(in.readUTF()).ifPresent(noticePlayers::add);
            }
            plugin.getScheduler().runTaskAsync(() -> {
                for (Player player : noticePlayers) {
                    plugin.getMailDatabase().getInBoxCount(player, true);
                    notice(player, Messages.Join.text_online.str(), 1);
                }
            });
        }
    }

    public void noticeNew(List<String> receivers) {
        if (receivers.isEmpty()) return;
        List<String> players = new ArrayList<>();
        for (String s : receivers) {
            Player player = Util.getOnlinePlayerByNameOrUUID(s).orElse(null);
            if (player != null) {
                plugin.getMailDatabase().getInBoxCount(player, true);
                notice(player, Messages.Join.text_online.str(), 1);
            } else {
                players.add(s);
            }
        }
        noticeToBungee(players);
    }

    private void noticeToBungee(List<String> players) {
        if (!noticeBungee || players.isEmpty()) return;
        Player player = Util.getAnyPlayerOrNull();
        if (player == null) return;
        plugin.getScheduler().runTaskAsync(() -> {
            ByteArrayDataOutput out = Util.newDataOutput();
            out.writeUTF("Forward");
            out.writeUTF("ALL");
            out.writeUTF("SweetMail_Notice");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream msgOut = new DataOutputStream(bytes)) {
                msgOut.writeUTF(noticeSenderKey);
                msgOut.writeInt(players.size());
                for (String s : players) {
                    msgOut.writeUTF(s);
                }
            } catch (Throwable t) {
                warn(t);
                return;
            }
            out.writeShort(bytes.toByteArray().length);
            out.write(bytes.toByteArray());
            byte[] message = out.toByteArray();
            player.sendPluginMessage(plugin, "BungeeCord", message);
        });
    }

    private void notice(Player player, String msg, int count) {
        if (msg.isEmpty() || !player.hasPermission("sweetmail.notice")) return;
        Component component = Util.miniMessage(msg.replace("%count%", String.valueOf(count)));
        String msgJoinHover = Messages.Join.hover.str();
        String msgJoinCmd = Messages.Join.command.str();
        if (!msgJoinHover.isEmpty()) {
            component = component.hoverEvent(Util.miniMessage(msgJoinHover).asHoverEvent());
        }
        if (!msgJoinCmd.isEmpty()) {
            component = component.clickEvent(ClickEvent.runCommand(msgJoinCmd));
        }
        Util.sendMessage(player, component);
    }

    public static NoticeManager inst() {
        return instanceOf(NoticeManager.class);
    }
}
