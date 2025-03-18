package top.mrxiaom.sweetmail.config;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.config.entry.Template;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static top.mrxiaom.sweetmail.utils.Util.mkdirs;

public class TemplateConfig extends AbstractPluginHolder {
    private final File configFolder;
    private final Map<String, Template> templates = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public TemplateConfig(SweetMail plugin) {
        super(plugin);
        configFolder = new File(plugin.getDataFolder(), "templates");
        register();
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        if (!configFolder.exists()) {
            mkdirs(configFolder);
            plugin.saveResource("templates/example.yml", true);
        }
        templates.clear();
        File[] files = configFolder.listFiles();
        if (files != null) for (File file : files) {
            if (file.isDirectory()) continue;
            String name = file.getName();
            if (!name.endsWith(".yml")) continue;
            String id = name.substring(0, name.length() - 4);
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            Template loaded = Template.load(this, config, id);
            templates.put(id, loaded);
        }
        info("加载了 " + templates.size() + " 个邮件模板");
    }

    public Set<String> keys() {
        return templates.keySet();
    }

    @Nullable
    public Template get(String id) {
        return templates.get(id);
    }

    public static TemplateConfig inst() {
        return instanceOf(TemplateConfig.class);
    }
}
