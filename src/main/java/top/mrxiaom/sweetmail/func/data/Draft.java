package top.mrxiaom.sweetmail.func.data;

import org.bukkit.configuration.file.YamlConfiguration;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.attachments.IAttachment;
import top.mrxiaom.sweetmail.func.DraftManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Draft {
    public final String sender;
    public String receiver = "";
    public String iconKey = "default";
    public String title;
    public List<String> content = new ArrayList<>();
    public List<IAttachment> attachments = new ArrayList<>();
    public String advSenderDisplay = null;
    public String advReceivers = null;
    public final DraftManager manager;
    private Draft(DraftManager manager, String sender) {
        this.manager = manager;
        this.title = manager.defaultTitle();
        this.sender = sender;
    }

    public void reset() {
        receiver = "";
        iconKey = "default";
        title = manager.defaultTitle();
        content = new ArrayList<>();
        attachments = new ArrayList<>();
        advSenderDisplay = null;
        advReceivers = null;
        save();
    }

    public static Draft load(DraftManager manager, String player) {
        File file = new File(manager.dataFolder, player + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        Draft draft = new Draft(manager, player);
        draft.receiver = config.getString("receiver", "");
        draft.iconKey = config.getString("icon_key", "default");
        draft.title = config.getString("title", manager.defaultTitle());
        draft.content = config.getStringList("content");
        List<IAttachment> attachments = new ArrayList<>();
        List<String> list = config.getStringList("attachments");
        for (String s : list) {
            IAttachment attachment = IAttachment.deserialize(s);
            if (attachment != null) {
                attachments.add(attachment);
            }
        }
        draft.attachments = attachments;
        draft.advSenderDisplay = config.getString("advance.sender_display", null);
        draft.advReceivers = config.getString("advance.receivers", null);
        if (!file.exists()) draft.save();
        return draft;
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("sender", sender);
        config.set("receiver", receiver);
        config.set("icon_key", iconKey);
        config.set("title", title);
        config.set("content", content);
        List<String> attachmentsList = new ArrayList<>();
        for (IAttachment attachment : attachments) {
            attachmentsList.add(attachment.serialize());
        }
        config.set("attachments", attachmentsList);
        if (advSenderDisplay != null) config.set("advance.sender_display", advSenderDisplay);
        if (advReceivers != null) config.set("advance.receivers", advReceivers);
        try {
            File file = new File(manager.dataFolder, sender + ".yml");
            config.save(file);
        } catch (Throwable t) {
            SweetMail.warn(t);
        }
    }
}
