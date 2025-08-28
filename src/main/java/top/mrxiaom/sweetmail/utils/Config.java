package top.mrxiaom.sweetmail.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class Config {
    public static YamlConfiguration load(File file) {
        return load(new YamlConfiguration(), file);
    }

    public static YamlConfiguration load(File file, Charset charset) {
        return load(new YamlConfiguration(), file, charset);
    }

    public static <T extends FileConfiguration> T load(T config, File file) {
        return load(config, file, StandardCharsets.UTF_8);
    }

    public static <T extends FileConfiguration> T load(T config, File file, Charset charset) {
        try (FileInputStream stream = new FileInputStream(file)) {
            config.load(new InputStreamReader(stream, charset));
        } catch (FileNotFoundException ignored) {
        } catch (IOException | InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, ex);
        }
        return config;
    }

    public static void save(FileConfiguration config, File file) throws IOException {
        save(config, file, StandardCharsets.UTF_8);
    }

    public static void save(FileConfiguration config, File file, Charset charset) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            Util.mkdirs(parent);
        }
        String data = config.saveToString();

        try (FileOutputStream output = new FileOutputStream(file);
             Writer writer = new OutputStreamWriter(output, charset)) {
            writer.write(data);
        }
    }
}
