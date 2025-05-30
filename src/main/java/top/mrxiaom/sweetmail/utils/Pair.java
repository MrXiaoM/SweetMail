package top.mrxiaom.sweetmail.utils;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Pair<K, V> {
    K key;
    V value;

    private Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    @SuppressWarnings({"unchecked"})
    public <S, T> Pair<S, T> cast() {
        return (Pair<S, T>) this;
    }

    public static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair<>(key, value);
    }

    public static <K, V> Pair<K, V> of(Map.Entry<K, V> entry) {
        return new Pair<>(entry.getKey(), entry.getValue());
    }

    @SafeVarargs
    public static List<String> replace(List<String> list, Pair<String, Object>... replacements) {
        if (replacements.length == 0) return new ArrayList<>(list);
        List<String> newList = new ArrayList<>();
        for (String s : list) {
            newList.add(replace(s, replacements));
        }
        return newList;
    }

    public static List<String> replace(List<String> list, Iterable<Pair<String, Object>> replacements) {
        List<String> newList = new ArrayList<>();
        for (String s : list) {
            newList.add(replace(s, replacements));
        }
        return newList;
    }

    @SafeVarargs
    public static String replace(String s, Pair<String, Object>... replacements) {
        if (replacements.length == 0) return s;
        String str = s;
        for (Pair<String, Object> pair : replacements) {
            if (str.contains(pair.key)) {
                str = str.replace(pair.key, String.valueOf(pair.value));
            }
        }
        return str;
    }

    public static String replace(String s, @Nullable Iterable<Pair<String, Object>> replacements) {
        if (replacements == null) return s;
        String str = s;
        for (Pair<String, Object> pair : replacements) {
            if (str.contains(pair.key)) {
                str = str.replace(pair.key, String.valueOf(pair.value));
            }
        }
        return str;
    }
}
