package top.mrxiaom.sweetmail.utils;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ItemStackUtil {

    public static String itemStackArrayToBase64(ItemStack[] var1) {
        return itemStackArrayToBase64(var1, false);
    }

    public static String itemStackArrayToBase64(ItemStack[] var1, boolean ignoreException) {
        try {
            ByteArrayOutputStream var2 = new ByteArrayOutputStream();
            BukkitObjectOutputStream var3 = new BukkitObjectOutputStream(var2);
            var3.writeInt(var1.length);

            for (ItemStack var7 : var1) {
                var3.writeObject(var7);
            }

            var3.close();
            return Base64Coder.encodeLines(var2.toByteArray());
        } catch (Throwable t) {
            if (!ignoreException)
                t.printStackTrace();
            return "";
        }
    }

    public static ItemStack[] itemStackArrayFromBase64(String var1) {
        return itemStackArrayFromBase64(var1, false);
    }

    public static ItemStack[] itemStackArrayFromBase64(String var1, boolean ignoreException) {
        if (var1.isEmpty() || var1.trim().equalsIgnoreCase(""))
            return new ItemStack[0];
        try {
            ByteArrayInputStream var2 = new ByteArrayInputStream(Base64Coder.decodeLines(var1));
            BukkitObjectInputStream var3 = new BukkitObjectInputStream(var2);
            ItemStack[] var4 = new ItemStack[var3.readInt()];

            for (int var5 = 0; var5 < var4.length; ++var5) {
                var4[var5] = (ItemStack) var3.readObject();
            }

            var3.close();
            return var4;
        } catch (Throwable t) {
            if (!ignoreException)
                t.printStackTrace();
            return new ItemStack[0];
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
        if (item == null)
            return;
        ItemMeta im = item.getItemMeta() == null ? getItemMeta(item.getType()) : item.getItemMeta();
        if (im == null)
            return;
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        item.setItemMeta(im);
    }

    public static void setItemLore(ItemStack item, String... lore) {
        setItemLore(item, Lists.newArrayList(lore));
    }

    public static void setItemLore(ItemStack item, List<String> lore) {
        if (item == null)
            return;
        ItemMeta im = item.getItemMeta() == null ? getItemMeta(item.getType()) : item.getItemMeta();
        if (im == null)
            return;
        List<String> newLore = new ArrayList<>();
        lore.forEach(s -> {
            if (s != null) newLore.add(ColorHelper.parseColor(s));
        });
        im.setLore(newLore);
        item.setItemMeta(im);
    }

    public static ItemStack buildItem(Material material, String name) {
        if (material.isAir()) return new ItemStack(material);
        return buildItem(material, null, name, Lists.newArrayList());
    }

    public static ItemStack buildItem(Material material, String name, String... lore) {
        if (material.isAir()) return new ItemStack(material);
        return buildItem(material, null, name, Lists.newArrayList(lore));
    }
    public static ItemStack buildItem(Material material, Integer customModeData, String name, String... lore) {
        if (material.isAir()) return new ItemStack(material);
        return buildItem(material, customModeData, name, Lists.newArrayList(lore));
    }

    public static ItemStack buildItem(Material material, String name, List<String> lore) {
        return buildItem(material, null, name, lore);
    }
    public static ItemStack buildItem(Material material, Integer customModelData, String name, List<String> lore) {
        if (material.isAir()) return new ItemStack(material);
        ItemStack item = new ItemStack(material, 1);
        ItemMeta im = getItemMeta(material);
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        if (!lore.isEmpty()) {
            List<String> l = new ArrayList<>();
            for (String s : lore) {
                l.add(ChatColor.translateAlternateColorCodes('&', s));
            }
            im.setLore(l);
        }
        if (customModelData != null) {
            im.setCustomModelData(customModelData);
        }
        item.setItemMeta(im);
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
            if (customModelData != null) {
                ItemMeta meta = ItemStackUtil.getItemMeta(item);
                meta.setCustomModelData(customModelData);
                item.setItemMeta(meta);
            }
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
