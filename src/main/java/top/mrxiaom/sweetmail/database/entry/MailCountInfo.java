package top.mrxiaom.sweetmail.database.entry;

public class MailCountInfo {
    public static final MailCountInfo ZERO = new MailCountInfo(0, 0, 0);
    public final int unreadCount;
    public final int usedCount;
    public final int totalCount;

    public MailCountInfo(int unreadCount, int usedCount, int totalCount) {
        this.unreadCount = unreadCount;
        this.usedCount = usedCount;
        this.totalCount = totalCount;
    }
}
