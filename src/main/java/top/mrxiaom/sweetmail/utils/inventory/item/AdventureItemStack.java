package top.mrxiaom.sweetmail.utils.inventory.item;

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
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static top.mrxiaom.sweetmail.utils.ItemStackUtil.isEmpty;

public class AdventureItemStack implements ItemStackAPI {
    /**
     * 版本是否支持物品组件
     * @since Minecraft 1.20.5
     */
    private boolean itemNbtUseComponentsFormat;
    /**
     * 物品中的文字（物品名称、Lore）是否使用 JSON字符串 格式的文本组件（Component）
     */
    private boolean textUseComponent;
    /**
     * 物品中的文字（物品名称、Lore）是否使用 NBT Compound 储存文本组件
     * @since Minecraft 1.21.5
     */
    private boolean componentUseNBT;
    public AdventureItemStack() {
        doItemTest();
    }

    private void doItemTest() {
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

    public ComponentSerializer<Component, ?, String> serializer() {
        if (textUseComponent) {
            return GsonComponentSerializer.gson();
        } else {
            return LegacyComponentSerializer.legacySection();
        }
    }

    @Override
    public boolean isTextUseComponent() {
        return textUseComponent;
    }

    @Override
    public Component getItemDisplayName(ItemStack item) {
        String nameAsJson = getItemDisplayNameAsJson(item);
        if (nameAsJson == null) return null;
        return serializer().deserialize(nameAsJson);
    }

    @Override
    public void setItemDisplayName(ItemStack item, Component name) {
        String json = serializer().serialize(name);
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

    @Override
    public List<Component> getItemLore(ItemStack item) {
        List<String> loreAsJson = getItemLoreAsJson(item);
        if (loreAsJson == null) return new ArrayList<>();
        List<Component> lore = new ArrayList<>();
        for (String line : loreAsJson) {
            Component component = serializer().deserialize(line);
            lore.add(component);
        }
        return lore;
    }

    @Override
    public void setItemLore(ItemStack item, List<Component> lore) {
        List<String> json = new ArrayList<>();
        for (Component c : lore) {
            json.add(serializer().serialize(c));
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

    @Nullable
    public List<String> getItemLoreAsJson(ItemStack item) {
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

    public void setItemDisplayNameRaw(ItemStack item, String json) {
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

    @Nullable
    public String getItemDisplayNameAsJson(ItemStack item) {
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

    public void setItemLoreRaw(ItemStack item, List<String> json) {
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
}
