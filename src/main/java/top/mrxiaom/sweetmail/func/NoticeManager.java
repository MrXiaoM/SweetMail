package top.mrxiaom.sweetmail.func;

import com.google.common.io.ByteArrayDataOutput;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.utils.ColorHelper;
import top.mrxiaom.sweetmail.utils.Util;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NoticeManager extends AbstractPluginHolder implements Listener {
    String msgJoinText;
    String msgJoinTextOnline;
    List<String> msgJoinHover;
    String msgJoinCmd;
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

        msgJoinText = config.getString("messages.join.text", "");
        msgJoinTextOnline = config.getString("messages.join.text-online", "");
        msgJoinHover = config.getStringList("messages.join.hover");
        msgJoinCmd = config.getString("messages.join.command", "");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (!player.hasPermission("sweetmail.notice")) return;
        String targetKey = plugin.getPlayerKey(player);
        int count = plugin.getMailDatabase().getInBox(true, targetKey, 1, 1).getMaxCount();
        if (count > 0) {
            notice(player, msgJoinText, count);
        }
    }

    @Override
    public void receiveBungee(String subChannel, DataInputStream in) throws IOException {
        if (noticeBungee && subChannel.equals("SweetMail_Notice")) {
            String key = in.readUTF();
            if (!key.contains(noticeReceiverKey)) return;
            int length = in.readInt();
            for (int i = 0; i < length; i++) {
                Player player = Util.getOnlinePlayerByNameOrUUID(in.readUTF()).orElse(null);
                if (player == null) continue;
                notice(player, msgJoinTextOnline, 1);
            }
        }
    }

    public void noticeNew(List<String> receivers) {
        if (receivers.isEmpty()) return;
        List<String> players = new ArrayList<>();
        for (String s : receivers) {
            Player player = Util.getOnlinePlayerByNameOrUUID(s).orElse(null);
            if (player != null) {
                notice(player, msgJoinTextOnline, 1);
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
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
        TextComponent component = ColorHelper.bungee(msg.replace("%count%", String.valueOf(count)));
        if (!msgJoinHover.isEmpty()) {
            component.setHoverEvent(ColorHelper.hover(msgJoinHover));
        }
        if (!msgJoinCmd.isEmpty()) {
            component.setClickEvent(new ClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    msgJoinCmd
            ));
        }
        player.spigot().sendMessage(component);
    }

    public static NoticeManager inst() {
        return get(NoticeManager.class).orElseThrow(IllegalStateException::new);
    }
}
