package top.mrxiaom.sweet.mail.func;

import top.mrxiaom.sweet.mail.SweetMailPlugin;

import java.io.File;

public abstract class AbstractGuiModule extends top.mrxiaom.pluginbase.func.AbstractGuiModule<SweetMailPlugin> {
    public AbstractGuiModule(SweetMailPlugin plugin, File file) {
        super(plugin, file);
    }
}
