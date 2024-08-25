package top.mrxiaom.sweetmail.attachments;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.utils.ColorHelper;
import top.mrxiaom.sweetmail.utils.ItemStackUtil;
import top.mrxiaom.sweetmail.utils.comp.PAPI;

import java.util.List;

public class AttachmentCommand implements IAttachment {
    private final String item;
    private final String display;
    private final String command;

    private AttachmentCommand(String item, String display, String command) {
        this.item = item;
        this.display = display;
        this.command = command;
    }

    /**
     * 构建一个命令附件
     * @param item 显示图标，格式详见 {@link top.mrxiaom.sweetmail.IMail.MailDraft#setIcon(String)}
     * @param display 在界面中显示的附件名称
     * @param command 领取该附件执行的控制台命令，支持PAPI变量
     */
    public static AttachmentCommand build(String item, String display, String command) {
        return new AttachmentCommand(item, display, command);
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
        if (!Internal.loreRemove.isEmpty()) {
            List<String> lore = ItemStackUtil.getItemLore(item);
            lore.addAll(Internal.loreRemove);
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
    public String toString() {
        return display;
    }

    @Override
    public String serialize() {
        return "command:" + item + "," + display.replace(",", "，") + "," + command;
    }

    @Override
    public boolean isLegal() {
        return true;
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
