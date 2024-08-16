package top.mrxiaom.sweetmail.utils;

import com.google.common.collect.Lists;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBTList;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.utils.comp.IA;
import top.mrxiaom.sweetmail.utils.comp.Mythic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static top.mrxiaom.sweetmail.utils.Util.miniMessage;

@SuppressWarnings({"deprecation", "unused"})
public class ItemStackUtil {
    private static boolean supportTranslationKey;
    protected static void init() {
        supportTranslationKey = Util.isPresent("org.bukkit.Translatable");
    }

    public static String miniTranslate(ItemStack item) {
        if (supportTranslationKey) {
            return "<translate:" + item.getTranslationKey() + ">";
        }
        return item.getType().name(); // TODO: 在不支持 Translatable 的服务端获取物品翻译键
    }

    public static boolean hasCustomModelData(ItemStack item) {
        if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_14_R1)) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasCustomModelData();
    }

    public static Integer getCustomModelData(ItemStack item) {
        if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_14_R1)) return null;
        ItemMeta meta = item.getItemMeta();
        return meta == null ? null : meta.getCustomModelData();
    }

    public static void setCustomModelData(ItemStack item, Integer customModelData) {
        if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_14_R1)) return;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(customModelData);
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
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            try (BukkitObjectOutputStream out = new BukkitObjectOutputStream(output)) {
                out.writeObject(item);
                return Base64Coder.encodeLines(output.toByteArray());
            }
        } catch (Throwable t) {
            SweetMail.warn(t);
            return "";
        }
    }

    public static ItemStack itemStackFromBase64(String s) {
        if (s.trim().isEmpty()) return null;
        try (ByteArrayInputStream in = new ByteArrayInputStream(Base64Coder.decodeLines(s))) {
            try (BukkitObjectInputStream out = new BukkitObjectInputStream(in)) {
                return (ItemStack) out.readObject();
            }
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
        if (item == null) return;
        Component displayName = miniMessage(name);
        String json = GsonComponentSerializer.gson().serialize(displayName);
        setItemDisplayNameRaw(item, json);
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
        if (item == null) return;
        List<String> json = new ArrayList<>();
        for (String s : lore) {
            Component line = miniMessage(s);
            json.add(GsonComponentSerializer.gson().serialize(line));
        }
        setItemLoreRaw(item, json);
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

    public static ItemStack buildItem(Material material, String name, List<String> lore) {
        return buildItem(material, null, name, lore);
    }
    public static ItemStack buildItem(Material material, Integer customModelData, String name, List<String> lore) {
        if (material.equals(Material.AIR)) return new ItemStack(material);
        ItemStack item = new ItemStack(material, 1);
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
            return IA.get(str.substring(11)).orElseThrow(
                    () -> new IllegalStateException("找不到 IA 物品 " + str.substring(11))
            );
        } else if (str.startsWith("mythic-")) {
            return Mythic.getItem(str.substring(7)).orElseThrow(
                    () -> new IllegalStateException("找不到 Mythic 物品 " + str.substring(7))
            );
        } else {
            Integer customModelData = null;
            String material = str;
            if (str.contains("#")) {
                String customModel = str.substring(str.indexOf("#") + 1);
                customModelData = Util.parseInt(customModel).orElseThrow(
                        () -> new IllegalStateException("无法解析 " + customModel + " 为整数")
                );
                material = str.replace("#" + customModelData, "");
            }
            Material m = Util.valueOr(Material.class, material, null);
            if (m == null) throw new IllegalStateException("找不到物品 " + str);
            ItemStack item = new ItemStack(m);
            if (customModelData != null) setCustomModelData(item, customModelData);
            return item;
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
