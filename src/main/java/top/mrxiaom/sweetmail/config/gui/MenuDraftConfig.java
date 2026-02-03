package top.mrxiaom.sweetmail.config.gui;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.permissions.Permissible;
import top.mrxiaom.sweetmail.Messages;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.attachments.AttachmentItem;
import top.mrxiaom.sweetmail.attachments.IAttachment;
import top.mrxiaom.sweetmail.config.AbstractMenuConfig;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.depend.PAPI;
import top.mrxiaom.sweetmail.events.PlayerMailSentEvent;
import top.mrxiaom.sweetmail.func.DraftManager;
import top.mrxiaom.sweetmail.func.data.Draft;
import top.mrxiaom.sweetmail.func.data.MailIcon;
import top.mrxiaom.sweetmail.gui.AbstractDraftGui;
import top.mrxiaom.sweetmail.gui.GuiIcon;
import top.mrxiaom.sweetmail.utils.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

import static top.mrxiaom.sweetmail.commands.CommandMain.PERM_ADMIN;
import static top.mrxiaom.sweetmail.utils.Pair.replace;

public class MenuDraftConfig extends AbstractMenuConfig<MenuDraftConfig.Gui> {
    private Icon iconReceiver;
    private Icon iconIcon;
    private Icon iconTitle;
    private Icon iconContent;
    private Icon iconAdvanced;
    private Icon iconReset;
    private Icon iconSend;
    private Icon iconAttachment;
    private String iconReceiverUnset;
    private String iconAdvancedRedirectKey;
    public String iconReceiverPromptTips;
    public String iconReceiverPromptCancel;
    public String iconReceiverWarnNotExists;
    public String iconReceiverRegex;
    public String iconIconTitle;
    public String iconIconTitleCustom;
    public String iconTitlePromptTips;
    public String iconTitlePromptCancel;

    public boolean canSendToYourself;

    private final Map<String, Double> priceMap = new HashMap<>();
    private final Map<String, Integer> outdateDaysMap = new HashMap<>();
    private final Map<String, Integer> outdateDraftHoursMap = new HashMap<>();
    public MenuDraftConfig(SweetMail plugin) {
        super(plugin, "menus/draft.yml");
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        super.reloadConfig(cfg);
        canSendToYourself = cfg.getBoolean("can-send-to-yourself", false);
        priceMap.clear();
        ConfigurationSection section = cfg.getConfigurationSection("price");
        if (section != null) for (String key : section.getKeys(false)) {
            double price = section.getDouble(key);
            priceMap.put(key, price > 0 ? price : 0);
        }
        outdateDaysMap.clear();
        section = cfg.getConfigurationSection("outdate-time");
        if (section != null) for (String key : section.getKeys(false)) {
            int days = section.getInt(key);
            outdateDaysMap.put(key, days);
        }
        outdateDraftHoursMap.clear();
        section = cfg.getConfigurationSection("outdate-draft");
        if (section != null) for (String key : section.getKeys(false)) {
            int hours = section.getInt(key);
            outdateDraftHoursMap.put(key, hours);
        }
    }

    public double getPrice(Permissible permissible) {
        ArrayList<Map.Entry<String, Double>> list = Lists.newArrayList(priceMap.entrySet());
        list.sort(Comparator.comparingDouble(Map.Entry::getValue));
        for (Map.Entry<String, Double> entry : list) {
            if (permissible.hasPermission(entry.getKey())) {
                return entry.getValue();
            }
        }
        return 0;
    }

    public int getOutdateDays(Permissible permissible) {
        ArrayList<Map.Entry<String, Integer>> list = Lists.newArrayList(outdateDaysMap.entrySet());
        list.sort(Comparator.comparingInt(Map.Entry::getValue));
        Collections.reverse(list);
        int max = 0;
        for (Map.Entry<String, Integer> entry : list) {
            if (entry.getValue() <= 0 || entry.getValue() > max) {
                if (permissible.hasPermission("sweetmail.outdate." + entry.getKey())) {
                    if (entry.getValue() <= 0) return entry.getValue();
                    if (entry.getValue() > max) {
                        max = entry.getValue();
                    }
                }
            }
        }
        return max;
    }

