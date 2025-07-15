package top.mrxiaom.sweetmail.utils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.meowj.langutils.lang.LanguageHelper;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTType;
import de.tr7zw.changeme.nbtapi.handler.NBTHandlers;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBTCompoundList;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBTList;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.attachments.IAttachment;
import top.mrxiaom.sweetmail.utils.items.ItemProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

import static top.mrxiaom.sweetmail.utils.Util.*;

@SuppressWarnings({"deprecation", "unused"})
public class ItemStackUtil {
    public static final String FLAG = "SWEETMAIL_MENU_ICON";
    /**
     * 服务端是否原生支持获取物品的翻译键
     */
    private static boolean supportTranslationKey;
    /**
     * 版本是否支持收纳袋
     */
    private static boolean supportBundle;
    /**
     * 是否已安装依赖插件 LangUtils
     */
    private static boolean supportLangUtils;
    /**
     * 版本是否支持物品组件
     * @since Minecraft 1.20.5
     */
    private static boolean itemNbtUseComponentsFormat;
    /**
     * 物品中的文字（物品名称、Lore）是否使用 JSON字符串 格式的文本组件（Component）
     */
    private static boolean textUseComponent;
    /**
     * 物品中的文字（物品名称、Lore）是否使用 NBT Compound 储存文本组件
     * @since Minecraft 1.21.5
     */
    private static boolean componentUseNBT;
    public static String locale = "zh_CN";
    private static final Map<String, ItemProvider> itemProviders = new HashMap<>();
    protected static void init() {
        supportTranslationKey = Util.isPresent("org.bukkit.Translatable");
        supportBundle = Util.isPresent("org.bukkit.inventory.meta.BundleMeta");
        supportLangUtils = isPresent("com.meowj.langutils.lang.LanguageHelper");
        doItemTest();
        SkullsUtil.init();
        ItemProvider.loadBuiltIn(itemProviders);
    }

