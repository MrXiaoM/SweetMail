package top.mrxiaom.sweetmail.database.entry;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface IAttachment {
    List<Function<String, IAttachment>> deserializers = new ArrayList<>();
    void use(Player player);
    ItemStack generateDraftIcon(Player target);
    ItemStack generateIcon(Player target);
    String serialize();
    static IAttachment deserialize(String s) {
        for (Function<String, IAttachment> deserializer : deserializers) {
            IAttachment apply = deserializer.apply(s);
            if (apply != null) {
                return apply;
            }
        }
        return null;
    }
    class Text extends AbstractPluginHolder {
        protected static List<String> loreRemove;
        protected static String moneyIcon;
        protected static String moneyName;
        protected static List<String> moneyLore;
        public Text(SweetMail plugin) {
            super(plugin);
            register();
        }

        @Override
        public void reloadConfig(MemoryConfiguration config) {
            loreRemove = config.getStringList("messages.draft.attachments.remove-lore");
            moneyIcon = config.getString("messages.draft.attachments.money.icon", "GOLD_NUGGET");
            moneyName = config.getString("messages.draft.attachments.money.name", "");
            moneyLore = config.getStringList("messages.draft.attachments.money.lore");
        }
    }
}
