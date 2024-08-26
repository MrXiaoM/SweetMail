package top.mrxiaom.sweetmail.attachments;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;
import top.mrxiaom.sweetmail.gui.IGui;
import top.mrxiaom.sweetmail.utils.Util;

import java.util.*;
import java.util.function.Function;

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
    static <T extends IAttachment> void registerAttachment(Class<T> clazz, Function<Player, ItemStack> icon, Function<Player, IGui> addGui, Function<String, T> deserializer) {
        Internal.AttachmentInfo<T> info = new Internal.AttachmentInfo<>(clazz, icon, addGui, deserializer);
        Internal.attachments.add(info);
    }
    static Collection<Internal.AttachmentInfo<?>> getAttachments() {
        return Collections.unmodifiableCollection(Internal.attachments);
    }
    class Internal extends AbstractPluginHolder {
        public static class AttachmentInfo<T extends IAttachment> {
            public @NotNull final Class<T> clazz;
            public @NotNull final Function<Player, ItemStack> icon;
            public @NotNull final Function<Player, IGui> addGui;
            public @NotNull final Function<String, T> deserializer;

            private AttachmentInfo(
                    @NotNull Class<T> clazz,
                    @NotNull Function<Player, ItemStack> icon,
                    @NotNull Function<Player, IGui> addGui,
                    @NotNull Function<String, T> deserializer
            ) {
                this.clazz = clazz;
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
        protected static String moneyIcon;
        protected static String moneyName;
        protected static List<String> moneyLore;
        protected static String itemDisplay;
        protected static String itemDisplayWithAmount;
        protected static List<Material> itemBanMaterials;
        protected static List<String> itemBanName;
        protected static List<String> itemBanLore;
        protected static String messageUseIllegalDeny;
        public Internal(SweetMail plugin) {
            super(plugin);
            register();
        }

        @Override
        public void reloadConfig(MemoryConfiguration config) {
            loreRemove = config.getStringList("messages.draft.attachments.remove-lore");
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
        }

        public void useIllegalDeny(CommandSender sender) {
            t(sender, messageUseIllegalDeny);
        }

        public static Internal inst() {
            return get(Internal.class).orElseThrow(IllegalStateException::new);
        }
    }
}
