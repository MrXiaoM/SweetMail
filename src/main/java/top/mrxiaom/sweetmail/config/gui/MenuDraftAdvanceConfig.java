package top.mrxiaom.sweetmail.config.gui;

import com.google.common.collect.Lists;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.config.AbstractMenuConfig;
import top.mrxiaom.sweetmail.func.DraftManager;
import top.mrxiaom.sweetmail.func.TimerManager;
import top.mrxiaom.sweetmail.func.data.Draft;
import top.mrxiaom.sweetmail.gui.AbstractDraftGui;
import top.mrxiaom.sweetmail.utils.ChatPrompter;
import top.mrxiaom.sweetmail.utils.Pair;
import top.mrxiaom.sweetmail.utils.Util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static top.mrxiaom.sweetmail.commands.CommandMain.parseLocalTimeOrZero;
import static top.mrxiaom.sweetmail.utils.Util.toTimestamp;

public class MenuDraftAdvanceConfig extends AbstractMenuConfig<MenuDraftAdvanceConfig.Gui> {
    Icon iconSenderDisplay;
    String iconSenderDisplayPromptTips;
    String iconSenderDisplayPromptCancel;
    String iconSenderDisplayUnset;
    Icon iconReceivers;
    String iconReceiversPrompts3Tips;
    String iconReceiversPrompts3Cancel;
    String iconReceiversPrompts4TipsStart;
    String iconReceiversPrompts4TipsEnd;
    String iconReceiversPrompts4Cancel;
    String iconReceiversPrompts5Tips;
    String iconReceiversPrompts5Cancel;
    String iconReceiversUnset;
    String iconReceiversBadTimeFormat;
    Icon iconTimed;
    String iconTimedPromptTips;
    String iconTimedPromptCancel;
    String iconTimedPromptWrongFormat;
    String iconTimedSuccess;
    Icon iconOutdate;
    String iconOutdatePromptTips;
    String iconOutdatePromptCancel;
    String iconOutdatePromptNotInteger;
    String iconOutdateSet;
    String iconOutdateUnset;
    String iconOutdateUnlimited;
    Icon iconBack;
    public MenuDraftAdvanceConfig(SweetMail plugin) {
        super(plugin, "menus/draft_advance.yml");
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        super.reloadConfig(cfg);
    }

    @Override
    protected void clearMainIcons() {
        iconBack = null;
    }

