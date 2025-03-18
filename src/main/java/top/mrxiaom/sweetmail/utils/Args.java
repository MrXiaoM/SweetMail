package top.mrxiaom.sweetmail.utils;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static top.mrxiaom.sweetmail.utils.Util.toCharList;

public class Args {
    private static final Map<Character, Character> quotes = new HashMap<Character, Character>() {{
        put('"', '"');
        put('\'', '\'');
    }};
    @SuppressWarnings("SpellCheckingInspection")
    private static final List<Character> allowInKey = toCharList("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-$");
    final Map<String, String> map;
    private Args(Map<String, String> map) {
        this.map = map;
    }

    @Nullable
    public String get(String key, String def) {
        return map.getOrDefault(key, def);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        boolean flag = false;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (flag) {
                sb.append(", ");
            } else {
                flag = true;
            }
            sb.append(entry.getKey());
            sb.append(":");
            sb.append('"').append(entry.getValue().replace("\"", "\\\"")).append('"');
        }
        sb.append("}");
        return sb.toString();
    }

    private static String toString(List<Character> list) {
        StringBuilder sb = new StringBuilder();
        for (Character c : list) {
            sb.append(c);
        }
        list.clear();
        return sb.toString();
    }

    public static Result<Args> parse(String str) {
        Map<String, String> map = new HashMap<>();
        List<Character> store = new ArrayList<>();
        String key = null;
        Character quote = null;
        char[] array = str.toCharArray();
        for (int i = 0; i < array.length; i++) {
            char c = array[i];
            if (key == null) { // 键
                if (store.isEmpty() && c == ' ') continue;
                if (c == '=') {
                    if (store.isEmpty()) {
                        return Result.fail("P" + (i+1) + ": 未输入键名");
                    }
                    key = toString(store);
                    if (i + 1 < array.length) {
                        Character ch = quotes.get(array[i + 1]);
                        if (ch != null) {
                            quote = ch;
                            i++;
                        }
                    }
                    continue;
                }
                if (!allowInKey.contains(c)) {
                    return Result.fail("P" + (i+1) + ": 不支持的字符 '" + c + "'");
                }
            } else { // 值
                if (c == '\\') { // 转义
                    if (i + 1 < array.length) {
                        char ch = array[i+1];
                        if (ch == '\\') {
                            store.add('\\');
                            i++;
                            continue;
                        }
                        if (quote != null && ch == quote) {
                            store.add(quote);
                            i++;
                            continue;
                        }
                    }
                    return Result.fail("P" + (i+1) + ": 无效转义");
                }
                if (c == ' ') {
                    if (quote != null) {
                        store.add(' ');
                        continue;
                    }
                    String value = toString(store);
                    map.put(key, value);
                    key = null;
                    continue;
                }
                if (quote != null && c == quote) {
                    String value = toString(store);
                    map.put(key, value);
                    key = null;
                    quote = null;
                    continue;
                }
            }
            store.add(c);
        }
        if (key != null) {
            if (quote == null) {
                map.put(key, toString(store));
            } else {
                return Result.fail("P" + str.length() + "：缺少" + quote);
            }
        }
        if (!store.isEmpty()) {
            int len = str.length();
            return Result.fail("P" + (len-store.size()) + "-" + len + ": 无法解析 " + toString(store));
        }

        return Result.success(new Args(map));
    }
}
