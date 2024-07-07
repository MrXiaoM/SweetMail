package top.mrxiaom.sweetmail.func;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.utils.ColorHelper;

import java.util.List;

public class NoticeManager extends AbstractPluginHolder implements Listener {
    String msgJoinText;
    List<String> msgJoinHover;
    String msgJoinCmd;
    public NoticeManager(SweetMail plugin) {
        super(plugin);
        registerEvents(this);
        register();
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        msgJoinText = config.getString("messages.join.text", "");
        msgJoinHover = config.getStringList("messages.join.hover");
        msgJoinCmd = config.getString("messages.join.command", "");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        int count = plugin.getDatabase().getInBox(true, player.getName(), 1, 1).getMaxCount();
        if (count > 0 && !msgJoinText.isEmpty()) {
            TextComponent component = ColorHelper.bungee(msgJoinText.replace("%count%", String.valueOf(count)));
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
    }
}
