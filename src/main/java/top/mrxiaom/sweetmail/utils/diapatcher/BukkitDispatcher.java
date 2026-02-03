package top.mrxiaom.sweetmail.utils.diapatcher;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class BukkitDispatcher implements ICommandDispatcher {
    public static final BukkitDispatcher INSTANCE = new BukkitDispatcher();
    private BukkitDispatcher() {}

    @Override
    public void dispatchCommand(@NotNull CommandSender sender, @NotNull String commandLine) {
        Bukkit.dispatchCommand(sender, commandLine);
    }
}
