package top.mrxiaom.sweetmail.attachments;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.sweetmail.Messages;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.depend.PAPI;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;
import top.mrxiaom.sweetmail.gui.IGui;
import top.mrxiaom.sweetmail.utils.ItemStackUtil;
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
    default boolean canGiveBack(Player player) {
        return true;
    }
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
        protected static final Set<AttachmentInfo<?>> attachments = new HashSet<>();
        protected static List<Material> itemBanMaterials;
        protected static List<String> itemBanName;
        protected static List<String> itemBanLore;

        protected static String attachmentItemMaterial;
        protected static String attachmentItemDisplay;
        protected static List<String> attachmentItemLore;
        protected static String attachmentMoneyMaterial;
        protected static String attachmentMoneyDisplay;
        protected static List<String> attachmentMoneyLore;
        protected static String attachmentCommandMaterial;
        protected static String attachmentCommandDisplay;
        protected static List<String> attachmentCommandLore;
        public Internal(SweetMail plugin) {
            super(plugin);
            register();
        }

        public static List<String> getLoreRemove(Player target) {
            List<String> list = (target.hasPermission(PERM_ADMIN)
                    ? Messages.Draft.attachments__remove_lore_admin
                    : Messages.Draft.attachments__remove_lore).list();
            return PAPI.setPlaceholders(target, list);
        }

        @Override
        public void reloadConfig(MemoryConfiguration config) {
            itemBanMaterials = new ArrayList<>();
            for (String s : config.getStringList("attachments.item.blacklist.materials")) {
                Material material = Util.valueOr(Material.class, s, null);
                if (material != null) {
                    itemBanMaterials.add(material);
                }
            }
            itemBanName = config.getStringList("attachments.item.blacklist.display_name");
            itemBanLore = config.getStringList("attachments.item.blacklist.lore");

            attachmentItemMaterial = config.getString("attachments.item.material", "ITEM_FRAME");
            attachmentItemDisplay = config.getString("attachments.item.display", "物品附件");
            attachmentItemLore = config.getStringList("attachments.item.lore");
            attachmentMoneyMaterial = config.getString("attachments.money.material", "GOLD_NUGGET");
            attachmentMoneyDisplay = config.getString("attachments.money.display", "金币附件");
            attachmentMoneyLore = config.getStringList("attachments.money.lore");
            attachmentCommandMaterial = config.getString("attachments.command.material", "COMMAND_BLOCK");
            attachmentCommandDisplay = config.getString("attachments.command.display", "控制台命令附件");
            attachmentCommandLore = config.getStringList("attachments.command.lore");
        }

        protected static ItemStack attachmentItem(Player player) {
            return ItemStackUtil.buildItem(player, attachmentItemMaterial, attachmentItemDisplay, attachmentItemLore);
        }

        protected static ItemStack attachmentMoney(Player player) {
            return ItemStackUtil.buildItem(player, attachmentMoneyMaterial, attachmentMoneyDisplay, attachmentMoneyLore);
        }

        protected static ItemStack attachmentCommand(Player player) {
            return ItemStackUtil.buildItem(player, attachmentCommandMaterial, attachmentCommandDisplay, attachmentCommandLore);
        }

        public static void useIllegalDeny(CommandSender sender) {
            Messages.Draft.attachments__use_illegal_deny.tm(sender);
        }

        public static Internal inst() {
            return instanceOf(Internal.class);
        }
    }
}
