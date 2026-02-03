package top.mrxiaom.sweetmail.utils.diapatcher;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.sweetmail.utils.scheduler.IScheduler;

public class FoliaDispatcher implements ICommandDispatcher {
    private final IScheduler scheduler;
    public FoliaDispatcher(IScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void dispatchCommand(@NotNull CommandSender sender, @NotNull String commandLine) {
        if (sender instanceof Entity) {
            scheduler.runAtEntity((Entity) sender, () -> Bukkit.dispatchCommand(sender, commandLine));
        } else {
            Bukkit.dispatchCommand(sender, commandLine);
        }
    }
}
