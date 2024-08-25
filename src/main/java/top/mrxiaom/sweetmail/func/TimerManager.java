package top.mrxiaom.sweetmail.func;

import org.bukkit.Bukkit;
import org.bukkit.configuration.MemoryConfiguration;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.func.data.Draft;

public class TimerManager extends AbstractPluginHolder {

    public TimerManager(SweetMail plugin) {
        super(plugin);
        Bukkit.getScheduler().runTaskTimer(plugin, this::everySecond, 20L, 20L);
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        // TODO: 加载定时发送队列
    }

    public void sendInTime(Draft draft, long timestamp) {
        // TODO: 加入到定时发送队列
        save();
    }

    public void save() {
        // TODO: 保存定时发送队列
    }

    private void everySecond() {
        // TODO: 检查定时发送队列中是否有邮件已到达发送时间
    }

    public static TimerManager inst() {
        return get(TimerManager.class).orElseThrow(IllegalStateException::new);
    }
}
