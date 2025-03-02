package top.mrxiaom.sweetmail.utils.inventory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import static top.mrxiaom.sweetmail.utils.MiniMessageConvert.legacyToMiniMessage;
import static top.mrxiaom.sweetmail.utils.MiniMessageConvert.miniMessageToLegacy;

public class PaperInventoryFactory implements InventoryFactory {
    private final MiniMessage miniMessage;
    public PaperInventoryFactory() {
        miniMessage = MiniMessage.builder()
                .postProcessor(it -> it.decoration(TextDecoration.ITALIC, false))
                .build();
    }

    public Component miniMessage(String text) {
        if (text == null) {
            return Component.empty();
        }
        return miniMessage.deserialize(legacyToMiniMessage(text));
    }

    @Override
    @SuppressWarnings({"deprecation"})
    public Inventory create(InventoryHolder owner, int size, String title) {
        try {
            Component parsed = miniMessage(title);
            return Bukkit.createInventory(owner, size, parsed);
        } catch (LinkageError e) { // 1.16 以下的旧版本 Paper 服务端不支持这个接口
            String parsed = miniMessageToLegacy(title);
            return Bukkit.createInventory(owner, size, parsed);
        }
    }
}
