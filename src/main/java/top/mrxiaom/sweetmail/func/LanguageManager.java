package top.mrxiaom.sweetmail.func;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.func.language.AbstractLanguageHolder;
import top.mrxiaom.sweetmail.func.language.LanguageEnumAutoHolder;
import top.mrxiaom.sweetmail.utils.Util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("UnusedReturnValue")
public class LanguageManager extends AbstractPluginHolder {
    private final Map<String, Object> holderValues = new HashMap<>();
    private final Map<String, AbstractLanguageHolder> holders = new HashMap<>();
    private File file = null;
    public LanguageManager(SweetMail plugin) {
        super(plugin);
        register();
    }

    @Override
    protected int priority() {
        return 0;
    }

    /**
     * 设置语言文件路径
     */
    public LanguageManager setLangFile(@Nullable String langFile) {
        if (langFile == null) {
            return setLangFile((File) null);
        }
        else {
            return setLangFile(new File(plugin.getDataFolder(), langFile));
        }
    }

    /**
     * 设置语言文件路径
     */
    public LanguageManager setLangFile(@Nullable File file) {
        this.file = file;
        return this;
    }

    /**
     * 注册枚举到语言管理器
     * @param enumType 枚举类型
     * @param getter 获取 holder 实例的 getter
     */
    public <T extends Enum<T>> LanguageManager register(Class<T> enumType, Function<T, LanguageEnumAutoHolder<T>> getter) {
        for (T value : enumType.getEnumConstants()) {
            LanguageEnumAutoHolder<T> holder = getter.apply(value);
            holders.put(holder.key, holder);
        }
        return this;
    }

    @Nullable
    public String getAsString(String key) {
        Object obj = holderValues.get(key);
        if (obj instanceof String) {
            return (String) obj;
        }
        return null;
    }

    @Nullable
    @SuppressWarnings({"unchecked"})
    public List<String> getAsList(String key) {
        Object obj = holderValues.get(key);
        if (obj instanceof List<?>) {
            return (List<String>) obj;
        }
        return null;
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        reload();
    }

    public LanguageManager reload() {
        if (file == null || holders.isEmpty()) return this;
        holderValues.clear();
        YamlConfiguration config = Util.load(file);

        YamlConfiguration defaults = new YamlConfiguration(); // 旧版配置兼容
        FileConfiguration pluginConfig = plugin.getConfig();
        ConfigurationSection section = pluginConfig.getConfigurationSection("messages");
        if (section != null) {
            defaults.set("messages", section);
        }
        section = pluginConfig.getConfigurationSection("help");
        if (section != null) {
            defaults.set("messages.help", section);
        }
        config.setDefaults(defaults);

        for (AbstractLanguageHolder holder : holders.values()) {
            if (!config.contains(holder.key)) {
                config.set(holder.key, holder.defaultValue);
                continue;
            }
            if (holder.isList) {
                holderValues.put(holder.key, config.getStringList(holder.key));
            } else {
                holderValues.put(holder.key, config.getString(holder.key));
            }
        }
        try {
            Util.save(config, file);
        } catch (IOException e) {
            warn("更新语言文件时出现异常", e);
        }
        return this;
    }

    public static LanguageManager inst() {
        return instanceOf(LanguageManager.class);
    }
}
