package top.mrxiaom.sweetmail.book;

import net.kyori.adventure.inventory.Book;
import org.bukkit.entity.Player;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.func.data.Draft;
import top.mrxiaom.sweetmail.utils.Util;

public class DefaultBook implements IBook {
    @Override
    public void openBook(Player player, Draft draft) {
        Book book = Util.legacyBook(draft.title, draft.content, player.getName());
        Util.openBook(player, book);
    }

    @Override
    public void openBook(Player player, Mail mail) {
        Util.openBook(player, mail.generateBook());
    }
}