    public int getDraftOutdateHours(Permissible permissible) {
        ArrayList<Map.Entry<String, Integer>> list = Lists.newArrayList(outdateDraftHoursMap.entrySet());
        list.sort(Comparator.comparingInt(Map.Entry::getValue));
        Collections.reverse(list);
        int max = 0;
        for (Map.Entry<String, Integer> entry : list) {
            if (entry.getValue() <= 0 || entry.getValue() > max) {
                if (permissible.hasPermission("sweetmail.draft.outdate." + entry.getKey())) {
                    if (entry.getValue() <= 0) return entry.getValue();
                    if (entry.getValue() > max) {
                        max = entry.getValue();
                    }
                }
            }
        }
        return max;
    }

    @Override
    protected void clearMainIcons() {
        iconReceiver = iconIcon = iconTitle = iconContent = iconAdvanced = iconReset = iconSend = iconAttachment = null;
        iconReceiverUnset = iconAdvancedRedirectKey
                = iconReceiverPromptTips = iconReceiverPromptCancel
                = iconTitlePromptTips = iconTitlePromptCancel
                = iconIconTitle = iconIconTitleCustom = null;
    }

    @Override
    protected boolean loadMainIcon(ConfigurationSection section, String key, Icon loadedIcon) {
        switch (key) {
            case "接": {
                iconReceiver = loadedIcon;
                iconReceiverUnset = section.getString(key + ".unset", "&7未设置");
                iconReceiverPromptTips = section.getString(key + ".prompt-tips", "&7[&e&l邮件&7] &b请在聊天栏发送&e“邮件接收者”&b的值 &7(输入&c cancel &7取消设置)");
                iconReceiverPromptCancel = section.getString(key + ".prompt-cancel", "cancel");
                iconReceiverWarnNotExists = section.getString(key + ".warn-not-exists", "%name% &7(&c从未加入过游戏&7)");
                iconReceiverRegex = section.getString(key + ".regex", "^[a-zA-Z0-9_\\u4e00-\\u9fa5]{1,20}");
                return true;
            }
            case "图": {
                iconIcon = loadedIcon;
                iconIconTitle = section.getString(key + ".title", "选择图标");
                iconIconTitleCustom = section.getString(key + ".title-custom", "选择图标 (可在物品栏选择)");
                return true;
            }
            case "题": {
                iconTitle = loadedIcon;
                iconTitlePromptTips = section.getString(key + ".prompt-tips", "&7[&e&l邮件&7] &b请在聊天栏发送&e“邮件标题”&b的值 &7(输入&c cancel &7取消设置)");
                iconTitlePromptCancel = section.getString(key + ".prompt-cancel", "cancel");
                return true;
            }
            case "文": {
                iconContent = loadedIcon;
                return true;
            }
            case "高": {
                iconAdvanced = loadedIcon;
                iconAdvancedRedirectKey = section.getString(key + ".redirect", "黑");
                return true;
            }
            case "重": {
                iconReset = loadedIcon;
                return true;
            }
            case "发": {
                iconSend = loadedIcon;
                return true;
            }
            case "附": {
                iconAttachment = loadedIcon;
                return true;
            }
        }
        return false;
    }

    @Override
    protected String getTitleText(Gui gui, Player target) {
        return replace(PAPI.setPlaceholders(target, title), Pair.of("%title%", gui.getDraft().title));
    }

