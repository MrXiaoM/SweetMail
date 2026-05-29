package top.mrxiaom.sweetmail.api;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.utils.adventure.serializer.legacy.LegacyComponentSerializer;

import java.util.List;
import java.util.Objects;

public interface IAdventureHandler {
    @NotNull
    MiniMessage.Builder builder();
    @NotNull
    @ApiStatus.Experimental
    Audience of(@NotNull CommandSender sender);
    @NotNull
    @ApiStatus.Experimental
    default Audience of(@NotNull Player player) {
        return of((CommandSender) player);
    }
    @NotNull
    @ApiStatus.Experimental
    default Audience console() {
        return of(Bukkit.getConsoleSender());
    }
    @NotNull
    MiniMessage miniMessage();

    @NotNull
    Component miniMessage(@NotNull MiniMessage miniMessage, @Nullable String s);
    @NotNull
    default Component miniMessage(@Nullable String s) {
        return miniMessage(miniMessage(), s);
    }

    @NotNull
    String miniMessage(@NotNull MiniMessage miniMessage, @Nullable Component component);
    @NotNull
    default String miniMessage(@Nullable Component component) {
        return miniMessage(miniMessage(), component);
    }

    @NotNull
    List<Component> miniMessage(MiniMessage miniMessage, List<String> list);
    @NotNull
    default List<Component> miniMessage(List<String> list) {
        return miniMessage(miniMessage(), list);
    }
    @NotNull
    Component miniMessageLines(MiniMessage miniMessage, List<String> list);
    @NotNull
    default Component miniMessageLines(List<String> list) {
        return miniMessageLines(miniMessage(), list);
    }

    @NotNull
    List<String> miniMessage_(MiniMessage miniMessage, List<Component> components);
    @NotNull
    default List<String> miniMessage_(List<Component> components) {
        return miniMessage_(miniMessage(), components);
    }

    @NotNull
    default String plain(@NotNull Component component) {
        Component input = Objects.requireNonNull(component, "component");
        StringBuilder sb = new StringBuilder();
        ComponentFlattener.basic().flatten(input, sb::append);
        return sb.toString();
    }

    @NotNull
    default String legacyAmpersand(@NotNull Component component) {
        Component input = Objects.requireNonNull(component, "component");
        return LegacyComponentSerializer.legacyAmpersand().serialize(input);
    }

    @NotNull
    default String legacySection(@NotNull Component component) {
        Component input = Objects.requireNonNull(component, "component");
        return LegacyComponentSerializer.legacySection().serialize(input);
    }

    void sendTitle(@NotNull Player player, @NotNull Component title, @NotNull Component subTitle, int fadeIn, int stay, int fadeOut);
    void sendTitle(@NotNull Player player, @NotNull MiniMessage miniMessage, @NotNull String title, @NotNull String subTitle, int fadeIn, int stay, int fadeOut);
    default void sendTitle(@NotNull Player player, @NotNull String title, @NotNull String subTitle, int fadeIn, int stay, int fadeOut) {
        sendTitle(player, miniMessage(), title, subTitle, fadeIn, stay, fadeOut);
    }
    void resetTitle(@NotNull Player player);
    void clearTitle(@NotNull Player player);

    void sendMessage(@NotNull CommandSender sender, @NotNull Component message);
    void sendMessage(@NotNull CommandSender sender, @NotNull MiniMessage miniMessage, @NotNull String message);
    default void sendMessage(@NotNull CommandSender sender, @NotNull String message) {
        sendMessage(sender, miniMessage(), message);
    }

    void sendActionBar(@NotNull Player player, @NotNull Component message);
    void sendActionBar(@NotNull Player player, @NotNull MiniMessage miniMessage, @NotNull String message);
    default void sendActionBar(@NotNull Player player, @NotNull String message) {
        sendActionBar(player, miniMessage(), message);
    }

    @NotNull
    String legacyToMiniMessage(@NotNull String legacy);

}
