package top.mrxiaom.sweetmail;

import com.google.common.collect.Lists;
import top.mrxiaom.sweetmail.func.language.IHolderAccessor;
import top.mrxiaom.sweetmail.func.language.Language;
import top.mrxiaom.sweetmail.func.language.LanguageEnumAutoHolder;

import java.util.List;

import static top.mrxiaom.sweetmail.func.language.LanguageEnumAutoHolder.wrap;

@Language(prefix = "messages.")
public enum Messages implements IHolderAccessor {
    prefix("&7[&e&l邮件&7] &a"),
    date_time("yyyy年MM月dd日 HH:mm:ss"),
    help__player(
            "&e&l邮件系统 &8<必选参数> [可选参数]",
            "  &f/mail draft &8-- &3创建或编辑一个草稿",
            "  &f/mail outbox &8-- &3打开发件箱",
            "  &f/mail inbox &7[&fall&7/&funread&7] &8-- &3打开收件箱&7(默认打开未读)"),
    help__admin(
            "&c管理员命令",
            "  &f/mail outbox <玩家> &8-- &e为某人打开发件箱",
            "  &f/mail inbox <all/unread> <玩家> &8-- &e为某人打开收件箱",
            "  &f/mail admin outbox <玩家> &8-- &e打开某玩家的发件箱",
            "  &f/mail admin inbox <all/unread> <玩家> &8-- &e打开某玩家的收件箱",
            "  &f/mail admin timed <定时序列> &8-- &e查看定时发送邮件序列简要信息",
            "  &f/mail admin cancel <定时序列> &8-- &e取消定时发送邮件序列",
            "  &f/mail save <模板> &8-- &e将你的草稿保存为邮件模板",
            "  &f/mail send <模板> <接收者表达式> [参数...] &8-- &e根据已配置的模板发送邮件（接收者格式详见文档）",
            "  &f/mail players <接收者表达式> [--book/-b] &8-- &e计算接收者表达式最终的玩家列表（用于测试）",
            "  &f/mail reload database &8-- &3重载数据库",
            "  &f/mail reload &8-- &3重载配置文件"),
    legacy__1_7_10__need_empty_hand("&e在创造模式下，为了避免将物品卡掉，需要&b空手&e才能浏览正文"),
    legacy__1_7_10__need_right_click("&e出于版本限制，你需要按下&b鼠标右键&e来浏览正文"),

    ;

    Messages(String defaultValue) {
        holder = wrap(this, defaultValue);
    }
    Messages(String... defaultValue) {
        holder = wrap(this, defaultValue);
    }
    Messages(List<String> defaultValue) {
        holder = wrap(this, defaultValue);
    }
    private final LanguageEnumAutoHolder<Messages> holder;
    public LanguageEnumAutoHolder<Messages> holder() {
        return holder;
    }

    @Language(prefix = "messages.command.")
    public enum Command implements IHolderAccessor {
        reload("&a配置文件已重载 &7(如需重载数据库，请使用 &f/mail reload database&7)"),
        reload_database("&a数据库配置已重载，已重新连接数据库"),

        timed__info__display(
                "&f定时发送序列: &e%id%",
                "&f发送人: &e%sender%",
                "&f发送人显示: &e%senderDisplay%",
                "&f收件人: &e%receiver%",
                "&f泛收件人: &e%advReceivers%",
                "&b其它信息请到 timed_draft.yml 查看"),
        timed__info__not_found("&c找不到定时发送序列 &e%id%"),
        timed__cancel__success("&a成功取消定时发送序列 &e%id%"),
        timed__cancel__fail("&c找不到定时发送序列 &e%id%"),

        send__no_template("&e邮件模板 %template% 不存在"),
        send__no_players("&e输入的接收者表达式共筛选出 0 位玩家"),
        send__wrong_arguments("&e参数错误 %error%"),
        send__failed("&e邮件发送失败: %error%"),
        send__success("&a成功向 %players_count% 位玩家发送模板邮件 %template% %parameters%"),

        players__empty("&a接收者表达式&e %formula% &e共计算出&c 0 &e名玩家"),
        players__chat_header("&a接收者表达式&e %formula% &e共计算出&e %players_count% &e名玩家，前&e %count% &a位名单如下:"),
        players__chat_entry("&7-&f <hover:show_text:%player_uuid%>%player_name%</hover>"),
        players__book_header("共 %players_count% 名玩家"),
        players__book_entry("- <hover:show_text:%player_uuid%>%player_name%</hover>"),
        ;

