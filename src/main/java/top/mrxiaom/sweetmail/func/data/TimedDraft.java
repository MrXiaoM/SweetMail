package top.mrxiaom.sweetmail.func.data;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import top.mrxiaom.sweetmail.IMail;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.func.DraftManager;
import top.mrxiaom.sweetmail.utils.Util;

import java.util.UUID;

public class TimedDraft {
    public final String id;
    public final String senderUUID;
    public final String senderName;
    public final Draft draft;
    public final long timestamp;

    public TimedDraft(String id, String senderUUID, String senderName, Draft draft, long timestamp) {
        this.id = id;
        this.senderUUID = senderUUID;
        this.senderName = senderName;
        this.draft = draft;
        this.timestamp = timestamp;
    }

    public boolean isOutOfTime(long time) {
        return time >= this.timestamp;
    }

    public static TimedDraft loadFromConfig(ConfigurationSection parentSection, String id) {
        String senderUUID = parentSection.getString(id + ".sender-uuid");
        String senderName = parentSection.getString(id + ".sender-name");
        long time = parentSection.getLong(id + ".time");
        ConfigurationSection draftSection = parentSection.getConfigurationSection(id + ".draft");
        if (senderUUID == null || senderName == null || draftSection == null) return null;
        String sender = SweetMail.getInstance().isOnlineMode() ? senderUUID : senderName;
        Draft draft = Draft.loadFromConfig(DraftManager.inst(), draftSection, sender);
        return new TimedDraft(id, senderUUID, senderName, draft, time);
    }

    public static TimedDraft createFromDraft(String id, Draft draft, long timestamp) {
        boolean serverSender = draft.sender.equals(IMail.SERVER_SENDER);
        String senderUUID = serverSender
                ? draft.sender
                : Util.getOfflinePlayerByNameOrUUID(draft.sender)
                    .map(OfflinePlayer::getUniqueId)
                    .map(UUID::toString)
                    .orElseThrow(IllegalStateException::new);
        String senderName = draft.advSenderDisplay != null && !draft.advSenderDisplay.isEmpty()
                ? draft.advSenderDisplay
                : Util.getOfflinePlayerByNameOrUUID(draft.sender)
                    .map(OfflinePlayer::getName)
                    .orElseThrow(IllegalStateException::new);
        return new TimedDraft(id, senderUUID, senderName, draft.deepClone(), timestamp);
    }

    public void saveToConfig(ConfigurationSection parentSection) {
        parentSection.set(id + ".sender-uuid", senderUUID);
        parentSection.set(id + ".sender-name", senderName);
        parentSection.set(id + ".time", timestamp);
        ConfigurationSection draftSection = parentSection.createSection(id + ".draft");
        draft.saveToConfig(draftSection);
    }
}
