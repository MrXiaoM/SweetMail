package top.mrxiaom.sweetmail.ext.email.perm;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedDataManager;
import net.luckperms.api.context.ContextManager;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.util.Tristate;
import org.bukkit.OfflinePlayer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class PermLuckPerms implements IPermissionProvider {
    private final LuckPerms luckPerms;
    public PermLuckPerms(LuckPerms luckPerms) {
        this.luckPerms = luckPerms;
    }

    @Override
    public String getName() {
        return "LuckPerms";
    }

    @Override
    public CompletableFuture<Boolean> has(OfflinePlayer player, String permission, Executor executor) {
        UserManager manager = luckPerms.getUserManager();
        return manager.loadUser(player.getUniqueId()).thenApply(user -> {
            ContextManager ctx = luckPerms.getContextManager();
            QueryOptions queryOptions = ctx.getQueryOptions(user).orElseGet(ctx::getStaticQueryOptions);
            CachedDataManager cachedData = user.getCachedData();
            Tristate tristate = cachedData.getPermissionData(queryOptions).checkPermission(permission);
            return tristate.asBoolean();
        });
    }
}
