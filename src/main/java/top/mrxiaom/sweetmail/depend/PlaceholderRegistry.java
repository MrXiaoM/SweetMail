package top.mrxiaom.sweetmail.depend;

import top.mrxiaom.sweetmail.SweetMail;

public class PlaceholderRegistry {
    public static void register(SweetMail plugin) {
        Placeholder placeholder;
        try {
            placeholder = new PlaceholderPersist(plugin);
            placeholder.persist();
        } catch (Throwable ignored) {
            placeholder = new Placeholder(plugin);
        }
        placeholder.register();
    }
}
