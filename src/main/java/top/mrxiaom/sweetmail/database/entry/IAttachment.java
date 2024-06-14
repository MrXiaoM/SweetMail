package top.mrxiaom.sweetmail.database.entry;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
}
