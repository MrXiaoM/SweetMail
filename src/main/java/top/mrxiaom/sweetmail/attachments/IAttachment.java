package top.mrxiaom.sweetmail.attachments;

import org.bukkit.Material;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;
import top.mrxiaom.sweetmail.utils.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface IAttachment {
    List<Function<String, IAttachment>> deserializers = new ArrayList<>();
    void use(Player player);
    ItemStack generateDraftIcon(Player target);
    ItemStack generateIcon(Player target);
    String serialize();
    boolean isLegal();
    static IAttachment deserialize(String s) {
        for (Function<String, IAttachment> deserializer : deserializers) {
            IAttachment apply = deserializer.apply(s);
            if (apply != null) {
                return apply;
            }
        }
        return null;
    }
    class Internal extends AbstractPluginHolder {
        protected static List<String> loreRemove;
        protected static String moneyIcon;
        protected static String moneyName;
        protected static List<String> moneyLore;
        protected static String itemDisplay;
        protected static String itemDisplayWithAmount;
        protected static List<Material> itemBanMaterials;
        protected static List<String> itemBanName;
        protected static List<String> itemBanLore;
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
            itemDisplay = config.getString("messages.draft.attachments.item.display");
            itemDisplayWithAmount = config.getString("messages.draft.attachments.item.display-with-amount");
            itemBanMaterials = new ArrayList<>();
            for (String s : config.getStringList("attachments.item.blacklist.materials")) {
                Material material = Util.valueOr(Material.class, s, null);
                if (material != null) {
                    itemBanMaterials.add(material);
                }
            }
            itemBanName = config.getStringList("attachments.item.blacklist.display_name");
            itemBanLore = config.getStringList("attachments.item.blacklist.lore");
        }
    }
}
