package top.mrxiaom.sweet.mail.func;

import top.mrxiaom.sweet.mail.SweetMailPlugin;

public abstract class AbstractPluginHolder extends top.mrxiaom.pluginbase.func.AbstractPluginHolder<SweetMailPlugin> {
    public AbstractPluginHolder(SweetMailPlugin plugin) {
        super(plugin);
    }

    public AbstractPluginHolder(SweetMailPlugin plugin, boolean register) {
        super(plugin, register);
    }
}