    @Override
    protected ItemStack tryApplyMainIcon(Gui gui, String key, Player target, int iconIndex) {
        DraftManager manager = DraftManager.inst();
        Draft draft = manager.getDraft(target);
        switch (key) {
            case "接": {
                String receiver;
                if (draft.receiver.isEmpty()) {
                    receiver = iconReceiverUnset;
                } else {
                    String name = Util.getPlayerName(draft.receiver);
                    receiver = Util.getOfflinePlayer(name)
                            .map(OfflinePlayer::getName)
                            .orElseGet(() -> iconReceiverWarnNotExists.replace("%name%", name));
                }
                ItemStack item = iconReceiver.generateIcon(
                        gui, target,
                        Pair.of("%receiver%", receiver)
                );
                if (!draft.receiver.isEmpty() && item.getItemMeta() instanceof SkullMeta) {
                    OfflinePlayer owner = Util.getOfflinePlayerByNameOrUUID(draft.receiver).orElse(null);
                    if (owner != null) {
                        ItemMeta meta = ItemStackUtil.setSkullOwner(item.getItemMeta(), owner);
                        item.setItemMeta(meta);
                    }
                }
                return item;
            }
            case "图": {
                MailIcon icon = manager.getMailIcon(draft.iconKey);
                String itemKey = icon == null ? draft.iconKey.substring(1) : icon.item;
                ItemStack item = ItemStackUtil.getItem(gui.getPlayer(), itemKey);
                String resolvedKey = draft.iconKey;
                if (icon != null && icon.display != null && !draft.iconKey.equals(icon.display)) {
                    resolvedKey = icon.display;
                } else if (resolvedKey.startsWith("!")) {
                    resolvedKey = resolvedKey.substring(1);
                }
                return iconIcon.generateIcon(
                        target, item,
                        Pair.of("%icon%", resolvedKey)
                );
            }
            case "题": {
                return iconTitle.generateIcon(
                        gui, target,
                        Pair.of("%title%", draft.title)
                );
            }
            case "文": {
                return iconContent.generateIcon(
                        gui, target,
                        Pair.of("%content_size%", String.join("", draft.content).length())
                );
            }
            case "高": {
                if (target.hasPermission(PERM_ADMIN)) {
                    return iconAdvanced.generateIcon(gui, target);
                } else {
                    Icon icon = otherIcon.get(iconAdvancedRedirectKey);
                    return icon == null ? null : icon.generateIcon(gui, target);
                }
            }
            case "重": {
                return iconReset.generateIcon(gui, target);
            }
            case "发": {
                return iconSend.generateIcon(
                        gui, target,
                        Pair.of("%price%", String.format(Messages.Draft.money_format.str(), getPrice(target)))
                );
            }
            case "附": {
                if (iconIndex < draft.attachments.size()) {
                    IAttachment attachment = draft.attachments.get(iconIndex);
                    return attachment.generateDraftIcon(target);
                } else {
                    return iconAttachment.generateIcon(gui, target);
                }
            }
        }
        return null;
    }

    public boolean testUsername(String name) {
        Pattern regex = Pattern.compile(iconReceiverRegex);
        return regex.matcher(name).matches();
    }