    private static void doItemTest() {
        itemNbtUseComponentsFormat = MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4);
        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta meta = item.getItemMeta();
        String testDisplayName = "§a§l测试§e§l文本";
        if (meta == null) { // 预料之外的情况
            textUseComponent = false;
            componentUseNBT = false;
        } else {
            meta.setDisplayName(testDisplayName);
            item.setItemMeta(meta);
            if (itemNbtUseComponentsFormat) {
                textUseComponent = true;
                componentUseNBT = NBT.getComponents(item, nbt -> { // 1.21.5 开始，文本组件从 JSON 字符串改为了 NBT 组件
                    NBTType type = nbt.getType("minecraft:custom_name");
                    return !type.equals(NBTType.NBTTagString);
                });
            } else {
                // 测试物品是否支持使用 component
                NBT.get(item, nbt -> {
                    ReadableNBT display = nbt.getCompound("display");
                    if (display == null) {
                        textUseComponent = false;
                        return;
                    }
                    String name = display.getString("Name");
                    // 旧版本文本组件不支持 JSON 字符串，设置旧版颜色符之后，物品名会跟之前一样
                    textUseComponent = !name.equals(testDisplayName);
                });
                componentUseNBT = false;
            }
        }
    }

    public static ComponentSerializer<Component, ?, String> serializer() {
        if (textUseComponent) {
            return GsonComponentSerializer.gson();
        } else {
            return LegacyComponentSerializer.legacySection();
        }
    }

    public static String miniTranslate(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName()) {
                String nameAdventure = getItemDisplayNameAsMiniMessage(item);
                if (nameAdventure != null) {
                    return nameAdventure;
                }
                String nameBukkit = meta.getDisplayName();
                if (!nameBukkit.isEmpty()) {
                    return MiniMessageConvert.legacyToMiniMessage(nameBukkit);
                }
            }
        }
        if (supportTranslationKey && textUseComponent) {
            return "<translate:" + item.getTranslationKey() + ">";
        }
        if (supportLangUtils) {
            return LanguageHelper.getItemName(item, locale);
        }
        return item.getType().name();
    }

    public static ItemStack resolveBundle(Player player, ItemStack item, List<IAttachment> attachments) {
        if (!supportBundle) return item;
        try {
            ItemMeta m = item.getItemMeta();
            if (m instanceof BundleMeta) {
                BundleMeta meta = (BundleMeta) m;
                meta.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM);
                meta.setItems(new ArrayList<>());
                int count = 0;
                for (IAttachment attachment : attachments) {
                    meta.addItem(attachment.generateIcon(player));
                    if (++count >= SweetMail.getInstance().bundleMaxSlots) {
                        break;
                    }
                }
                item.setItemMeta(meta);
            }
        } catch (Throwable ignored) {
        }
        return item;
    }

    public static boolean hasCustomModelData(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        try {
            return meta.hasCustomModelData();
        } catch (LinkageError e) {
            return false;
        }
    }

    public static Integer getCustomModelData(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        try {
            if (meta.hasCustomModelData()) {
                return meta.getCustomModelData();
            } else {
                return null;
            }
        } catch (LinkageError e) {
            return null;
        }
    }

    public static void setCustomModelData(ItemStack item, Integer customModelData) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        try {
            meta.setCustomModelData(customModelData);
            item.setItemMeta(meta);
        } catch (LinkageError ignored) {
        }
    }

    public static ItemMeta setSkullOwner(ItemMeta meta, OfflinePlayer owner) {
        if (meta instanceof SkullMeta) {
            SkullMeta skull = (SkullMeta) meta;
            try {
                skull.setOwningPlayer(owner);
            } catch (Throwable ignored) {
                skull.setOwner(owner.getName());
            }
        }
        return meta;
    }

    public static String itemStackToBase64(ItemStack item) {
        if (item == null) return "";
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(output)) {
            out.writeObject(item);
            return Base64Coder.encodeLines(output.toByteArray());
        } catch (Throwable t) {
            SweetMail.warn(t);
            return "";
        }
    }

    public static ItemStack itemStackFromBase64(String s) {
        if (s.trim().isEmpty()) return null;
        try (ByteArrayInputStream in = new ByteArrayInputStream(Base64Coder.decodeLines(s));
             BukkitObjectInputStream out = new BukkitObjectInputStream(in)) {
            return (ItemStack) out.readObject();
        } catch (Throwable t) {
            SweetMail.warn(t);
            return null;
        }
    }

    @Nullable
    public static String getItemDisplayNameAsMiniMessage(ItemStack item) {
        Component component = getItemDisplayName(item);
        if (component == null) return null;
        return miniMessage(component);
    }

    @Nullable
    public static Component getItemDisplayName(ItemStack item) {
        String nameAsJson = getItemDisplayNameAsJson(item);
        if (nameAsJson == null) return null;
        return serializer().deserialize(nameAsJson);
    }

    @Nullable
    public static String getItemDisplayNameAsJson(ItemStack item) {
        if (isEmpty(item)) return null;
        if (itemNbtUseComponentsFormat) {
            ReadWriteNBT nbtItem = NBT.itemStackToNBT(item);
            ReadWriteNBT nbt = nbtItem.getCompound("components");
            if (nbt == null) return null;
            if (componentUseNBT) {
                ReadWriteNBT component = nbt.hasTag("minecraft:custom_name")
                        ? nbt.getCompound("minecraft:custom_name")
                        : null;
                return component != null
                        ? component.toString()
                        : null;
            } else {
                return nbt.hasTag("minecraft:custom_name", NBTType.NBTTagString)
                        ? nbt.getString("minecraft:custom_name")
                        : null;
            }
        } else {
            return NBT.get(item, nbt -> {
                ReadableNBT display = nbt.getCompound("display");
                return display != null && display.hasTag("Name", NBTType.NBTTagString)
                        ? display.getString("Name")
                        : null;
            });
        }
    }

    public static List<String> getItemLoreAsMiniMessage(ItemStack item) {
        List<Component> components = getItemLore(item);
        List<String> lore = new ArrayList<>();
        for (Component component : components) {
            String s = miniMessage(component);
            lore.add(s);
        }
        return lore;
    }

    public static List<Component> getItemLore(ItemStack item) {
        List<String> loreAsJson = getItemLoreAsJson(item);
        if (loreAsJson == null) return new ArrayList<>();
        List<Component> lore = new ArrayList<>();
        for (String line : loreAsJson) {
            Component component = serializer().deserialize(line);
            lore.add(component);
        }
        return lore;
    }

    @Nullable
    public static List<String> getItemLoreAsJson(ItemStack item) {
        if (isEmpty(item)) return null;
        if (itemNbtUseComponentsFormat) {
            ReadWriteNBT nbtItem = NBT.itemStackToNBT(item);
            ReadWriteNBT nbt = nbtItem.getCompound("components");
            if (nbt == null) return null;
            if (componentUseNBT) {
                ReadWriteNBTCompoundList components = nbt.hasTag("minecraft:custom_name")
                        ? nbt.getCompoundList("minecraft:custom_name")
                        : null;
                if (components == null) return null;
                List<String> list = new ArrayList<>();
                for (ReadWriteNBT component : components) {
                    list.add(component.toString());
                }
                return list;
            } else {
                return nbt.hasTag("minecraft:custom_name", NBTType.NBTTagList)
                        ? nbt.getStringList("minecraft:lore").toListCopy()
                        : null;
            }
        } else {
            return NBT.get(item, nbt -> {
                ReadableNBT display = nbt.getCompound("display");
                return display != null && display.hasTag("Lore", NBTType.NBTTagList)
                        ? display.getStringList("Lore").toListCopy()
                        : null;
            });
        }
    }

    public static void setItemDisplayName(ItemStack item, String name) {
        if (item == null || item.getType().equals(Material.AIR)) return;
        Component displayName = miniMessage(name);
        String json = serializer().serialize(displayName);
        if (textUseComponent) {
            setItemDisplayNameRaw(item, json);
        } else {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(json);
                item.setItemMeta(meta);
            }
        }
    }

    public static void setItemDisplayNameRaw(ItemStack item, String json) {
        if (itemNbtUseComponentsFormat) {
            NBT.modifyComponents(item, nbt -> {
                if (componentUseNBT) {
                    ReadWriteNBT component = NBT.parseNBT(json);
                    nbt.set("minecraft:custom_name", component, NBTHandlers.STORE_READWRITE_TAG);
                } else {
                    nbt.setString("minecraft:custom_name", json);
                }
            });
        } else {
            NBT.modify(item, nbt -> {
                ReadWriteNBT display = nbt.getOrCreateCompound("display");
                display.setString("Name", json);
            });
        }
    }

    public static void setItemLore(ItemStack item, String... lore) {
        setItemLore(item, Lists.newArrayList(lore));
    }

    public static void setItemLore(ItemStack item, List<String> lore) {
        if (item == null || item.getType().equals(Material.AIR)) return;
        List<String> json = new ArrayList<>();
        for (String s : lore) {
            Component line = miniMessage(s);
            json.add(serializer().serialize(line));
        }
        if (textUseComponent) {
            setItemLoreRaw(item, json);
        } else {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setLore(json);
                item.setItemMeta(meta);
            }
        }
    }

    public static void setItemLoreRaw(ItemStack item, List<String> json) {
        if (itemNbtUseComponentsFormat) {
            NBT.modifyComponents(item, nbt -> {
                if (componentUseNBT) {
                    ReadWriteNBTCompoundList list = nbt.getCompoundList("minecraft:lore");
                    if (!list.isEmpty()) list.clear();
                    for (String s : json) {
                        ReadWriteNBT component = NBT.parseNBT(s);
                        list.addCompound(component);
                    }
                } else {
                    ReadWriteNBTList<String> list = nbt.getStringList("minecraft:lore");
                    if (!list.isEmpty()) list.clear();
                    list.addAll(json);
                }
            });
        } else {
            NBT.modify(item, nbt -> {
                ReadWriteNBT display = nbt.getOrCreateCompound("display");
                ReadWriteNBTList<String> list = display.getStringList("Lore");
                if (!list.isEmpty()) list.clear();
                list.addAll(json);
            });
        }
    }
    @Deprecated
    public static ItemStack buildItem(String material, Integer customModelData, String name, List<String> lore) {
        return buildItem(null, material, customModelData, name, lore);
    }
    @Deprecated
    public static ItemStack buildItem(String material, String name, List<String> lore) {
        return buildItem(null, material, name, lore);
    }
    @Deprecated
    public static ItemStack getItem(String str) {
        return getItem(null, str);
    }

    public static ItemStack buildItem(Player player, String material, String name, List<String> lore) {
        return buildItem(player, material, null, name, lore);
    }
    public static ItemStack buildItem(Player player, String material, Integer customModelData, String name, List<String> lore) {
        if (material.equalsIgnoreCase("AIR")) return new ItemStack(Material.AIR);
        ItemStack item = getItem(player, material);
        setItemDisplayName(item, name);
        setItemLore(item, lore);
        if (customModelData != null) setCustomModelData(item, customModelData);
        return item;
    }

    public static void setGlow(ItemStack item) {
        ItemMeta meta = getItemMeta(item);
        try {
            Enchantment enchant = Registry.ENCHANTMENT.match("UNBREAKING"); // 1.20.5+
            if (enchant == null) enchant = Registry.ENCHANTMENT.match("DURABILITY"); // 1.14-1.20.4
            if (enchant == null) enchant = Iterables.getFirst(Registry.ENCHANTMENT, null);
            if (enchant == null) throw new LinkageError();
            meta.addEnchant(enchant, 1, true);
        } catch (LinkageError e) {
            Enchantment enchant = Enchantment.getByName("DURABILITY"); // 1.8-1.13.2
            if (enchant != null) {
                meta.addEnchant(enchant, 1, true);
            }
        }
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
    }

    public static ItemStack getItem(Player player, String str) {
        if (str.startsWith("!")) str = str.substring(1);
        for (Map.Entry<String, ItemProvider> entry : itemProviders.entrySet()) {
            if (str.startsWith(entry.getKey())) {
                String argument = str.substring(entry.getKey().length());
                return entry.getValue().get(player, argument);
            }
        }
        Integer customModelData = null;
        String material = str;
        Integer dataValue = null;
        if (str.contains("#")) {
            String customModel = str.substring(str.indexOf("#") + 1);
            customModelData = Util.parseInt(customModel).orElseThrow(
                    () -> new IllegalStateException("无法解析 " + customModel + " 为整数")
            );
            material = str.replace("#" + customModel, "");
        }
        if (material.contains(":")) {
            String data = material.substring(str.indexOf(":"));
            dataValue = Util.parseInt(data.substring(1)).orElse(null);
            material = material.replace(data, "");
        }
        ItemStack item = parseMaterial(material.toUpperCase(), dataValue);
        if (customModelData != null) setCustomModelData(item, customModelData);
        return item;
    }

    private static final String[] materialColors = new String[] {
            "STAINED_GLASS", "STAINED_GLASS_PANE", "WOOL", "BANNER", "CARPET"
    };
    private static final String[] dataValueColors = new String[] {
            "WHITE", "ORANGE", "MAGENTA", "LIGHT_BLUE", "YELLOW", "LIME", "PINK", "GRAY",
            "LIGHT_GRAY", "CYAN", "PURPLE", "BLUE", "BROWN", "GREEN", "RED", "BLACK"
    };
    @SuppressWarnings("ConstantValue")
    public static ItemStack parseMaterial(String str, @Nullable Integer dataValue) {
        Class<Material> m = Material.class;
        // 高版本与低版本同名的物品，或者低版本的物品
        Material material = valueOr(m, str, null);
        if (dataValue != null) {
            if (material == null) {
                return new ItemStack(Material.PAPER);
            }
            return legacy(material, dataValue);
        }
        if (material != null) {
            return new ItemStack(material);
        }
        // 带颜色的物品
        if (str.endsWith("_CONCRETE") || str.endsWith("_TERRACOTTA")) {
            material = valueOr(m, "STAINED_HARDENED_CLAY", null);
        } else if (str.contains("BANNER") && !str.contains("PATTERN")) {
            material = valueOr(m, "STAINED_BANNER", null);
        } else if (str.endsWith("DYE")) {
            material = valueOr(m, "INK_SACK", null);
        } else for (String mc : materialColors) {
            if (str.endsWith(mc)) {
                material = valueOr(m, mc, null);
                break;
            }
        }
        if (material != null) {
            Integer data = null;
            boolean reverse = str.endsWith("DYE");
            for (int i = 0; i < dataValueColors.length; i++) {
                if (str.startsWith(dataValueColors[i])) {
                    data = reverse ? (15 - i) : i;
                    break;
                }
            }
            return legacy(material, data);
        }
        // 头颅
        if (material == null && (str.equals("SKELETON_SKULL") || str.equals("SKELETON_WALL_SKULL"))) {
            material = valueOr(m, "SKULL_ITEM", null);
            if (material != null) return legacy(material, 0);
        }
        if (material == null && (str.equals("WITHER_SKELETON_SKULL") || str.equals("WITHER_SKELETON_WALL_SKULL"))) {
            material = valueOr(m, "SKULL_ITEM", null);
            if (material != null) return legacy(material, 1);
        }
        if (material == null && (str.equals("ZOMBIE_HEAD") || str.equals("ZOMBIE_WALL_HEAD"))) {
            material = valueOr(m, "SKULL_ITEM", null);
            if (material != null) return legacy(material, 2);
        }
        if (material == null && (str.equals("PLAYER_HEAD") || str.equals("PLAYER_WALL_HEAD"))) {
            material = valueOr(m, "SKULL_ITEM", null);
            if (material != null) return legacy(material, 3);
        }
        if (material == null && (str.equals("CREEPER_HEAD") || str.equals("CREEPER_WALL_HEAD"))) {
            material = valueOr(m, "SKULL_ITEM", null);
            if (material != null) return legacy(material, 4);
        }
        // 其它杂项物品
        if (material == null && str.equals("CLOCK")) material = valueOr(m, "WATCH", null);
        if (material == null && str.contains("BED")) material = valueOr(m, "BED", null);
        if (material == null && str.equals("CRAFT_TABLE")) material = valueOr(m, "WORKBENCH", null);
        if (material == null && str.contains("_DOOR") && !str.contains("IRON")) material = valueOr(m, "WOODEN_DOOR", null);
        if (material == null && str.startsWith("WOODEN_")) material = valueOr(m, str.replace("WOODEN_", "WOOD_"), null);
        if (material == null && str.equals("IRON_BARS")) material = valueOr(m, "IRON_FENCE", null);
        if (material == null && str.equals("BUNDLE")) material = valueOr(m, "FEATHER", null);
        if (material == null && str.equals("ENDER_EYE")) material = valueOr(m, "EYE_OF_ENDER", null);
        if (material == null && str.equals("COMMAND_BLOCK")) material = valueOr(m, "COMMAND", null);
        if (material == null && str.equals("COMMAND_BLOCK_MINECART")) material = valueOr(m, "COMMAND_MINECART", null);
        if (material == null && str.equals("CHAIN_COMMAND_BLOCK")) material = valueOr(m, "COMMAND_CHAIN", null);
        if (material == null && str.equals("REPEATING_COMMAND_BLOCK")) material = valueOr(m, "COMMAND_REPEATING", null);
        // TODO: 支持更多新旧版本的物品转换
        return new ItemStack(material != null ? material : Material.PAPER);
    }

    public static ItemStack legacy(Material material, @Nullable Integer data) {
        if (data != null) {
            return new ItemStack(material, 1, data.shortValue());
        } else {
            return new ItemStack(material);
        }
    }

    @Contract("null -> true")
    public static boolean isEmpty(ItemStack item) {
        return item == null || item.getType().equals(Material.AIR);
    }

    @NotNull
    public static ItemMeta getItemMeta(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return meta == null ? getItemMeta(item.getType()) : meta;
    }

    @NotNull
    public static ItemMeta getItemMeta(Material material) {
        return Objects.requireNonNull(Bukkit.getItemFactory().getItemMeta(material));
    }
}
