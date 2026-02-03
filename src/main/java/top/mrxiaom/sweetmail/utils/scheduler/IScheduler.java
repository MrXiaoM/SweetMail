package top.mrxiaom.sweetmail.utils.scheduler;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * 方便对接 FoliaLib 的调度器接口
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public interface IScheduler {
    @NotNull IRunTask runTask(@NotNull Runnable runnable);
    @NotNull IRunTask runTaskLater(@NotNull Runnable runnable, long delay);
    @NotNull IRunTask runTaskTimer(@NotNull Runnable runnable, long delay, long period);
    @NotNull IRunTask runTaskAsync(@NotNull Runnable runnable);
    @NotNull IRunTask runTaskLaterAsync(@NotNull Runnable runnable, long delay);
    @NotNull IRunTask runTaskTimerAsync(@NotNull Runnable runnable, long delay, long period);
    <T extends Entity> void runAtEntity(@NotNull T entity, @NotNull Consumer<T> runnable);
    <T extends Entity> @NotNull IRunTask runAtEntityLater(@NotNull T entity, @NotNull Consumer<T> runnable, long delay);
    <T extends Entity> @NotNull IRunTask runAtEntityTimer(@NotNull T entity, @NotNull Consumer<T> runnable, long delay, long period);
    void runAtLocation(@NotNull Location location, @NotNull Consumer<Location> runnable);
    @NotNull IRunTask runAtLocationLater(@NotNull Location location, @NotNull Consumer<Location> runnable, long delay);
    @NotNull IRunTask runAtLocationTimer(@NotNull Location location, @NotNull Consumer<Location> runnable, long delay, long period);
    void teleport(@NotNull Entity entity, @NotNull Location location, @NotNull PlayerTeleportEvent.TeleportCause cause, @Nullable Consumer<Entity> then);
    void teleport(@NotNull Entity entity, @NotNull Location location, @Nullable Consumer<Entity> then);
    default @NotNull IRunTask runTaskAsynchronously(@NotNull Runnable runnable) {
        return runTaskAsync(runnable);
    }
    default @NotNull IRunTask runTaskLaterAsynchronously(@NotNull Runnable runnable, long delay) {
        return runTaskLaterAsync(runnable, delay);
    }
    default @NotNull IRunTask runTaskTimerAsynchronously(@NotNull Runnable runnable, long delay, long period) {
        return runTaskTimerAsync(runnable, delay, period);
    }
    default <T extends Entity> void runAtEntity(@NotNull T entity, @NotNull Runnable runnable) {
        runAtEntity(entity, e -> runnable.run());
    }
    default <T extends Entity> @NotNull IRunTask runAtEntityLater(@NotNull T entity, @NotNull Runnable runnable, long delay) {
        return runAtEntityLater(entity, e -> runnable.run(), delay);
    }
    default <T extends Entity> @NotNull IRunTask runAtEntityTimer(@NotNull T entity, @NotNull Runnable runnable, long delay, long period) {
        return runAtEntityTimer(entity, e -> runnable.run(), delay, period);
    }
    default void runAtLocation(@NotNull Location location, @NotNull Runnable runnable) {
        runAtLocation(location, l -> runnable.run());
    }
    default @NotNull IRunTask runAtLocationLater(@NotNull Location location, @NotNull Runnable runnable, long delay) {
        return runAtLocationLater(location, l -> runnable.run(), delay);
    }
    default @NotNull IRunTask runAtLocationTimer(@NotNull Location location, @NotNull Runnable runnable, long delay, long period) {
        return runAtLocationTimer(location, l -> runnable.run(), delay, period);
    }

    void cancelTasks();
}
