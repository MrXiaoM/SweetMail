package top.mrxiaom.sweetmail.book;

import org.bukkit.entity.Player;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.func.data.Draft;

public interface IBook {
    void openBook(Player player, Draft draft);

    void openBook(Player player, Mail mail);
}
