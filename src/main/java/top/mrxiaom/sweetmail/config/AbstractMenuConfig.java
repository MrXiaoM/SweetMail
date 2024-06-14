package top.mrxiaom.sweetmail.config;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;
import top.mrxiaom.sweetmail.utils.ColorHelper;
import top.mrxiaom.sweetmail.utils.ItemStackUtil;
import top.mrxiaom.sweetmail.utils.PAPI;
import top.mrxiaom.sweetmail.utils.Pair;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static top.mrxiaom.sweetmail.utils.Pair.replace;

public abstract class AbstractMenuConfig extends AbstractPluginHolder {
    public static class Icon {
        String material;
        boolean glow;
        String display;
        List<String> lore;
        List<String> leftClick = null;
        List<String> rightClick = null;
        List<String> shiftLeftClick = null;
        List<String> shiftRightClick = null;
        List<String> dropClick = null;

        private Icon() {
        }

        @SafeVarargs
        public final ItemStack generateIcon(OfflinePlayer target, ItemStack item, Pair<String, Object>... replacements) {
            if (display != null) {
                ItemStackUtil.setItemDisplayName(item, PAPI.setPlaceholders(target, replace(display, replacements)));
            }
            if (!lore.isEmpty()) {
                ItemStackUtil.setItemLore(item, PAPI.setPlaceholders(target, replace(lore, replacements)));
            }
            if (glow) {
                ItemStackUtil.setGlow(item);
            }
            return item;
        }
        @SafeVarargs
        public final ItemStack generateIcon(OfflinePlayer target, Pair<String, Object>... replacements) {
            ItemStack item = ItemStackUtil.getItem(material);
            return generateIcon(target, item, replacements);
        }

        public void click(Player player, ClickType type) {
            List<String> commands = null;
            switch (type) {
                case LEFT:
                    commands = leftClick;
                    break;
                case RIGHT:
                    commands = rightClick;
                    break;
                case SHIFT_LEFT:
                    commands = shiftLeftClick;
                    break;
                case SHIFT_RIGHT:
                    commands = shiftRightClick;
                    break;
                case DROP:
                    commands = dropClick;
                    break;
            }
            if (commands == null || commands.isEmpty()) return;
            commands = PAPI.setPlaceholders(player, commands);
            for (String s : commands) {
                if (s.startsWith("[console]")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.substring(9).trim());
                }
                if (s.startsWith("[player]")) {
                    Bukkit.dispatchCommand(player, s.substring(8).trim());
                }
                if (s.startsWith("[message]")) {
                    t(player, s.substring(9));
                }
            }
        }

        public static Icon getIcon(ConfigurationSection section, String key, boolean commands) {
            Icon icon = new Icon();
            icon.material = section.getString(key + ".material", "STONE");
            icon.glow = section.getBoolean(key + ".glow", false);
            icon.display = section.getString(key + ".display", null);
            icon.lore = section.getStringList(key + ".lore");
            if (commands) {
                icon.leftClick = section.getStringList(key + ".left-click-commands");
                icon.rightClick = section.getStringList(key + ".right-click-commands");
                icon.shiftLeftClick = section.getStringList(key + ".shift-left-click-commands");
                icon.shiftRightClick = section.getStringList(key + ".shift-right-click-commands");
                icon.dropClick = section.getStringList(key + ".drop-commands");
            }
            return icon;
        }
    }
    File configFile;
    String file;
    public Map<String, Icon> otherIcon = new HashMap<>();
    String title;
    public char[] inventory;
    public AbstractMenuConfig(SweetMail plugin, String file) {
        super(plugin);
        this.configFile = new File(plugin.getDataFolder(), this.file = file);
        this.register();
    }

    /**
     * 清空主图标列表
     */
    protected abstract void clearMainIcons();

    /**
     * 加载主图标
     * @param section items:
     * @param key 键
     * @param loadedIcon 已加载的基本图标信息
     */
    protected abstract void loadMainIcon(ConfigurationSection section, String key, Icon loadedIcon);

    /**
     * 生成界面图标
     * @param key 键
     * @param target 玩家
     * @param iconIndex 该键在界面配置中第几次出现，从0数起
     * @return 图标物品
     */
    protected abstract ItemStack tryApplyMainIcon(String key, Player target, int iconIndex);
    public Inventory createInventory(Player target) {
        return Bukkit.createInventory(null, inventory.length, PAPI.setPlaceholders(target, title));
    }
    public void applyIcons(Inventory inv, Player target) {
        applyIcons(inv::setItem, target);
    }
    public void applyIcons(InventoryView inv, Player target) {
        applyIcons(inv::setItem, target);
    }
    public void applyIcons(BiConsumer<Integer, ItemStack> setItem, Player target) {
        for (int i = 0; i < this.inventory.length; i++) {
            applyIcon(setItem, target, i);
        }
    }
    public void applyIcon(Inventory inv, Player target, int i) {
        applyIcon(inv::setItem, target, i);
    }
    public void applyIcon(InventoryView inv, Player target, int i) {
        applyIcon(inv::setItem, target, i);
    }
    public void applyIcon(BiConsumer<Integer, ItemStack> setItem, Player target, int i) {
        if (i >= this.inventory.length) return;
        char c = this.inventory[i];
        String key = String.valueOf(c);
        if (key.equals(" ") || key.equals("　")) {
            setItem.accept(i, null);
            return;
        }
        int index = getKeyIndex(c, i);
        ItemStack item = tryApplyMainIcon(key, target, index);
        if (item != null) {
            setItem.accept(i, item);
            return;
        }
        Icon icon = otherIcon.get(key);
        if (icon != null) {
            setItem.accept(i, icon.generateIcon(target));
        } else {
            setItem.accept(i, null);
        }
    }
    public int getKeyIndex(char key, int i) {
        int index = 0;
        for (int j = 0; j < i; j++) {
            if (this.inventory[j] == key) {
                index++;
            }
        }
        return index;
    }
    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        if (!configFile.exists()) {
            plugin.saveResource(file, true);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        ConfigurationSection section;
        title = ColorHelper.parseColor(config.getString("title", "菜单标题"));
        inventory = String.join("", config.getStringList("inventory")).toCharArray();

        clearMainIcons();
        section = config.getConfigurationSection("items");
        if (section != null) for (String key : section.getKeys(false)) {
            Icon icon = Icon.getIcon(section, key, false);
            loadMainIcon(section, key, icon);
        }

        otherIcon.clear();
        section = config.getConfigurationSection("other-items");
        if (section != null) for (String key : section.getKeys(false)) {
            Icon icon = Icon.getIcon(section, key, true);
            otherIcon.put(key, icon);
        }
    }
}
