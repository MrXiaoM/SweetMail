package top.mrxiaom.sweetmail.utils.inventory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.regex.Pattern;

import static top.mrxiaom.sweetmail.utils.MiniMessageConvert.legacyToMiniMessage;
import static top.mrxiaom.sweetmail.utils.MiniMessageConvert.miniMessageToLegacy;
import static top.mrxiaom.sweetmail.utils.StringHelper.split;

public class PaperInventoryFactory implements InventoryFactory {
    private final MiniMessage miniMessage;
    private String offsetFont = "mrxiaom:sweetmail";
    private final Pattern offsetPattern = Pattern.compile("<offset:(-?[0-9]+)>");
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
            StringBuilder sb = new StringBuilder();
            split(offsetPattern, title, result -> {
                if (result.isMatched) {
                    int offset = Integer.parseInt(result.result.group(1));
                    String str = Offset.get(offset);
                    sb.append("<font:").append(offsetFont).append(">").append(str).append("</font>");
                } else {
                    sb.append(result.text);
                }
            });
            Component parsed = miniMessage(sb.toString());
            return Bukkit.createInventory(owner, size, parsed);
        } catch (LinkageError e) { // 1.16 以下的旧版本 Paper 服务端不支持这个接口
            String parsed = miniMessageToLegacy(title);
            return Bukkit.createInventory(owner, size, parsed);
        }
    }

    @Override
    public void setOffsetFont(String font) {
        this.offsetFont = font;
    }
}