    /**
     * 根据玩家拥有的权限，返回 MiniMessage 序列化器
     * <ul>
     *     <li><code>sweetmail.format.all</code> 所有标签（不推荐给玩家）</li>
     *     <li><code>sweetmail.format.color.basic</code> 基本颜色和十六进制颜色</li>
     *     <li><code>sweetmail.format.color.gradient</code> 渐变颜色</li>
     *     <li><code>sweetmail.format.color.rainbow</code> 彩虹颜色</li>
     *     <li><code>sweetmail.format.decoration.basic</code> 加粗、斜体、下划线、删除线</li>
     *     <li><code>sweetmail.format.decoration.bold</code> 加粗</li>
     *     <li><code>sweetmail.format.decoration.italic</code> 斜体</li>
     *     <li><code>sweetmail.format.decoration.underline</code> 下划线</li>
     *     <li><code>sweetmail.format.decoration.strike</code> 删除线</li>
     *     <li><code>sweetmail.format.decoration.obfuscated</code> 乱码</li>
     *     <li><code>sweetmail.format.shadow</code> 文字阴影</li>
     *     <li><code>sweetmail.format.font</code> 自定义字体</li>
     *     <li><code>sweetmail.format.translatable</code> 客户端翻译</li>
     *     <li><code>sweetmail.format.keybind</code> 按键显示</li>
     *     <li><code>sweetmail.format.hover</code> 悬停显示</li>
     *     <li><code>sweetmail.format.click</code> 点击操作</li>
     *     <li><code>sweetmail.format.insertion</code> Shift点击插入操作</li>
     * </ul>
     */
    public MiniMessage getMiniMessage(Player player) {
        if (player.hasPermission("sweetmail.format.all")) {
            return MiniMessageConvert.miniMessage();
        }
        List<TagResolver> tags = new ArrayList<>();
        if (player.hasPermission("sweetmail.format.color.basic")) {
            tags.add(StandardTags.color());
        }
        if (player.hasPermission("sweetmail.format.color.gradient")) {
            tags.add(StandardTags.gradient());
        }
        if (player.hasPermission("sweetmail.format.color.rainbow")) {
            tags.add(StandardTags.rainbow());
        }
        if (player.hasPermission("sweetmail.format.decoration.basic")) {
            tags.add(StandardTags.decorations(TextDecoration.BOLD));
            tags.add(StandardTags.decorations(TextDecoration.ITALIC));
            tags.add(StandardTags.decorations(TextDecoration.UNDERLINED));
            tags.add(StandardTags.decorations(TextDecoration.STRIKETHROUGH));
        } else {
            if (player.hasPermission("sweetmail.format.decoration.bold")) {
                tags.add(StandardTags.decorations(TextDecoration.BOLD));
            }
            if (player.hasPermission("sweetmail.format.decoration.italic")) {
                tags.add(StandardTags.decorations(TextDecoration.ITALIC));
            }
            if (player.hasPermission("sweetmail.format.decoration.underline")) {
                tags.add(StandardTags.decorations(TextDecoration.UNDERLINED));
            }
            if (player.hasPermission("sweetmail.format.decoration.strike")) {
                tags.add(StandardTags.decorations(TextDecoration.STRIKETHROUGH));
            }
        }
        if (player.hasPermission("sweetmail.format.decoration.obfuscated")) {
            tags.add(StandardTags.decorations(TextDecoration.OBFUSCATED));
        }
        if (player.hasPermission("sweetmail.format.shadow")) {
            tags.add(StandardTags.shadowColor());
        }
        if (player.hasPermission("sweetmail.format.font")) {
            tags.add(StandardTags.font());
        }
        if (player.hasPermission("sweetmail.format.translatable")) {
            tags.add(StandardTags.translatable());
            tags.add(StandardTags.translatableFallback());
        }
        if (player.hasPermission("sweetmail.format.keybind")) {
            tags.add(StandardTags.keybind());
        }
        if (player.hasPermission("sweetmail.format.hover")) {
            tags.add(StandardTags.hoverEvent());
        }
        if (player.hasPermission("sweetmail.format.click")) {
            tags.add(StandardTags.clickEvent());
        }
        if (player.hasPermission("sweetmail.format.insertion")) {
            tags.add(StandardTags.insertion());
        }
        TagResolver[] array = tags.toArray(new TagResolver[0]);
        return MiniMessage.builder().tags(TagResolver.builder().resolvers(array).build()).build();
    }

    public String format(Player player, String text) {
        MiniMessage miniMessage = getMiniMessage(player);
        Component component = MiniMessageConvert.miniMessage(text);
        return miniMessage.serialize(component);
    }

    public List<String> format(Player player, List<String> lines) {
        MiniMessage miniMessage = getMiniMessage(player);
        List<String> list = new ArrayList<>();
        for (String line : lines) {
            Component component = MiniMessageConvert.miniMessage(line);
            list.add(miniMessage.serialize(component));
        }
        return list;
    }

    public static MenuDraftConfig inst() {
        return instanceOf(MenuDraftConfig.class);
    }

    public class Gui extends AbstractDraftGui {
        public Gui(SweetMail plugin, Player player) {
            super(plugin, player);
            checkDraft();
        }

