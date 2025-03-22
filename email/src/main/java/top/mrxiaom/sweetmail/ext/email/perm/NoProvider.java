package top.mrxiaom.sweetmail.ext.email.perm;

import org.bukkit.OfflinePlayer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class NoProvider implements IPermissionProvider {
    public static final NoProvider INSTANCE = new NoProvider();
    private NoProvider() {}

    @Override
    public String getName() {
        return "None";
    }

    @Override
    public CompletableFuture<Boolean> has(OfflinePlayer player, String permission, Executor executor) {
        return CompletableFuture.completedFuture(true);
    }
}
