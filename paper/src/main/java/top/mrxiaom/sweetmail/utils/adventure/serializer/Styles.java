package top.mrxiaom.sweetmail.utils.adventure.serializer;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Entity;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.awt.*;
import java.util.UUID;
import java.util.function.BiConsumer;

import static top.mrxiaom.sweetmail.utils.adventure.serializer.BungeeComponentSerializer.isModernHover;

@SuppressWarnings({"deprecation"})
public enum Styles {
    FONT((bc, c) -> {
        Key font = c.font();
        if (font != null) {
            bc.setFont(font.asString());
        }
    }),
    COLOR((bc, c) -> {
        TextColor color = c.color();
        if (color != null) {
            if (color.equals(NamedTextColor.AQUA)) bc.setColor(ChatColor.AQUA);
            else if (color.equals(NamedTextColor.BLACK)) bc.setColor(ChatColor.BLACK);
            else if (color.equals(NamedTextColor.DARK_BLUE)) bc.setColor(ChatColor.DARK_BLUE);
            else if (color.equals(NamedTextColor.DARK_GREEN)) bc.setColor(ChatColor.DARK_GREEN);
            else if (color.equals(NamedTextColor.DARK_AQUA)) bc.setColor(ChatColor.DARK_AQUA);
            else if (color.equals(NamedTextColor.DARK_RED)) bc.setColor(ChatColor.DARK_RED);
            else if (color.equals(NamedTextColor.DARK_PURPLE)) bc.setColor(ChatColor.DARK_PURPLE);
            else if (color.equals(NamedTextColor.GOLD)) bc.setColor(ChatColor.GOLD);
            else if (color.equals(NamedTextColor.GRAY)) bc.setColor(ChatColor.GRAY);
            else if (color.equals(NamedTextColor.DARK_GRAY)) bc.setColor(ChatColor.DARK_GRAY);
            else if (color.equals(NamedTextColor.BLUE)) bc.setColor(ChatColor.BLUE);
            else if (color.equals(NamedTextColor.AQUA)) bc.setColor(ChatColor.AQUA);
            else if (color.equals(NamedTextColor.RED)) bc.setColor(ChatColor.RED);
            else if (color.equals(NamedTextColor.LIGHT_PURPLE)) bc.setColor(ChatColor.LIGHT_PURPLE);
            else if (color.equals(NamedTextColor.YELLOW)) bc.setColor(ChatColor.YELLOW);
            else if (color.equals(NamedTextColor.WHITE)) bc.setColor(ChatColor.WHITE);
            else bc.setColor(ChatColor.of(new Color(color.value())));
        }
    }),
    DECORATIONS((bc, c) -> {
        c.decorations().forEach(((textDecoration, state) -> {
            Boolean value;
            switch (state) {
                case TRUE: value = true; break;
                case FALSE: value = false; break;
                default: value = null; break;
            }
            switch (textDecoration.name().toLowerCase()) {
                case "bold":
                    bc.setBold(value);
                    break;
                case "italic":
                    bc.setItalic(value);
                    break;
                case "obfuscated":
                    bc.setObfuscated(value);
                    break;
                case "underlined":
                    bc.setUnderlined(value);
                    break;
                case "strikethrough":
                    bc.setStrikethrough(value);
                    break;
            }
        }));
    }),
    CLICK_EVENT((bc, c) -> {
        ClickEvent event = c.clickEvent();
        if (event != null) {
            net.md_5.bungee.api.chat.ClickEvent.Action action;
            switch (event.action().toString()) {
                case "open_url":
                    action = net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL;
                    break;
                case "open_file":
                    action = net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_FILE;
                    break;
                case "change_page":
                    action = net.md_5.bungee.api.chat.ClickEvent.Action.CHANGE_PAGE;
                    break;
                case "run_command":
                    action = net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND;
                    break;
                case "suggest_command":
                    action = net.md_5.bungee.api.chat.ClickEvent.Action.SUGGEST_COMMAND;
                    break;
                case "copy_to_clipboard":
                    action = net.md_5.bungee.api.chat.ClickEvent.Action.COPY_TO_CLIPBOARD;
                    break;
                case "custom":
                    action = net.md_5.bungee.api.chat.ClickEvent.Action.CUSTOM;
                    break;
                default:
                    return;
            }
            String value = event.value();
            bc.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(action, value));
        }
    }),
    HOVER_EVENT((bc, c) -> {
        HoverEvent<?> event = c.hoverEvent();
        if (event != null) {
            HoverEvent.Action<?> action = event.action();
            if (isModernHover) {
                // 1.13+ 支持自定义 Content
                if (action.equals(HoverEvent.Action.SHOW_TEXT)) {
                    Component value = (Component) event.value();
                    Text text = new Text(BungeeComponentSerializer.serialize(value));
                    bc.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, text));
                } else if (action.equals(HoverEvent.Action.SHOW_ENTITY)) {
                    HoverEvent.ShowEntity value = (HoverEvent.ShowEntity) event.value();
                    Key type = value.type();
                    UUID uuid = value.id();
                    Component name = value.name();
                    Entity entity = new Entity(type.asString(), uuid.toString(), name == null ? null : BungeeComponentSerializer.serialize(name));
                    bc.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_ENTITY, entity));
                } else if (action.equals(HoverEvent.Action.SHOW_ITEM)) {
                    HoverEvent.ShowItem value = (HoverEvent.ShowItem) event.value();
                    String type = value.item().asString();
                    int count = value.count();
                    String nbt = value.nbt() == null ? "{}" : value.nbt().string();
                    Item item = new Item(type, count, ItemTag.ofNbt(nbt));
                    bc.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_ITEM, item));
                }
            } else {
                // 1.12 及以下只能使用 BaseComponent[] 作为值
                if (action.equals(HoverEvent.Action.SHOW_TEXT) && event.value() instanceof Component) {
                    BaseComponent[] components = new BaseComponent[] { BungeeComponentSerializer.serialize((Component) event.value()) };
                    bc.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, components));
                }
                if (action.equals(HoverEvent.Action.SHOW_ITEM) && event.value() instanceof HoverEvent.ShowItem) {
                    HoverEvent.ShowItem showItem = (HoverEvent.ShowItem) event.value();
                    String tag = showItem.nbt() == null ? "{}" : showItem.nbt().string();
                    String id = showItem.item().asString();
                    int count = showItem.count();
                    String nbt = "{id:\"" + id + "\",Count:" + count + "b,tag:" + tag + "}";
                    BaseComponent[] components = new TextComponent[] { new TextComponent(nbt) };
                    bc.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_ITEM, components));
                }
            }
        }
    }),
    INSERTION((bc, c) -> {
        String insertion = c.insertion();
        if (insertion != null) {
            bc.setInsertion(insertion);
        }
    }),
    SHADOW((bc, c) -> {
        ShadowColor shadow = c.shadowColor();
        if (shadow != null) {
            bc.setShadowColor(new Color(shadow.value()));
        }
    }),

    ;
    private final BiConsumer<BaseComponent, Component> impl;
    Styles(BiConsumer<BaseComponent, Component> impl) {
        this.impl = impl;
    }

    public void apply(BaseComponent bc, Component c) {
        try {
            impl.accept(bc, c);
        } catch (LinkageError ignored) {
        }
    }
}