        public void checkDraft() {
            int outdateHours = getDraftOutdateHours(player);
            long now = Util.toTimestamp(LocalDateTime.now());
            if (outdateHours > 0 && !player.hasPermission("sweetmail.draft.bypass.outdate")) {
                long outdateTime = outdateHours * 3600L * 1000L;
                if (draft.lastEditTime != null) {
                    long last = draft.lastEditTime;
                    if (now > last + outdateTime) {
                        LocalDateTime time = Util.fromTimestamp(last);
                        info("玩家 " + player.getName() + " 的草稿已过期重置");
                        t(player, plugin.prefix() + Messages.Draft.outdate_tips.str(
                                Pair.of("%time%", plugin.text().toStringTips(time))));
                        draft.reset();
                    }
                }
                LocalDateTime time = Util.fromTimestamp(now + outdateTime);
                t(player, plugin.prefix() + Messages.Draft.open_tips.str(
                        Pair.of("%hours%", outdateHours),
                        Pair.of("%time%", plugin.text().toStringTips(time))));
            }
            // TODO: 将启用 PAPI 变量选项加到高级设置
            draft.advPlaceholders = player.hasPermission("sweetmail.admin");
            draft.lastEditTime = now;
            draft.save();
        }

        @Override
        public Inventory newInventory() {
            created = createInventory(this, player);
            applyIcons(this, created, player);
            return created;
        }

        @Override
        @SuppressWarnings({"deprecation"})
        public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
            Character c = getSlotKey(slot);
            if (c == null) return;
            event.setCancelled(true);

