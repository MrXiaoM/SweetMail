package top.mrxiaom.sweet.mail.func;

import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.func.gui.IModel;
import top.mrxiaom.sweet.mail.SweetMailPlugin;

import java.io.File;

public abstract class AbstractGuisModule<M extends IModel> extends top.mrxiaom.pluginbase.func.AbstractGuisModule<SweetMailPlugin, M> {
    public AbstractGuisModule(SweetMailPlugin plugin, String warningPrefix) {
        super(plugin, warningPrefix);
    }
}
