package top.mrxiaom.sweetmail.database.entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.utils.ColorHelper;
import top.mrxiaom.sweetmail.utils.ItemStackUtil;
import top.mrxiaom.sweetmail.utils.comp.PAPI;

import java.util.List;

public class AttachmentCommand implements IAttachment {
    String item;
    String display;
    String command;

    public AttachmentCommand(String item, String display, String command) {
        this.item = item;
        this.display = display;
        this.command = command;
    }

    @Override
    public void use(Player player) {
        String cmd = PAPI.setPlaceholders(player, command);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ColorHelper.parseColor(cmd));
    }

    @Override
    public ItemStack generateDraftIcon(Player target) {
        ItemStack item = ItemStackUtil.getItem(this.item);
        ItemStackUtil.setItemDisplayName(item, display);
        if (!Text.loreRemove.isEmpty()) {
            List<String> lore = ItemStackUtil.getItemLore(item);
            lore.addAll(Text.loreRemove);
            ItemStackUtil.setItemLore(item, lore);
        }
        return item;
    }

    @Override
    public ItemStack generateIcon(Player target) {
        ItemStack item = ItemStackUtil.getItem(this.item);
        ItemStackUtil.setItemDisplayName(item, display);
        return item;
    }

    @Override
    public String serialize() {
        return "command:" + item + "," + "display" + "," + command;
    }

    public static IAttachment deserialize(String s) {
        if (s.startsWith("command:")) {
            String[] split = s.substring(8).split(",", 3);
            if (split.length == 3) {
                String item = split[0];
                String display = split[1];
                String command = split[2];
                return new AttachmentCommand(item, display, command);
            }
        }
        return null;
    }
}