    @Override
    protected boolean loadMainIcon(ConfigurationSection section, String key, Icon loadedIcon) {
        switch (key) {
            case "系": {
                iconSenderDisplay = loadedIcon;
                iconSenderDisplayPromptTips = section.getString(key + ".prompt-tips", "&7[&e&l邮件&7] &b请在聊天栏发送&e“发件人显示名称”&b的值 &7(输入&c cancel &7取消设置)");
                iconSenderDisplayPromptCancel = section.getString(key + ".prompt-cancel", "cancel");
                iconSenderDisplayUnset = section.getString(key + ".unset", "&7未设置");
                return true;
            }
            case "收": {
                iconReceivers = loadedIcon;
                iconReceiversPrompts3Tips = section.getString(key + ".prompts.3.tips", "&7[&e&l邮件&7] &b请在聊天栏发送，&f“多久之前到现在，上过线的玩家”&b的 &e判定起始时间 &7(格式 &f年-月-日 时:分:秒&7，不输入时分秒部分默认为0。输入&c cancel &7取消设置)");
                iconReceiversPrompts3Cancel = section.getString(key + ".prompts.3.cancel", "cancel");
                iconReceiversPrompts4TipsStart = section.getString(key + ".prompts.4.tips-start", "&7[&e&l邮件&7] &b请在聊天栏发送，&f“在某段时间内上过线的玩家”&b的 &e判定起始时间 &7(格式 &f年-月-日 时:分:秒&7，不输入时分秒部分默认为0。输入&c cancel &7取消设置)");
                iconReceiversPrompts4TipsEnd = section.getString(key + ".prompts.4.tips-end", "&7[&e&l邮件&7] &b请在聊天栏发送，&f“在某段时间内上过线的玩家”&b的 &e判定结束时间 &7(格式 &f年-月-日 时:分:秒&7，不输入时分秒部分默认为0。输入&c cancel &7取消设置)");
                iconReceiversPrompts4Cancel = section.getString(key + ".prompts.4.cancel", "cancel");
                iconReceiversPrompts5Tips = section.getString(key + ".prompts.5.tips", "&7[&e&l邮件&7] &b请在聊天栏发送玩家列表，使用逗号分隔 &7(输入&c cancel &7取消设置)");
                iconReceiversPrompts5Cancel = section.getString(key + ".prompts.5.cancel", "cancel");
                iconReceiversUnset = section.getString(key + ".unset", "&7未设置");
                iconReceiversBadTimeFormat = section.getString(key + ".bad-time-format", "&7[&e&l邮件&7] &f你输入的时间格式不正确!");
                return true;
            }
            case "定": {
                iconTimed = loadedIcon;
                iconTimedPromptTips = section.getString(key + ".prompt-tips", "&7[&e&l邮件&7] &b请在聊天栏发送&e“定时发送时间”&b的值，并立即加入定时发送队列 &7(格式 &f年-月-日 时:分:秒&7，不输入时分秒部分默认为0。输入&c cancel &7取消定时发送)");
                iconTimedPromptCancel = section.getString(key + ".prompt-cancel", "cancel");
                iconTimedPromptWrongFormat = section.getString(key + ".prompt-wrong-format", "&e时间格式不正确");
                iconTimedSuccess = section.getString(key + ".success", "&a邮件已成功加入到定时发送队列");
                return true;
            }
            case "过": {
                iconOutdate = loadedIcon;
                iconOutdatePromptTips = section.getString(key + ".prompt-tips", "&7[&e&l邮件&7] &b请在聊天栏发送&e“附件过期时间”&b的值 &7(单位为天数。输入&c cancel &7取消设置)");
                iconOutdatePromptCancel = section.getString(key + ".prompt-cancel", "cancel");
                iconOutdatePromptNotInteger = section.getString(key + ".prompt-not-integer", "&e请输入一个整数");
                iconOutdateSet = section.getString(key + ".set", "%value% 天");
                iconOutdateUnset = section.getString(key + ".unset", "&7未设置");
                iconOutdateUnlimited = section.getString(key + ".unlimited", "&b无限制");
                return true;
            }
            case "返": {
                iconBack = loadedIcon;
                return true;
            }
        }
        return false;
    }

    @Override
    protected ItemStack tryApplyMainIcon(Gui gui, String key, Player target, int iconIndex) {
        DraftManager manager = DraftManager.inst();
        Draft draft = manager.getDraft(target);
        switch (key) {
            case "系": {
                String senderDisplay = draft.advSenderDisplay == null || draft.advSenderDisplay.isEmpty()
                        ? iconSenderDisplayUnset
                        : draft.advSenderDisplay;
                return iconSenderDisplay.generateIcon(gui, target, Pair.of("%sender%", senderDisplay));
            }
            case "收": {
                String receivers = draft.advReceivers == null || draft.advReceivers.isEmpty()
                        ? iconReceiversUnset
                        : draft.advReceivers;
                return iconReceivers.generateIcon(gui, target, Pair.of("%receivers%", receivers));
            }
            case "定": {
                return iconTimed.generateIcon(gui, target);
            }
            case "过": {
                String outdate;
                if (draft.outdateDays == 0) outdate = iconOutdateUnset;
                else if (draft.outdateDays < 0) outdate = iconOutdateUnlimited;
                else outdate = iconOutdateSet.replace("%value%", String.valueOf(draft.outdateDays));
                return iconOutdate.generateIcon(gui, target, Pair.of("%outdate%", outdate));
            }
            case "返": {
                return iconBack.generateIcon(gui, target);
            }
        }
        return null;
    }

    public static MenuDraftAdvanceConfig inst() {
        return instanceOf(MenuDraftAdvanceConfig.class);
    }

