package top.mrxiaom.sweetmail.depend;

import top.mrxiaom.sweetmail.SweetMail;

public class PlaceholderPersist extends Placeholder {
    public PlaceholderPersist(SweetMail plugin) {
        super(plugin);
    }

    @Override
    public boolean persist() {
        return true;
    }
}
