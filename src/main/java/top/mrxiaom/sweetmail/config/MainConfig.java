package top.mrxiaom.sweetmail.config;

import org.bukkit.configuration.MemoryConfiguration;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;

public class MainConfig extends AbstractPluginHolder {
    public MainConfig(SweetMail plugin) {
        super(plugin);
        register();
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        // TODO:
    }
}