            switch (String.valueOf(c)) {
                case "接": {
                    if (click.isLeftClick() && !click.isShiftClick()) {
                        player.closeInventory();
                        ChatPrompter.prompt(
                                plugin, player,
                                iconReceiverPromptTips,
                                iconReceiverPromptCancel,
                                receiver -> {
                                    if (testUsername(receiver)) {
                                        OfflinePlayer offline = Util.getOfflinePlayer(receiver).orElse(null);
                                        String id = plugin.getPlayerKey(offline);
                                        if (id == null) {
                                            t(player, plugin.prefix() + Messages.Draft.online__no_player.str());
                                            reopen.run();
                                            return;
                                        }
                                        draft.receiver = id;
                                    } else {
                                        t(player, plugin.prefix() + Messages.Draft.online__no_player.str());
                                    }
                                    draft.save();
                                    reopen.run();
                                }, reopen
                        );
                    }
                    return;
                }
                case "图": {
                    if (click.isLeftClick() && !click.isShiftClick()) {
                        String title = player.hasPermission("sweetmail.icon.custom") ? iconIconTitleCustom : iconIconTitle;
                        plugin.getGuiManager().openGui(new GuiIcon(plugin, player, title));
                    }
                    return;
                }
                case "题": {
                    if (click.isLeftClick() && !click.isShiftClick()) {
                        player.closeInventory();
                        ChatPrompter.prompt(
                                plugin, player,
                                iconTitlePromptTips,
                                iconTitlePromptCancel,
                                title -> {
                                    draft.title = format(player, title);
                                    draft.save();
                                    reopen.run();
                                }, reopen
                        );
                    }
                    return;
                }
                case "文": {
                    if (!click.isShiftClick()) {
                        if (click.isLeftClick()) {
                            ItemMeta rawMeta = cursor != null ? cursor.getItemMeta() : null;
                            if (rawMeta instanceof BookMeta) {
                                BookMeta meta = (BookMeta) rawMeta;
                                draft.content = format(player, meta.getPages());
                                draft.save();
                                applyIcon(this, view, player, slot);
                                Util.updateInventory(player);
                            } else {
                                Messages.Draft.cursor_no_book.tm(player);
                            }
                        }
                        if (click.isRightClick()) {
                            plugin.getBookImpl().openBook(player, draft);
                        }
                    }
                    return;
                }
                case "高": {
                    if (click.isLeftClick() && !click.isShiftClick()) {
                        if (player.hasPermission(PERM_ADMIN)) {
                            MenuDraftAdvanceConfig.inst()
                                    .new Gui(plugin, player)
                                    .open();
                        }
                    }
                    return;
                }
                case "重": {
                    if (click.isLeftClick() && !click.isShiftClick()) {
                        info("玩家 " + player.getName() + " 手动重置了草稿");
                        List<IAttachment> old = draft.attachments;
                        draft.reset();
                        draft.save();
                        for (IAttachment attachment : old) {
                            if (attachment.isLegal() && attachment.canGiveBack(player)) {
                                attachment.use(player);
                            }
                        }
                        reopen.run();
                    }
                    return;
                }
                case "发": {
                    if (click.isLeftClick() && !click.isShiftClick()) {
                        double price = getPrice(player);
                        if (!plugin.economy().has(player, price)) {
                            t(player, plugin.prefix() + Messages.Draft.no_money.str().replace("%price%", String.format(Messages.Draft.money_format.str(), price)));
                            return;
                        }
                        if (!canSendToYourself && draft.sender.equalsIgnoreCase(draft.receiver)) {
                            t(player, plugin.prefix() + Messages.Draft.cant_send_to_yourself.str());
                            return;
                        }
                        // 提醒发送人，计算泛接收人列表的时间可能会很长
                        if (draft.advReceivers != null && draft.advReceivers.startsWith("last ")) {
                            t(player, plugin.prefix() + Messages.Draft.send_with_adv_receivers.str());
                        }
                        player.closeInventory();
                        plugin.getScheduler().runTaskAsync(() -> {
                            List<String> receivers = DraftManager.inst().generateReceivers(draft);
                            if (!canSendToYourself) receivers.remove(player.getName());
                            if (receivers.isEmpty()) {
                                t(player, plugin.prefix() + Messages.Draft.no_receivers.str());
                                return;
                            }
                            if (!plugin.economy().takeMoney(player, price)) {
                                t(player, plugin.prefix() + Messages.Draft.no_money.str().replace("%price%", String.format(Messages.Draft.money_format.str(), price)));
                                return;
                            }
                            String uuid = plugin.getMailDatabase().generateMailUUID();
                            if (draft.outdateDays == 0) {
                                draft.outdateDays = getOutdateDays(player);
                            }
                            Mail mail = draft.createMail(uuid, receivers);
                            plugin.getMailDatabase().sendMail(mail);
                            if (draft.advSenderDisplay == null) plugin.getScheduler().runTask(() -> {
                                PlayerMailSentEvent e = new PlayerMailSentEvent(player, draft.deepClone(), mail);
                                Bukkit.getPluginManager().callEvent(e);
                            });
                            draft.reset();
                            draft.save();
                            t(player, plugin.prefix() + Messages.Draft.sent.str());
                        });
                    }
                    return;
                }
                case "附": {
                    if (click.isLeftClick()) {
                        boolean hasCursorItem = cursor != null && !cursor.getType().equals(Material.AIR);
                        int i = getKeyIndex(c, slot);
                        if (i < draft.attachments.size()) {
                            if (!hasCursorItem) {
                                IAttachment attachment = draft.attachments.remove(i);
                                draft.save();
                                updateAttachmentSlots(view);
                                if (!player.hasPermission(PERM_ADMIN) || !click.isShiftClick()) {
                                    if (attachment != null) plugin.getScheduler().runTask(() -> {
                                        if (attachment.isLegal()) {
                                            if (attachment.canGiveBack(player)) {
                                                attachment.use(player);
                                            }
                                        } else {
                                            IAttachment.Internal.useIllegalDeny(player);
                                        }
                                    });
                                }
                            }
                        } else if (!click.isShiftClick() && player.hasPermission(AttachmentItem.PERM)) {
                            // 快速添加物品附件
                            if (hasCursorItem) {
                                IAttachment attachment = AttachmentItem.build(cursor);
                                if (!attachment.isLegal()) {
                                    t(player, plugin.prefix() + Messages.Draft.attachments__item__banned.str());
                                    return;
                                }
                                event.setCursor(null);
                                draft.attachments.add(attachment);
                                draft.save();
                                updateAttachmentSlots(view);
                                return;
                            }
                            MenuAddAttachmentConfig.inst().new Gui(plugin, player).open();
                        }
                    }
                    return;
                }
                default: {
                    handleClick(player, click, c);
                }
            }
        }

        private void updateAttachmentSlots(InventoryView view) {
            for (int k = 0; k < inventory.length; k++) {
                if (inventory[k] == '附') {
                    applyIcon(this, view, player, k);
                }
            }
            Util.updateInventory(player);
        }
    }
}
