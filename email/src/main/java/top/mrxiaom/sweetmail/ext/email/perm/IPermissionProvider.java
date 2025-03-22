package top.mrxiaom.sweetmail.ext.email.perm;

import org.bukkit.OfflinePlayer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface IPermissionProvider {
    String getName();
    CompletableFuture<Boolean> has(OfflinePlayer player, String permission, Executor executor);
}