        Command(String defaultValue) {
            holder = wrap(this, defaultValue);
        }
        Command(String... defaultValue) {
            holder = wrap(this, defaultValue);
        }
        Command(List<String> defaultValue) {
            holder = wrap(this, defaultValue);
        }
        private final LanguageEnumAutoHolder<Command> holder;
        public LanguageEnumAutoHolder<Command> holder() {
            return holder;
        }
    }
    @Language(prefix = "messages.draft.")
    public enum Draft implements IHolderAccessor {
        tips_date_time("yyyy-MM-dd HH:mm:ss"),
        cursor_no_book("&e要覆盖邮件正文内容，你应该先使用鼠标指针，在物品栏&b拿起&e一个&b书与笔&e，再&a左键点击&e正文内容图标。"),
        online__no_player("&e找不到该玩家"),
        no_money("&e你没有足够的金币&7(%price%)&e去发送邮件"),
        money_format("%.1f"),
        selected_icon_lore(Lists.newArrayList("&a&l已选择")),
        no_receivers("&e你没有设置接收者，无法发送邮件!"),
        cant_send_to_yourself("&e你不能发送邮件给你自己!"),
        open_tips("&f你的草稿将会保存 &e%hours% &f小时，在&e %time% &f过期，请勿在附件存放贵重物品。草稿过期时间会在重新打开草稿编辑器时刷新。"),
        outdate_tips("&f你的草稿上次在&e %time% &f编辑，现已过期自动重置。"),
        attachments__remove_lore(
                "",
                "&a左键 &7| &f删除并取出该附件"),
        attachments__remove_lore_admin(
                "",
                "&a左键 &7| &f删除并取出该附件",
                "&eShift+左键 &7| &f删除"),
        attachments__money__icon("GOLD_NUGGET"),
        attachments__money__name("&e&l金币"),
        attachments__money__lore("", "  &f数量: &e%money%"),
        attachments__money__add__prompt_tips("&7[&e&l邮件&7] &b请在聊天栏发送&e“附件金币数量”&b的值 &7(输入&c cancel &7取消添加附件)"),
        attachments__money__add__prompt_cancel("cancel"),
        attachments__money__add__fail("&7[&e&l邮件&7] &e请输入大于0的实数"),
        attachments__money__add__not_enough("&7[&e&l邮件&7] &e你没有足够的金币"),
        attachments__item__display("%item%"),
        attachments__item__display_with_amount("%item%&7 x%amount%"),
        attachments__item__title("请在物品栏点击要添加附件的物品"),
        attachments__item__banned("&e禁止添加该物品到附件"),
        attachments__command__prompt_tips("&7[&e&l邮件&7] &b请在聊天栏发送&e“控制台命令附件”&b的值 &7(格式 &f图标,显示名称,执行命令&7，如&f PAPER,10金币,money give %player_name% 10 &7。输入&c cancel &7取消添加附件)"),
        attachments__command__prompt_cancel("cancel"),
        attachments__command__fail("&7[&e&l邮件&7] &e格式不正确，应为 &f图标,显示名称,执行命令&e，如&f PAPER,10金币,money give %player_name% 10"),
        attachments__use_illegal_deny("&b附件 &e%name% &b的数据不合规，无法领取，请联系管理员。"),
        send_with_adv_receivers("&a正在发送邮件… &f发送所需时间可能会比较长，请耐心等待，这由服务器的玩家总数决定。"),
        sent("&a邮件发送成功"),

        ;

        Draft(String defaultValue) {
            holder = wrap(this, defaultValue);
        }
        Draft(String... defaultValue) {
            holder = wrap(this, defaultValue);
        }
        Draft(List<String> defaultValue) {
            holder = wrap(this, defaultValue);
        }
        private final LanguageEnumAutoHolder<Draft> holder;
        public LanguageEnumAutoHolder<Draft> holder() {
            return holder;
        }
    }
    @Language(prefix = "messages.inbox.")
    public enum InBox implements IHolderAccessor {
        attachments_outdated("&e该邮件的附件已过期，无法领取"),
        attachments_fail("&e有附件领取失败! 请联系管理员查看后台了解详细"),
        read_all("&a所有未读邮件已标为已读"),
        ;

        InBox(String defaultValue) {
            holder = wrap(this, defaultValue);
        }
        InBox(String... defaultValue) {
            holder = wrap(this, defaultValue);
        }
        InBox(List<String> defaultValue) {
            holder = wrap(this, defaultValue);
        }
        private final LanguageEnumAutoHolder<InBox> holder;
        public LanguageEnumAutoHolder<InBox> holder() {
            return holder;
        }
    }
    @Language(prefix = "messages.outbox.")
    public enum OutBox implements IHolderAccessor {
        deleted("&a你已成功删除 %player% 的邮件 %title%&7 (%uuid%)"),
        ;

        OutBox(String defaultValue) {
            holder = wrap(this, defaultValue);
        }
        OutBox(String... defaultValue) {
            holder = wrap(this, defaultValue);
        }
        OutBox(List<String> defaultValue) {
            holder = wrap(this, defaultValue);
        }
        private final LanguageEnumAutoHolder<OutBox> holder;
        public LanguageEnumAutoHolder<OutBox> holder() {
            return holder;
        }
    }
    @Language(prefix = "messages.join.")
    public enum Join implements IHolderAccessor {
        text("&e你有 &b%count% &e封未读邮件 &7[&a点击查看&7]"),
        text_online("&e你有一封新的未读邮件 &7[&a点击查看&7]"),
        hover(Lists.newArrayList("&e点击打开收件箱")),
        command("/sweetmail inbox unread"),
        ;

        Join(String defaultValue) {
            holder = wrap(this, defaultValue);
        }
        Join(String... defaultValue) {
            holder = wrap(this, defaultValue);
        }
        Join(List<String> defaultValue) {
            holder = wrap(this, defaultValue);
        }
        private final LanguageEnumAutoHolder<Join> holder;
        public LanguageEnumAutoHolder<Join> holder() {
            return holder;
        }
    }
}
