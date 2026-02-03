package top.mrxiaom.sweetmail.utils.diapatcher;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public interface ICommandDispatcher {
    void dispatchCommand(@NotNull CommandSender sender, @NotNull String commandLine);
}