    public class Gui extends AbstractDraftGui {
        public Gui(SweetMail plugin, Player player) {
            super(plugin, player);
        }

        @Override
        public Inventory newInventory() {
            created = createInventory(this, player);
            applyIcons(this, created, player);
            return created;
        }

        @Override
        public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
            Character c = getSlotKey(slot);
            if (c == null) return;
            event.setCancelled(true);

            switch (String.valueOf(c)) {
                case "系": {
                    if (!click.isShiftClick()) {
                        if (click.isLeftClick()) {
                            player.closeInventory();
                            ChatPrompter.prompt(
                                    plugin, player,
                                    iconSenderDisplayPromptTips,
                                    iconSenderDisplayPromptCancel,
                                    advSenderDisplay -> {
                                        draft.advSenderDisplay = advSenderDisplay;
                                        draft.save();
                                        reopen.run();
                                    }, reopen);
                        }
                        if (click.isRightClick()) {
                            draft.advSenderDisplay = null;
                            draft.save();
                            applyIcon(this, view, player, slot);
                            Util.updateInventory(player);
                        }
                    }
                    return;
                }
                case "收": {
                    if (!click.isShiftClick()) {
                        if (click.isRightClick()) {
                            draft.advReceivers = null;
                            draft.save();
                            applyIcon(this, view, player, slot);
                            Util.updateInventory(player);
                            return;
                        }
                        if (click.equals(ClickType.DROP)) {
                            t(player, "正在计算泛接收人列表，请稍等…");
                            plugin.getScheduler().runTaskAsync(() -> {
                                List<String> receivers = draft.advReceivers();
                                if (receivers.isEmpty()) {
                                    t(player, "(空)");
                                } else if (receivers.size() < 16) {
                                    t(player, String.join(", ", receivers));
                                } else {
                                    List<String> list = Lists.partition(receivers, 16).get(0);
                                    t(player, String.join(", ", list) + "... (" + receivers.size() + ")");
                                }
                            });
                            player.closeInventory();
                            return;
                        }
                        if (click.equals(ClickType.NUMBER_KEY)) {
                            int btn = event.getHotbarButton() + 1;
                            switch (btn) {
                                case 1: {
                                    draft.advReceivers = "current online";
                                    draft.save();
                                    break;
                                }
                                case 2: {
                                    draft.advReceivers = "current online bungeecord";
                                    draft.save();
                                    break;
                                }
                                case 3: {
                                    player.closeInventory();
                                    Consumer<String> receiver1 = timeStr -> {
                                        Long timestamp = parseTime(timeStr);
                                        if (timestamp == null) {
                                            t(player, iconReceiversBadTimeFormat);
                                        } else {
                                            draft.advReceivers = "last played in " + timestamp;
                                            draft.save();
                                        }
                                        reopen.run();
                                    };
                                    ChatPrompter.prompt(
                                            plugin, player,
                                            iconReceiversPrompts3Tips,
                                            iconReceiversPrompts3Cancel,
                                            receiver1, reopen);
                                    return;
                                }
                                case 4: {
                                    player.closeInventory();

                                    AtomicReference<Consumer<Long>> nextPrompt = new AtomicReference<>();
                                    Consumer<String> receiver1 = timeStr -> {
                                        Long timestampStart = parseTime(timeStr);
                                        if (timestampStart == null) {
                                            t(player, iconReceiversBadTimeFormat);
                                            reopen.run();
                                            return;
                                        }
                                        nextPrompt.get().accept(timestampStart);
                                    };
                                    BiConsumer<String, Long> receiver2 = (timeStr, timestampStart) -> {
                                        Long timestampEnd = parseTime(timeStr);
                                        if (timestampEnd == null) {
                                            t(player, iconReceiversBadTimeFormat);
                                            reopen.run();
                                            return;
                                        }
                                        draft.advReceivers = "last played from " + timestampStart + " to " + timestampEnd;
                                        draft.save();
                                        reopen.run();
                                    };

                                    ChatPrompter.prompt(
                                            plugin, player,
                                            iconReceiversPrompts4TipsStart,
                                            iconReceiversPrompts4Cancel,
                                            receiver1, reopen);
                                    nextPrompt.set(timestampStart -> ChatPrompter.prompt(
                                            plugin, player,
                                            iconReceiversPrompts4TipsEnd,
                                            iconReceiversPrompts4Cancel,
                                            timeStr -> receiver2.accept(timeStr, timestampStart), reopen));
                                    break;
                                }
                                case 5: {
                                    player.closeInventory();
                                    Consumer<String> receiver1 = str -> {
                                        String[] split = str.split("[，、；;,]");
                                        for (int i = 0; i < split.length; i++) {
                                            split[i] = split[i].trim();
                                        }
                                        draft.advReceivers = "players " + String.join(",", split);
                                        draft.save();
                                        reopen.run();
                                    };
                                    ChatPrompter.prompt(
                                            plugin, player,
                                            iconReceiversPrompts5Tips,
                                            iconReceiversPrompts5Cancel,
                                            receiver1, reopen);
                                    return;
                                }
                                default:
                                    return;
                            }
                            applyIcon(this, view, player, slot);
                            Util.updateInventory(player);
                            return;
                        }
                    }
                    return;
                }
                case "定": {
                    player.closeInventory();

                    ChatPrompter.prompt(plugin, player,
                            iconTimedPromptTips, iconTimedPromptCancel,
                            receive -> {
                                String[] split = receive.split(" ", 2);
                                LocalDateTime localDateTime;
                                try {
                                    LocalDate date = LocalDate.parse(split[0]);
                                    LocalTime time = split.length > 1 ? LocalTime.parse(split[1]) : LocalTime.of(0, 0, 0);
                                    localDateTime = date.atTime(time);
                                } catch (DateTimeParseException ignored) {
                                    t(player, iconTimedPromptWrongFormat);
                                    reopen.run();
                                    return;
                                }
                                long time = toTimestamp(localDateTime);
                                TimerManager.inst().sendInTime(draft, time);
                                draft.reset();
                                draft.save();
                                t(player, iconTimedSuccess);
                            }, reopen);
                    return;
                }
                case "过": {
                    if (!click.isShiftClick()) {
                        if (click.isLeftClick()) {
                            player.closeInventory();
                            ChatPrompter.prompt(plugin, player,
                                    iconOutdatePromptTips, iconOutdatePromptCancel,
                                    receive -> {
                                        Integer i = Util.parseInt(receive).orElse(null);
                                        if (i == null) {
                                            t(player, iconOutdatePromptNotInteger);
                                        } else {
                                            draft.outdateDays = i;
                                        }
                                        reopen.run();
                                    }, reopen);
                        }
                        if (click.isRightClick()) {
                            draft.outdateDays = 0;
                            applyIcons(this, view, player);
                        }
                    } else {
                        if (click.isLeftClick()) {
                            draft.outdateDays = -1;
                            applyIcons(this, view, player);
                        }
                    }
                    return;
                }
                case "返": {
                    if (click.isLeftClick() && !click.isShiftClick()) {
                        MenuDraftConfig.inst()
                                .new Gui(plugin, player)
                                .open();
                    }
                    return;
                }
                default: {
                    handleClick(player, click, c);
                }
            }
        }
    }

    @Nullable
    public static Long parseTime(String s) {
        String[] split = s.split(" ", 2);
        String[] dateSplit = split[0].split("-", 3);
        if (dateSplit.length != 3) return null;
        Integer year = Util.parseInt(dateSplit[0]).orElse(null);
        Integer month = Util.parseInt(dateSplit[1]).orElse(null);
        Integer date = Util.parseInt(dateSplit[2]).orElse(null);
        if (year == null || month == null || date == null) return null;
        LocalDate localDate = LocalDate.of(year, month, date);
        LocalTime localTime = parseLocalTimeOrZero(split.length > 1 ? split[1] : null);
        LocalDateTime time = localDate.atTime(localTime);
        return toTimestamp(time);
    }
}
