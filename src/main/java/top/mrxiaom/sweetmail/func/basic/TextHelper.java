package top.mrxiaom.sweetmail.func.basic;

import org.bukkit.configuration.MemoryConfiguration;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TextHelper extends AbstractPluginHolder {
    private DateTimeFormatter formatter;
    public TextHelper(SweetMail plugin) {
        super(plugin);
        register();
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        try {
            formatter = DateTimeFormatter.ofPattern(config.getString("messages.date-time", "yyyy年MM月dd日 HH:mm:ss"));
        } catch (Throwable t) {
            warn("[config.yml] 无法解析 messages.date-time", t);
            formatter = DateTimeFormatter.ISO_DATE_TIME;
        }
    }

    public String toString(LocalDateTime time) {
        return time.format(formatter);
    }
}
