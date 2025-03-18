package top.mrxiaom.sweetmail;

import top.mrxiaom.sweetmail.func.language.IHolderAccessor;
import top.mrxiaom.sweetmail.func.language.LanguageEnumAutoHolder;

import java.util.List;

import static top.mrxiaom.sweetmail.func.language.LanguageEnumAutoHolder.wrap;

public enum Messages implements IHolderAccessor {
    ;

    Messages(String defaultValue) {
        holder = wrap(this, defaultValue);
    }
    Messages(String... defaultValue) {
        holder = wrap(this, defaultValue);
    }
    Messages(List<String> defaultValue) {
        holder = wrap(this, defaultValue);
    }
    private final LanguageEnumAutoHolder<Messages> holder;
    public LanguageEnumAutoHolder<Messages> holder() {
        return holder;
    }
}
