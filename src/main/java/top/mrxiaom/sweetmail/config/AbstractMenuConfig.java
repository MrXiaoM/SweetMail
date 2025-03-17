package top.mrxiaom.sweetmail.config;

import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;
import top.mrxiaom.sweetmail.gui.IGui;
import top.mrxiaom.sweetmail.utils.ColorHelper;
import top.mrxiaom.sweetmail.utils.ItemStackUtil;
import top.mrxiaom.sweetmail.utils.Pair;
import top.mrxiaom.sweetmail.utils.Util;
import top.mrxiaom.sweetmail.utils.comp.PAPI;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static top.mrxiaom.sweetmail.utils.Pair.replace;

public abstract class AbstractMenuConfig<T extends IGui> extends AbstractPluginHolder {
    public static class Icon {
        public final String material;
        public final boolean glow;
        @Nullable
        public final String display;
        @Nullable
        public final Integer customModel;
        public final List<String> lore;
        List<String> leftClick = null;
        List<String> rightClick = null;
        List<String> shiftLeftClick = null;
        List<String> shiftRightClick = null;
        List<String> dropClick = null;

        private Icon(String material, boolean glow, @Nullable String display, @Nullable Integer customModel, List<String> lore) {
            this.material = material;
            this.glow = glow;
            this.display = display;
            this.customModel = customModel;
            this.lore = lore;
        }

        @SafeVarargs
        public final ItemStack generateIcon(OfflinePlayer target, ItemStack item, Pair<String, Object>... replacements) {
            if (item.getType().equals(Material.AIR)) return item;
            if (display != null) {
                ItemStackUtil.setItemDisplayName(item, PAPI.setPlaceholders(target, replace(display, replacements)));
            }
            if (customModel != null) {
                ItemStackUtil.setCustomModelData(item, customModel);
            }
            if (!lore.isEmpty()) {
                ItemStackUtil.setItemLore(item, PAPI.setPlaceholders(target, replace(lore, replacements)));
            }
            if (glow) {
                ItemStackUtil.setGlow(item);
            }
            NBT.modify(item, nbt -> {
                nbt.setBoolean(ItemStackUtil.FLAG, true);
            });
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
            String material = section.getString(key + ".material", "STONE");
            boolean glow = section.getBoolean(key + ".glow", false);
            String display = section.getString(key + ".display", null);
            Integer customModel = section.contains(key + ".custom-model")
                    ? section.getInt(key + ".custom-model")
                    : null;
            List<String> lore = section.getStringList(key + ".lore");
            Icon icon = new Icon(material, glow, display, customModel, lore);
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
    protected String title;
    public char[] inventory;
    protected YamlConfiguration config;
    public AbstractMenuConfig(SweetMail plugin, String file) {
        super(plugin);
        this.configFile = new File(plugin.getDataFolder(), this.file = file);
        this.register();
    }

    public Character getSlotKey(int slot) {
        if (slot < 0 || slot >= inventory.length) return null;
        return inventory[slot];
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
    protected abstract boolean loadMainIcon(ConfigurationSection section, String key, Icon loadedIcon);

    /**
     * 生成界面图标
     * @param key 键
     * @param target 玩家
     * @param iconIndex 该键在界面配置中第几次出现，从0数起
     * @return 图标物品
     */
    protected abstract ItemStack tryApplyMainIcon(T gui, String key, Player target, int iconIndex);
    public Inventory createInventory(T gui, Player target) {
        return plugin.getInventoryFactory().create(gui, inventory.length, PAPI.setPlaceholders(target, title));
    }
    public void applyIcons(T gui, Inventory inv, Player target) {
        applyIcons(gui, inv::setItem, target);
    }
    public void applyIcons(T gui, InventoryView inv, Player target) {
        applyIcons(gui, inv::setItem, target);
        Util.updateInventory(target);
    }
    public void applyIcons(T gui, BiConsumer<Integer, ItemStack> setItem, Player target) {
        for (int i = 0; i < this.inventory.length; i++) {
            applyIcon(gui, setItem, target, i);
        }
    }
    public void applyIcon(T gui, Inventory inv, Player target, int i) {
        applyIcon(gui, inv::setItem, target, i);
    }
    public void applyIcon(T gui, InventoryView inv, Player target, int i) {
        applyIcon(gui, inv::setItem, target, i);
    }
    public void applyIcon(T gui, BiConsumer<Integer, ItemStack> setItem, Player target, int i) {
        if (i >= this.inventory.length) return;
        char c = this.inventory[i];
        String key = String.valueOf(c);
        if (key.equals(" ") || key.equals("　")) {
            setItem.accept(i, null);
            return;
        }
        int index = getKeyIndex(c, i);
        ItemStack item = tryApplyMainIcon(gui, key, target, index);
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

    public void handleClick(Player player, ClickType click, char key) {
        AbstractMenuConfig.Icon icon = otherIcon.get(String.valueOf(key));
        if (icon != null) {
            icon.click(player, click);
        }
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        if (!configFile.exists()) {
            plugin.saveResource(file, true);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        ConfigurationSection section;
        title = ColorHelper.parseColor(config.getString("title", "菜单标题"));
        inventory = String.join("", config.getStringList("inventory")).toCharArray();
        List<Character> ignored = config.getCharacterList("ignored");

        boolean flag = false;
        clearMainIcons();
        section = config.getConfigurationSection("items");
        if (section != null) for (String key : section.getKeys(false)) {
            Icon icon = Icon.getIcon(section, key, false);
            if (loadMainIcon(section, key, icon)) {
                char c = key.charAt(0);
                if (!contains(inventory, c) && !ignored.contains(c)) {
                    if (!flag) {
                        flag = true;
                        warn("菜单配置 " + configFile.getName() + " 存在缺失的物品，可能由你的菜单配置版本过低导致，具体缺失情况如下，请从默认配置复制相关配置");
                        warn("https://github.com/MrXiaoM/SweetMail/blob/main/src/main/resources/" + file);
                    }
                    warn("[" + configFile.getName() + "] 界面布局 `inventory` 没有添加必选物品 `" + key + "`");
                }
            }
        }

        otherIcon.clear();
        section = config.getConfigurationSection("other-items");
        if (section != null) for (String key : section.getKeys(false)) {
            Icon icon = Icon.getIcon(section, key, true);
            otherIcon.put(key, icon);
        }
    }

    private static boolean contains(char[] chars, char c) {
        for (char i : chars) {
            if (i == c) return true;
        }
        return false;
    }
}
