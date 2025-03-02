package top.mrxiaom.sweetmail.utils.inventory;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import static top.mrxiaom.sweetmail.utils.MiniMessageConvert.miniMessageToLegacy;

public class BukkitInventoryFactory implements InventoryFactory {
    @Override
    @SuppressWarnings({"deprecation"})
    public Inventory create(InventoryHolder owner, int size, String title) {
        String parsed = miniMessageToLegacy(title);
        return Bukkit.createInventory(owner, size, parsed);
    }
}
