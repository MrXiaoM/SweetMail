package top.mrxiaom.sweetmail;

import org.jetbrains.annotations.NotNull;
import top.mrxiaom.sweetmail.database.entry.IAttachment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class IMail {
    public static final String SERVER_SENDER = "#Server#";
    protected static IMail instance;
    @NotNull
    public static IMail api() {
        return instance;
    }

    protected abstract boolean send(MailDraft draft);

    /**
     * 创建邮件草稿，不会覆盖发件人创建的草稿
     * @param sender 发件人
     */
    public MailDraft createMail(String sender) {
        return new MailDraft(sender);
    }

    /**
     * 创建系统邮件草稿
     * @param senderDisplay 系统邮件发件人显示名称
     */
    public MailDraft createSystemMail(String senderDisplay) {
        return new MailDraft(SERVER_SENDER).setSenderDisplay(senderDisplay);
    }

    public class MailDraft {
        private final String sender;
        private String senderDisplay;
        private String icon = "PAPER";
        private List<String> receivers = new ArrayList<>();
        private String title = "";
        private List<String> content = new ArrayList<>();
        private List<IAttachment> attachments = new ArrayList<>();
        private MailDraft(String sender) {
            this.sender = sender;
        }

        /**
         * 获取邮件发送者
         */
        public String getSender() {
            return sender;
        }

        /**
         * 获取邮件发送者显示名称，建议仅在发送系统邮件时设置
         */
        public String getSenderDisplay() {
            return senderDisplay;
        }

        /**
         * 获取邮件图标，字符串格式详见 setIcon
         * @see MailDraft#setIcon(String)
         */
        public String getIcon() {
            return icon;
        }

        /**
         * 获取邮件接收者
         */
        public List<String> getReceivers() {
            return receivers;
        }

        /**
         * 获取邮件标题
         */
        public String getTitle() {
            return title;
        }

        /**
         * 获取邮件内容，每一元素为书与笔中的每一页
         */
        public List<String> getContent() {
            return content;
        }

        /**
         * 获取邮件附件
         */
        public List<IAttachment> getAttachments() {
            return attachments;
        }

        /**
         * 设置发送者显示名称，建议仅在发送系统邮件时设置
         */
        public MailDraft setSenderDisplay(String senderDisplay) {
            this.senderDisplay = senderDisplay;
            return this;
        }

        /**
         * 设置邮件接收者
         */
        public MailDraft setReceiver(String receiver) {
            this.receivers.clear();
            this.receivers.add(receiver);
            return this;
        }

        /**
         * 设置邮件接收者列表
         */
        public MailDraft setReceivers(List<String> receivers) {
            this.receivers = receivers;
            return this;
        }

        /**
         * 设置图标，格式如下<br>
         * <ul>
         *     <li>物品ID#CustomModelData - 原版物品，不输入 # 则不添加 CMD</li>
         *     <li>itemsadder-物品ID - ItemsAdder物品</li>
         *     <li>mythic-物品ID - MythicMobs物品</li>
         * </ul>
         */
        public MailDraft setIcon(String icon) {
            this.icon = icon;
            return this;
        }

        /**
         * 设置邮件标题
         */
        public MailDraft setTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * 设置邮件内容
         * @param content 书与笔中的每页内容
         */
        public MailDraft setContent(List<String> content) {
            this.content = content;
            return this;
        }

        /**
         * @see MailDraft#setContent(List)
         */
        public MailDraft addContent(Collection<String> content) {
            this.content.addAll(content);
            return this;
        }

        /**
         * @see MailDraft#setContent(List)
         */
        public MailDraft addContent(String... content) {
            Collections.addAll(this.content, content);
            return this;
        }

        /**
         * 设置邮件附件
         */
        public MailDraft setAttachments(List<IAttachment> attachments) {
            this.attachments = attachments;
            return this;
        }

        /**
         * @see MailDraft#setAttachments(List)
         */
        public MailDraft addAttachments(Collection<IAttachment> attachments) {
            this.attachments.addAll(attachments);
            return this;
        }

        /**
         * @see MailDraft#setAttachments(List)
         */
        public MailDraft addAttachments(IAttachment... attachments) {
            Collections.addAll(this.attachments, attachments);
            return this;
        }

        /**
         * 发送邮件
         * @return 返回 true 时发送成功，返回 false 时发送失败。发送失败通常由“未设置接收者”引起
         */
        public boolean send() {
            return IMail.this.send(this);
        }
    }
}
