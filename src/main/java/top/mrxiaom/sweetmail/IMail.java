package top.mrxiaom.sweetmail;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.attachments.IAttachment;
import top.mrxiaom.sweetmail.players.IPlayerList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unused"})
public abstract class IMail {
    public static final String SERVER_SENDER = "#Server#";
    protected static IMail instance;
    @NotNull
    public static IMail api() {
        if (instance == null) {
            throw new IllegalStateException("插件未加载");
        }
        return instance;
    }

    protected abstract Status send(MailDraft draft);

    protected abstract String send(MailDraft draft, long timestamp);

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

    public enum Status {
        SUCCESS(true),
        EMPTY_RECEIVER(false)

        ;
        private final boolean isOK;
        Status(boolean isOK) {
            this.isOK = isOK;
        }
        public boolean ok() {
            return isOK;
        }
    }

    public class MailDraft {
        private final String sender;
        private String senderDisplay;
        private String icon = "PAPER";
        private final List<String> receivers = new ArrayList<>();
        private IPlayerList extensiveReceivers;
        private String title = "";
        private final List<String> content = new ArrayList<>();
        private final List<IAttachment> attachments = new ArrayList<>();
        private boolean enablePlaceholders = false;
        private long outdateTime = 0L;
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
         * 获取邮件正文是否要使用 PAPI 变量
         */
        public boolean isEnablePlaceholders() {
            return enablePlaceholders;
        }

        /**
         * 获取邮件附件到期时间 (毫秒时间戳)
         */
        public long getOutdateTime() {
            return outdateTime;
        }

        /**
         * 设置发送者显示名称，建议仅在发送系统邮件时设置
         */
        public MailDraft setSenderDisplay(String senderDisplay) {
            this.senderDisplay = senderDisplay;
            return this;
        }

        /**
         * 设置邮件接收者 ID
         */
        public MailDraft setReceiver(String receiver) {
            this.receivers.clear();
            this.receivers.add(receiver);
            return this;
        }

        /**
         * 设置邮件接收者
         * @see IMail.MailDraft#setReceiver(OfflinePlayer)
         */
        public MailDraft setReceiverFromPlayer(OfflinePlayer receiver) {
            return setReceiver(receiver);
        }

        /**
         * 设置邮件接收者
         * @see IMail.MailDraft#setReceiver(String)
         */
        public MailDraft setReceiver(OfflinePlayer receiver) {
            String s = SweetMail.getInstance().getPlayerKey(receiver);
            if (s != null) {
                setReceiver(s);
            }
            return this;
        }

        /**
         * 设置邮件接收者列表
         * <p>
         * 定时发送不支持该选项输入多个接收者，如有需要多个接收者，详见使用 IPlayerList 参数的选项
         * @see MailDraft#setReceivers(IPlayerList)
         */
        public MailDraft setReceivers(List<String> receivers) {
            this.receivers.clear();
            this.receivers.addAll(receivers);
            if (receivers.size() > 1) {
                // 兼容旧功能
                if ("#advance#".equalsIgnoreCase(receivers.get(0))) {
                    String str = receivers.get(1);
                    IPlayerList inst = SweetMail.getInstance().parsePlayerList(str);
                    if (inst != null) {
                        this.extensiveReceivers = inst;
                    } else {
                        this.receivers.clear();
                    }
                }
            }
            return this;
        }

        /**
         * 按泛收件人方式发送邮件
         * @param receivers 泛收件人计算逻辑
         * @see top.mrxiaom.sweetmail.players.builtin.PlayerListRaw
         */
        public MailDraft setReceivers(IPlayerList receivers) {
            this.receivers.clear();
            this.extensiveReceivers = receivers;
            return this;
        }

        /**
         * 获取泛收件人计算逻辑
         */
        @Nullable
        public IPlayerList getExtensiveReceivers() {
            return extensiveReceivers;
        }

        /**
         * 设置邮件接收者列表
         * @see IMail.MailDraft#setReceivers(List)
         */
        public MailDraft setReceiversFromPlayers(List<OfflinePlayer> receivers) {
            List<String> list = new ArrayList<>();
            for (OfflinePlayer p : receivers) {
                String s = SweetMail.getInstance().getPlayerKey(p);
                if (s != null) {
                    list.add(s);
                }
            }
            return setReceivers(list);
        }

        /**
         * 设置图标
         * @see MailDraft#setIcon(String)
         * @param material 图片
         */
        public MailDraft setIcon(Material material) {
            return setIcon(material.name());
        }

        /**
         * 设置图标，格式如下
         * <ul>
         *     <li><code>物品ID#CustomModelData</code> - 原版物品，不输入 <code>#</code> 则不添加 CMD</li>
         *     <li><code>itemsadder-物品ID</code> - ItemsAdder物品</li>
         *     <li><code>mythic-物品ID</code> - MythicMobs物品</li>
         *     <li><code>head-base64-数据值</code> - Base64格式头颅</li>
         *     <li><code>craftengine-命名空间:物品ID</code> - CraftEngine物品</li>
         *     <li><code>neigeitems-物品ID;数据值</code> - NeigeItems物品，可不添加数据值</li>
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
            this.content.clear();
            this.content.addAll(content);
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
            this.attachments.clear();
            this.attachments.addAll(attachments);
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
         * @see MailDraft#enablePlaceholders(boolean)
         */
        public MailDraft enablePlaceholders() {
            return enablePlaceholders(true);
        }

        /**
         * 设置邮件正文是否要使用 PAPI 变量
         */
        public MailDraft enablePlaceholders(boolean enablePlaceholders) {
            this.enablePlaceholders = enablePlaceholders;
            return this;
        }

        /**
         * 设置邮件附件到期时间 (毫秒时间戳)
         */
        public MailDraft setOutdate(long outdateTime) {
            this.outdateTime = outdateTime;
            return this;
        }

        /**
         * @see MailDraft#enablePlaceholders(boolean)
         */
        @Deprecated
        public void setEnablePlaceholders(boolean enablePlaceholders) {
            this.enablePlaceholders = enablePlaceholders;
        }

        /**
         * @see MailDraft#setOutdate(long)
         */
        @Deprecated
        public void setOutdateTime(long outdateTime) {
            this.outdateTime = outdateTime;
        }

        /**
         * 发送邮件
         * @return 邮件发送状态
         */
        public Status send() {
            return IMail.this.send(this);
        }

        /**
         * 将邮件加入定时发送队列
         * @param timestamp 定时发送时间 (毫秒时间戳)
         * @return 定时发送序列ID
         */
        public String send(long timestamp) {
            return IMail.this.send(this, timestamp);
        }
    }
}
