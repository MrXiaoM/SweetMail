package top.mrxiaom.sweetmail.func.language;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.sweetmail.func.LanguageManager;
import top.mrxiaom.sweetmail.utils.Pair;

import java.util.List;
import java.util.stream.Collectors;

import static top.mrxiaom.sweetmail.utils.Pair.replace;

public abstract class AbstractLanguageHolder {
    public final String key;
    public final boolean isList;
    public final Object defaultValue;

    public AbstractLanguageHolder(@NotNull String key, List<String> defaultValue) {
        this.key = key;
        this.isList = true;
        this.defaultValue = defaultValue;
    }
    public AbstractLanguageHolder(@NotNull String key, String defaultValue) {
        this.key = key;
        this.isList = false;
        this.defaultValue = defaultValue;
    }

    public abstract LanguageManager getLanguageManager();

    @SuppressWarnings({"unchecked"})
    private <T> T getOrDefault(T value) {
        return value == null ? (T) defaultValue : value;
    }

    public String str() {
        LanguageManager lang = getLanguageManager();
        if (isList) {
            List<String> list = getOrDefault(lang.getAsList(key));
            return String.join("\n", list);
        } else {
            return getOrDefault(lang.getAsString(key));
        }
    }
    public String str(Object... args) {
        return String.format(str(), args);
    }
    @SafeVarargs
    public final String str(Pair<String, Object>... replacements) {
        return replace(str(), replacements);
    }
    public String str(Iterable<Pair<String, Object>> replacements) {
        return replace(str(), replacements);
    }
    public List<String> list() {
        LanguageManager lang = getLanguageManager();
        if (isList) {
            return getOrDefault(lang.getAsList(key));
        } else {
            String str = getOrDefault(lang.getAsString(key));
            return Lists.newArrayList(str.split("\n"));
        }
    }
    public List<String> list(Object... args) {
        return list().stream()
                .map(it -> String.format(it, args))
                .collect(Collectors.toList());
    }
    @SafeVarargs
    public final List<String> list(Pair<String, Object>... replacements) {
        return list().stream()
                .map(it -> replace(it, replacements))
                .collect(Collectors.toList());
    }
    public List<String> list(Iterable<Pair<String, Object>> replacements) {
        return list().stream()
                .map(it -> replace(it, replacements))
                .collect(Collectors.toList());
    }
}
