package top.mrxiaom.sweetmail.attachments;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;
import top.mrxiaom.sweetmail.gui.IGui;
import top.mrxiaom.sweetmail.utils.Util;

import java.util.*;
import java.util.function.Function;

import static top.mrxiaom.sweetmail.commands.CommandMain.PERM_ADMIN;

public interface IAttachment {
    void use(Player player);
    ItemStack generateDraftIcon(Player target);
    ItemStack generateIcon(Player target);
    String serialize();
    boolean isLegal();
    static IAttachment deserialize(String s) {
        for (Internal.AttachmentInfo<?> info : Internal.attachments) {
            IAttachment apply = info.deserializer.apply(s);
            if (apply != null) {
                return apply;
            }
        }
        return null;
    }
    static <T extends IAttachment> void registerAttachment(Class<T> clazz, String permission, Function<Player, ItemStack> icon, Function<Player, IGui> addGui, Function<String, T> deserializer) {
        Internal.AttachmentInfo<T> info = new Internal.AttachmentInfo<>(clazz, permission, icon, addGui, deserializer);
        Internal.attachments.add(info);
    }
    static <T extends IAttachment> boolean unregisterAttachment(Class<T> clazz) {
        return Internal.attachments.removeIf(it -> it.clazz.getName().equals(clazz.getName()));
    }
    static <T extends IAttachment> boolean isRegistered(Class<T> clazz) {
        for (Internal.AttachmentInfo<?> attachment : getAttachments()) {
            if (attachment.clazz.getName().equals(clazz.getName())) return true;
        }
        return false;
    }
    static Collection<Internal.AttachmentInfo<?>> getAttachments() {
        return Collections.unmodifiableCollection(Internal.attachments);
    }
    class Internal extends AbstractPluginHolder {
        public static class AttachmentInfo<T extends IAttachment> {
            public @NotNull final Class<T> clazz;
            public @NotNull final String permission;
            public @NotNull final Function<Player, ItemStack> icon;
            public @NotNull final Function<Player, IGui> addGui;
            public @NotNull final Function<String, T> deserializer;

            private AttachmentInfo(
                    @NotNull Class<T> clazz,
                    @NotNull String permission,
                    @NotNull Function<Player, ItemStack> icon,
                    @NotNull Function<Player, IGui> addGui,
                    @NotNull Function<String, T> deserializer
            ) {
                this.clazz = clazz;
                this.permission = permission;
                this.icon = icon;
                this.addGui = addGui;
                this.deserializer = deserializer;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof AttachmentInfo)) return false;
                AttachmentInfo<?> that = (AttachmentInfo<?>) o;
                return Objects.equals(clazz, that.clazz);
            }

            @Override
            public int hashCode() {
                return Objects.hashCode(clazz);
            }
        }
        protected static Set<AttachmentInfo<?>> attachments = new HashSet<>();
        protected static List<String> loreRemove;
        protected static List<String> loreRemoveAdmin;
        protected static String moneyIcon;
        protected static String moneyName;
        protected static List<String> moneyLore;
        protected static String itemDisplay;
        protected static String itemDisplayWithAmount;
        protected static List<Material> itemBanMaterials;
        protected static List<String> itemBanName;
        protected static List<String> itemBanLore;
        protected static String messageUseIllegalDeny;

        protected static String addMoneyPromptTips;
        protected static String addMoneyPromptCancel;
        protected static String addMoneyFail;
        protected static String addMoneyNotEnough;
        protected static String addCommandPromptTips;
        protected static String addCommandPromptCancel;
        protected static String addCommandFail;
        public Internal(SweetMail plugin) {
            super(plugin);
            register();
        }

        public static List<String> getLoreRemove(Permissible target) {
            return target.hasPermission(PERM_ADMIN)
                    ? Internal.loreRemoveAdmin
                    : Internal.loreRemove;
        }

        @Override
        public void reloadConfig(MemoryConfiguration config) {
            loreRemove = config.getStringList("messages.draft.attachments.remove-lore");
            loreRemoveAdmin = config.getStringList("messages.draft.attachments.remove-lore-admin");
            moneyIcon = config.getString("messages.draft.attachments.money.icon", "GOLD_NUGGET");
            moneyName = config.getString("messages.draft.attachments.money.name", "");
            moneyLore = config.getStringList("messages.draft.attachments.money.lore");
            itemDisplay = config.getString("messages.draft.attachments.item.display", "");
            itemDisplayWithAmount = config.getString("messages.draft.attachments.item.display-with-amount", "");
            itemBanMaterials = new ArrayList<>();
            for (String s : config.getStringList("attachments.item.blacklist.materials")) {
                Material material = Util.valueOr(Material.class, s, null);
                if (material != null) {
                    itemBanMaterials.add(material);
                }
            }
            itemBanName = config.getStringList("attachments.item.blacklist.display_name");
            itemBanLore = config.getStringList("attachments.item.blacklist.lore");
            messageUseIllegalDeny = config.getString("messages.draft.attachments.use-illegal-deny", "");

            addMoneyPromptTips = config.getString("messages.draft.attachments.money.add.prompt-tips", "&7[&e&l邮件&7] &b请在聊天栏发送&e“附件金币数量”&b的值 &7(输入 &ccancel &7取消添加附件)");
            addMoneyPromptCancel = config.getString("messages.draft.attachments.money.add.prompt-cancel", "cancel");
            addMoneyFail = config.getString("messages.draft.attachments.money.add.fail", "&7[&e&l邮件&7] &e请输入大于0的实数");
            addMoneyNotEnough = config.getString("messages.draft.attachments.money.add.not-enough", "&7[&e&l邮件&7] &e你没有足够的金币");
            addCommandPromptTips = config.getString("messages.draft.attachments.command.prompt-tips", "&7[&e&l邮件&7] &b请在聊天栏发送&e“控制台命令附件”&b的值 &7(格式 &f图标,显示名称,执行命令&7，如&f PAPER,10金币,money give %player_name% 10 &7。输入 &ccancel &7取消添加附件)");
            addCommandPromptCancel = config.getString("messages.draft.attachments.command.prompt-cancel", "cancel");
            addCommandFail = config.getString("messages.draft.attachments.command.add.fail", "&7[&e&l邮件&7] &e格式不正确，应为 &f图标,显示名称,执行命令&e，如&f PAPER,10金币,money give %player_name% 10");
        }

        public static void useIllegalDeny(CommandSender sender) {
            t(sender, messageUseIllegalDeny);
        }

        public static Internal inst() {
            return get(Internal.class).orElseThrow(IllegalStateException::new);
        }
    }
}
