package top.mrxiaom.sweetmail.ext.email.perm;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class PermVault implements IPermissionProvider {
    private final Permission vault;
    public PermVault(Permission vault) {
        this.vault = vault;
    }

    @Override
    public String getName() {
        return "Vault{" + vault.getName() + "}";
    }

    @Override
    public CompletableFuture<Boolean> has(OfflinePlayer player, String permission, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            String world = null;
            if (player.isOnline()) {
                Player p = player.getPlayer();
                world = p == null ? null : p.getWorld().getName();
            }
            return vault.playerHas(world, player, permission);
        }, executor);
    }
}
