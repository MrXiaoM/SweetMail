package top.mrxiaom.sweet.mail;

import top.mrxiaom.pluginbase.func.language.Language;
import top.mrxiaom.pluginbase.func.language.Message;

import static top.mrxiaom.pluginbase.func.language.LanguageFieldAutoHolder.field;

@Language(prefix="messages.")
public class Messages {
    public static final Message no_permission = field("&c你没有执行该操作的权限");
    public static final Message player__not_online = field("&c玩家不在线 &7(或不存在)");
    public static final Message player__only = field("只有玩家才能执行该操作");
}
