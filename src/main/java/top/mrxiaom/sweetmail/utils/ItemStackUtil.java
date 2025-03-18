package top.mrxiaom.sweetmail.utils;

import com.google.common.collect.Lists;
import com.meowj.langutils.lang.LanguageHelper;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTType;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.attachments.IAttachment;
import top.mrxiaom.sweetmail.depend.ItemsAdder;
import top.mrxiaom.sweetmail.depend.Mythic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static top.mrxiaom.sweetmail.utils.Util.*;

@SuppressWarnings({"deprecation", "unused"})
public class ItemStackUtil {
    public static final String FLAG = "SWEETMAIL_MENU_ICON";
    private static boolean supportTranslationKey;
    private static boolean supportBundle;
    private static boolean useComponent;
    private static boolean supportLangUtils;
    public static String locale = "zh_CN";
    protected static void init() {
        supportTranslationKey = Util.isPresent("org.bukkit.Translatable");
        supportBundle = Util.isPresent("org.bukkit.inventory.meta.BundleMeta");

        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
            useComponent = true;
        } else {
            ItemStack item = new ItemStack(Material.STONE);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String testDisplayName = "§a§l测试§e§l文本";
                meta.setDisplayName(testDisplayName);
                item.setItemMeta(meta);
                NBT.get(item, nbt -> {
                    ReadableNBT display = nbt.getCompound("display");
                    if (display == null) {
                        useComponent = false;
                        return;
                    }
                    String name = display.getString("Name");
                    useComponent = !name.equals(testDisplayName);
                });
            } else {
                useComponent = false;
            }
        }
        supportLangUtils = isPresent("com.meowj.langutils.lang.LanguageHelper");
    }

    public static ComponentSerializer<Component, ?, String> serializer() {
        if (useComponent) {
            return GsonComponentSerializer.gson();
        } else {
            return LegacyComponentSerializer.legacySection();
        }
    }

    public static String miniTranslate(ItemStack item) {
        if (supportTranslationKey && useComponent) {
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
        return getCustomModelData(item) != null;
    }

    public static Integer getCustomModelData(ItemStack item) {
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
            return NBT.modifyComponents(item, nbt -> {
                String key = "minecraft:custom_model_data";
                return nbt.hasTag(key, NBTType.NBTTagInt) ? nbt.getInteger(key) : null;
            });
        } else {
            return NBT.get(item, nbt -> {
                String key = "CustomModelData";
                return nbt.hasTag(key, NBTType.NBTTagInt) ? nbt.getInteger(key) : null;
            });
        }
    }

    public static void setCustomModelData(ItemStack item, Integer customModelData) {
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
            NBT.modifyComponents(item, nbt -> {
                String key = "minecraft:custom_model_data";
                if (customModelData != null) nbt.setInteger(key, customModelData);
                else nbt.removeKey(key);
            });
        } else {
            NBT.modify(item, nbt -> {
                String key = "CustomModelData";
                if (customModelData != null) nbt.setInteger(key, customModelData);
                else nbt.removeKey(key);
            });
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

    public static String getItemDisplayName(ItemStack item) {
        if ((item == null) || !item.hasItemMeta() || item.getItemMeta() == null)
            return item != null ? item.getType().name() : "";
        return item.getItemMeta().getDisplayName();
    }

    public static List<String> getItemLore(ItemStack item) {
        if ((item == null) || !item.hasItemMeta() || item.getItemMeta() == null
                || (item.getItemMeta().getLore() == null))
            return new ArrayList<>();
        return item.getItemMeta().getLore();
    }

    public static void setItemDisplayName(ItemStack item, String name) {
        if (item == null || item.getType().equals(Material.AIR)) return;
        Component displayName = miniMessage(name);
        String json = serializer().serialize(displayName);
        if (useComponent) {
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
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
            NBT.modifyComponents(item, nbt -> {
                nbt.setString("minecraft:custom_name", json);
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
        if (useComponent) {
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
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
            NBT.modifyComponents(item, nbt -> {
                ReadWriteNBTList<String> list = nbt.getStringList("minecraft:lore");
                if (!list.isEmpty()) list.clear();
                list.addAll(json);
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

    public static ItemStack buildItem(String material, String name, List<String> lore) {
        return buildItem(material, null, name, lore);
    }
    public static ItemStack buildItem(String material, Integer customModelData, String name, List<String> lore) {
        if (material.equalsIgnoreCase("AIR")) return new ItemStack(Material.AIR);
        ItemStack item = getItem(material);
        setItemDisplayName(item, name);
        setItemLore(item, lore);
        if (customModelData != null) setCustomModelData(item, customModelData);
        return item;
    }

    public static void setGlow(ItemStack item) {
        ItemMeta meta = getItemMeta(item);
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
    }

    public static ItemStack getItem(String str) {
        if (str.startsWith("itemsadder-")) {
            return ItemsAdder.get(str.substring(11)).orElseThrow(
                    () -> new IllegalStateException("找不到 IA 物品 " + str.substring(11))
            );
        } else if (str.startsWith("mythic-")) {
            return Mythic.getItem(str.substring(7)).orElseThrow(
                    () -> new IllegalStateException("找不到 Mythic 物品 " + str.substring(7))
            );
        } else {
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
