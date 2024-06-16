package top.mrxiaom.sweetmail.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.commands.CommandMain;
import top.mrxiaom.sweetmail.database.entry.IAttachment;
import top.mrxiaom.sweetmail.func.DraftManager;
import top.mrxiaom.sweetmail.gui.GuiDraft;
import top.mrxiaom.sweetmail.utils.ItemStackUtil;
import top.mrxiaom.sweetmail.utils.Pair;

public class MenuDraftConfig extends AbstractMenuConfig<GuiDraft> {
    Icon iconReceiver;
    String iconReceiverUnset;
    public String iconReceiverPromptTips;
    public String iconReceiverPromptCancel;
    Icon iconIcon;
    public String iconIconTitle;
    public String iconIconTitleCustom;
    Icon iconTitle;
    public String iconTitlePromptTips;
    public String iconTitlePromptCancel;
    Icon iconContent;
    Icon iconAdvanced;
    String iconAdvancedRedirectKey;
    Icon iconReset;
    Icon iconSend;
    Icon iconAttachment;

    public MenuDraftConfig(SweetMail plugin) {
        super(plugin, "menus/draft.yml");
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        super.reloadConfig(cfg);

    }

    @Override
    protected void clearMainIcons() {
        iconReceiver = iconIcon = iconTitle = iconContent = iconAdvanced = iconReset = iconSend = iconAttachment = null;
        iconReceiverUnset = iconAdvancedRedirectKey
                = iconReceiverPromptTips = iconReceiverPromptCancel
                = iconTitlePromptTips = iconTitlePromptCancel
                = iconIconTitle = iconIconTitleCustom = null;
    }

    @Override
    protected void loadMainIcon(ConfigurationSection section, String key, Icon loadedIcon) {
        switch (key) {
            case "接": {
                iconReceiver = loadedIcon;
                iconReceiverUnset = section.getString(key + ".unset", "&7未设置");
                iconReceiverPromptTips = section.getString(key + ".prompt-tips", "&7[&e&l邮件&7] &b请在聊天栏发送&e“邮件接收者”&b的值 &7(输入 &ccancel &7取消设置)");
                iconReceiverPromptCancel = section.getString(key + ".prompt-cancel", "cancel");
                break;
            }
            case "图": {
                iconIcon = loadedIcon;
                iconIconTitle = section.getString(key + ".title", "选择图标");
                iconIconTitleCustom = section.getString(key + ".title-custom", "选择图标 (可在物品栏选择)");
                break;
            }
            case "题": {
                iconTitle = loadedIcon;
                iconTitlePromptTips = section.getString(key + ".prompt-tips", "&7[&e&l邮件&7] &b请在聊天栏发送&e“邮件标题”&b的值 &7(输入 &ccancel &7取消设置)");
                iconTitlePromptCancel = section.getString(key + ".prompt-cancel", "cancel");
                break;
            }
            case "文": {
                iconContent = loadedIcon;
                break;
            }
            case "高": {
                iconAdvanced = loadedIcon;
                iconAdvancedRedirectKey = section.getString(key + ".redirect", "黑");
                break;
            }
            case "重": {
                iconReset = loadedIcon;
                break;
            }
            case "发": {
                iconSend = loadedIcon;
                break;
            }
            case "附": {
                iconAttachment = loadedIcon;
                break;
            }
        }
    }

    @Override
    protected ItemStack tryApplyMainIcon(GuiDraft gui, String key, Player target, int iconIndex) {
        DraftManager manager = DraftManager.inst();
        DraftManager.Draft draft = manager.getDraft(target);
        switch (key) {
            case "接": {
                String receiver = draft.receiver.isEmpty() ? iconReceiverUnset : draft.receiver;
                return iconReceiver.generateIcon(
                        target,
                        Pair.of("%receiver%", receiver)
                );
            }
            case "图": {
                ItemStack item = ItemStackUtil.getItem(manager.getMailIcon(draft.iconKey));
                return iconIcon.generateIcon(
                        target, item,
                        Pair.of("%icon%", draft.iconKey)
                );
            }
            case "题": {
                return iconTitle.generateIcon(
                        target,
                        Pair.of("%title%", draft.title)
                );
            }
            case "文": {
                return iconContent.generateIcon(
                        target,
                        Pair.of("%content_size%", String.join("", draft.content).length())
                );
            }
            case "高": {
                if (target.hasPermission(CommandMain.PERM_ADMIN)) {
                    return iconAdvanced.generateIcon(target);
                } else {
                    Icon icon = otherIcon.get(iconAdvancedRedirectKey);
                    return icon == null ? null : icon.generateIcon(target);
                }
            }
            case "重": {
                return iconReset.generateIcon(target);
            }
            case "发": {
                return iconSend.generateIcon(target);
            }
            case "附": {
                if (iconIndex < draft.attachments.size()) {
                    IAttachment attachment = draft.attachments.get(iconIndex);
                    return attachment.generateDraftIcon(target);
                } else {
                    return iconAttachment.generateIcon(target);
                }
            }
        }
        return null;
    }

    public static MenuDraftConfig inst() {
        return get(MenuDraftConfig.class).orElseThrow(IllegalStateException::new);
    }
}
