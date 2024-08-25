# SweetMail

便于玩家使用的图形化邮件系统，支持邮件携带物品等附件，支持 BC 跨服通知，支持跨服务器查看、发送邮件。

## 简介

我找了一堆免费的、开源的、付费的，支持物品附件的邮件插件，效果都不尽人意。  
又不想用全球市场插件自带的邮件系统，功能太少了。  
于是我计划编写一个对玩家使用友好，功能相对强大的邮件插件。

## 版本特性

本插件为高版本 Minecraft 打造，更低版本会尽可能地去兼容，以下为各 Minecraft 版本之间区别
+ `1.13` 起进行扁平化，弃用 数字ID 和 Data值 (俗称 子ID)。如果需要使用旧版本的 子ID 特性，请在 ID 后面加冒号`:`，如 `WOOL:15`。本插件不再支持 数字ID，请使用 英文ID。
+ `1.14` 起加入 CustomModelData。这意味着更旧的版本无法设置 CustomModelData 选项。
+ `1.19.3` 起 Bukkit API 加入 `Translatable` 接口，可以让插件获取到物品的翻译键，使得物品原名可正常显示。这意味着更旧的版本将无法在邮件详细信息查看附件物品中文名。这个问题可以通过外部引入语言文件解决，低版本兼容正在编写中。

## 未完成

目前还有少量功能就写好可以正式发布了，敬请期待。

+ [ ] 草稿高级设置 (用于管理员发送系统邮件)
+ [ ] 第三方附属插件注册附件类型
+ [ ] 添加附件菜单
+ [ ] 材质包界面
+ [ ] 针对非 Paper 服务端的功能 fallback
+ [ ] 在线模式 使用UUID而非玩家名来识别玩家

若想尝鲜，可翻到本文末尾自行尝试编译插件使用。

## 草稿

任何人都可以拥有且仅拥有一个草稿，使用 `/mail draft` 创建或编辑草稿。

邮件的 `收件人`、`标题`、`内容`、`附件` 等均在草稿界面中点击编辑。

管理员在发送`系统邮件`时，还可以设置`发件人显示名称`，以及设置 `泛收件人`，比如
+ 7天内上过线的玩家
+ 当前服务所有在线玩家
+ 通过代理端获取的全服所有在线玩家

等等…

目前草稿不支持多服同步，如果部署在多个服区，每个玩家将在每个服区有不同的一个草稿。

## 发件箱/收件箱

玩家拥有自己的发件箱和收件箱，时间从新到旧排序，可翻页。

收件箱可查看未读邮件列表，可一键领取未领取附件的邮件。

管理员可以使用命令查看他人的发件箱和收件箱。

## 界面配置

所有的界面和文字均可自定义！界面布局使用可高度自定义的配置格式

图标配置的完整示例详见 [menus/draft.yml](https://github.com/MrXiaoM/SweetMail/blob/main/src/main/resources/menus/draft.yml) 末尾

## 命令

标记了 ✅ 的命令代表控制台也可以执行，反之只有玩家可以执行。  
`<>` 代表必选参数，`[]` 代表可选参数。
根命令为 `/sweetmail`，可简写为 `/mail` 或 `/sm`

|   | 命令                                    | 描述                    | 权限                    |
|---|---------------------------------------|-----------------------|-----------------------|
|   | 玩家命令                                  |                       |                       |
|   | `/mail draft`                         | 打开草稿界面                | `sweetmail.draft`     |
|   | `/mail inbox [all/unread]`            | 打开收件箱界面(所有/未读分区，默认未读) | `sweetmail.box`       |
|   | `/mail outbox`                        | 打开收件箱界面               | `sweetmail.box`       |
|   | 管理员命令                                 |                       |                       |
| ✅ | `/mail inbox <all/unread> <玩家>`       | 为某人打开收件箱界面            | `sweetmail.box.other` |
| ✅ | `/mail outbox <玩家>`                   | 为某人打开发件箱界面            | `sweetmail.box.other` |
|   | `/mail admin inbox <all/unread> <玩家>` | 打开某人的收件箱界面            | `sweetmail.box.other` |
|   | `/mail admin outbox <玩家>`             | 打开某人的发件箱界面            | `sweetmail.box.other` |
| ✅ | `/mail reload database`               | 重载并重新连接数据库            | `sweetmail.admin`     |
| ✅ | `/mail reload`                        | 重载配置文件，不重连数据库         | `sweetmail.admin`     |

## 权限

+ `sweetmail.admin` 邮件管理员权限，可在草稿打开高级设置发送系统邮件等
+ `sweetmail.icon.<图标>` 允许使用某个预设图标 (配置文件的 preset-icons)
+ `sweetmail.icon.custom` 允许使用自定义图标 (在选择图标界面使用物品栏里的物品)
+ `sweetmail.price.<价格组>` 设定玩家在哪个价格组内，详见配置文件 price 的注释
+ `sweetmail.notice` 玩家是否在收到新邮件时、上线时接收新邮件通知

## 开发者

**使用接口发送系统邮件**

[![版本号](https://img.shields.io/github/v/release/MrXiaoM/SweetMail
)](https://github.com/MrXiaoM/SweetMail/releases)
```kotlin
repositories {
    maven("https://jitpack.io")
}
dependencies {
    compileOnly("com.github.MrXiaoM:SweetMail:$VERSION")
}
```

```java
import top.mrxiaom.sweetmail.IMail;
// 使用示例
void foo() {
    IMail.Status status = IMail.api()
            .createSystemMail("系统消息")
            .setIcon("BOOK") // 设置图标，详见源码注释
            .setTitle("邮件标题")
            .addContent("邮件正文内容", "列表中每个元素代表每页。换行依旧用\n")
            .addAttachments( // 添加附件
                AttachmentItem.build(new ItemStack(Material.DIAMOND))
            )
            .send();
    if (status.ok()) {
        info("邮件发送成功");
    }
}
```

**构建插件**

请使用 `java 17` 执行以下命令。别担心，构建产物的目标版本是 `java 8`。

```shell
./gradlew clean build
```
