package top.mrxiaom.sweetmail.func.language;

import org.bukkit.command.CommandSender;
import top.mrxiaom.sweetmail.utils.Pair;
import top.mrxiaom.sweetmail.utils.Util;

import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked", "unused", "UnusedReturnValue"})
public interface IHolderAccessor {

    AbstractLanguageHolder holder();
    
    default String str() {
        return holder().str();
    }
    default String str(Object... args) {
        return holder().str(args);
    }
    default String str(Pair... replacements) {
        return holder().str(replacements);
    }
    default String str(Iterable<Pair<String, Object>> replacements) {
        return holder().str(replacements);
    }
    default List<String> list() {
        return holder().list();
    }
    default List<String> list(Object... args) {
        return holder().list(args);
    }
    default List<String> list(Pair... replacements) {
        Pair<String, Object>[] array = new Pair[replacements.length];
        for (int i = 0; i < replacements.length; i++) {
            array[i] = replacements[i].cast();
        }
        return holder().list(array);
    }
    default List<String> list(Iterable<Pair<String, Object>> replacements) {
        return holder().list(replacements);
    }
    default boolean tm(CommandSender receiver) {
        Util.sendMessage(receiver, str());
        return true;
    }
    default boolean tm(CommandSender receiver, Object... args) {
        Util.sendMessage(receiver, str(args));
        return true;
    }
    default boolean tm(CommandSender receiver, Pair... replacements) {
        Util.sendMessage(receiver, str(replacements));
        return true;
    }
    default boolean tm(CommandSender receiver, Iterable<Pair<String, Object>> replacements) {
        Util.sendMessage(receiver, str(replacements));
        return true;
    }
}
