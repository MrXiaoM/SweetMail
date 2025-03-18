package top.mrxiaom.sweetmail.depend;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.config.gui.MenuDraftConfig;
import top.mrxiaom.sweetmail.database.entry.MailCountInfo;

public class Placeholder extends PlaceholderExpansion {
    private final SweetMail plugin;
    public Placeholder(SweetMail plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean register() {
        try {
            unregister(); // prevent hot load
        } catch (Throwable ignored) {
        }
        return super.register();
    }

    @Override
    public @NotNull String getIdentifier() {
        return plugin.getDescription().getName().toLowerCase();
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.equalsIgnoreCase("mail_price")) {
            return String.valueOf(MenuDraftConfig.inst().getPrice(player));
        }
        if (params.equalsIgnoreCase("mail_attachments_outdate_days")) {
            return String.valueOf(MenuDraftConfig.inst().getOutdateDays(player));
        }
        if (params.equalsIgnoreCase("mail_draft_outdate_hours")) {
            return String.valueOf(MenuDraftConfig.inst().getDraftOutdateHours(player));
        }
        if (params.equalsIgnoreCase("inbox_total")) {
            MailCountInfo info = plugin.getMailDatabase().getInBoxCount(player);
            return String.valueOf(info.totalCount);
        }
        if (params.equalsIgnoreCase("inbox_unread")) {
            MailCountInfo info = plugin.getMailDatabase().getInBoxCount(player);
            return String.valueOf(info.unreadCount);
        }
        if (params.equalsIgnoreCase("inbox_used")) {
            MailCountInfo info = plugin.getMailDatabase().getInBoxCount(player);
            return String.valueOf(info.usedCount);
        }
        return super.onPlaceholderRequest(player, params);
    }
}
