package top.mrxiaom.sweetmail.func;

import com.google.common.collect.Lists;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.func.language.AbstractLanguageHolder;
import top.mrxiaom.sweetmail.func.language.LanguageEnumAutoHolder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("UnusedReturnValue")
public class LanguageManager extends AbstractPluginHolder {
    @SuppressWarnings({"rawtypes"})
    private final Map<String, Function> holderGetters = new HashMap<>();
    private final Map<String, Object> holderValues = new HashMap<>();
    private final Map<String, AbstractLanguageHolder> holders = new HashMap<>();
    private File file = null;
    /**
     * 是否禁止在重载配置文件时重载语言文件
     */
    private boolean disableReloadConfig = false;
    /**
     * 语言键前缀
     */
    private String keyPrefix = "";
    public LanguageManager(SweetMail plugin) {
        super(plugin);
        register();
    }

    @Override
    protected int priority() {
        return 0;
    }

    /**
     * 获取语言文件路径
     */
    @Nullable
    public File getLangFile() {
        return file;
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

    public boolean isDisableReloadConfig() {
        return disableReloadConfig;
    }

    public LanguageManager setDisableReloadConfig(boolean disableReloadConfig) {
        this.disableReloadConfig = disableReloadConfig;
        return this;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public LanguageManager setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
        return this;
    }

    /**
     * 注册枚举到语言管理器
     * @param enumType 枚举类型
     * @param getter 获取 holder 实例的 getter
     */
    public <T extends Enum<T>> LanguageManager register(Class<T> enumType, Function<T, LanguageEnumAutoHolder<T>> getter) {
        holderGetters.put(enumType.getName(), getter);
        for (T value : enumType.getEnumConstants()) {
            LanguageEnumAutoHolder<T> holder = getter.apply(value);
            holders.put(holder.key, holder);
        }
        return this;
    }

    @Nullable
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T extends Enum<T>> LanguageEnumAutoHolder<T> getHolderByEnum(T value) {
        Function getter = holderGetters.get(value.getClass().getName());
        if (getter == null) return null;
        return (LanguageEnumAutoHolder<T>) getter.apply(value);
    }

    public List<AbstractLanguageHolder> getHolders() {
        return Lists.newArrayList(holders.values());
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
        if (disableReloadConfig) return;
        reload();
    }

    public LanguageManager reload() {
        if (file == null || holders.isEmpty()) return this;
        holderValues.clear();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.setDefaults(new YamlConfiguration());
        for (AbstractLanguageHolder holder : holders.values()) {
            if (!config.contains(holder.key)) {
                config.set(keyPrefix + holder.key, holder.defaultValue);
                continue;
            }
            if (holder.isList) {
                holderValues.put(keyPrefix + holder.key, config.getStringList(holder.key));
            } else {
                holderValues.put(keyPrefix + holder.key, config.getString(holder.key));
            }
        }
        try {
            config.save(file);
        } catch (IOException e) {
            warn("更新语言文件时出现异常", e);
        }
        return this;
    }

    public static LanguageManager inst() {
        return instanceOf(LanguageManager.class);
    }
}
