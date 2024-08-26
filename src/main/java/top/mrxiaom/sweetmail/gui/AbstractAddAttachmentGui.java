package top.mrxiaom.sweetmail.gui;

import org.bukkit.entity.Player;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.attachments.IAttachment;
import top.mrxiaom.sweetmail.config.gui.MenuDraftConfig;

public abstract class AbstractAddAttachmentGui extends AbstractDraftGui {
    public AbstractAddAttachmentGui(Player player) {
        super(SweetMail.getInstance(), player);
    }

    public void backToDraftGui() {
        MenuDraftConfig.inst().new Gui(plugin, player).open();
    }

    public void backToSelectTypeGui() {
        // TODO: 返回附件类型选择菜单
    }

    /**
     * 添加附件
     * @param attachment 附件，请自行检查 isLegal 是否为 true，本方法不会替你检查
     */
    public void addAttachment(IAttachment attachment) {
        draft.attachments.add(attachment);
    }
}
