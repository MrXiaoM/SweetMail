# SweetMail

便于玩家使用的图形化邮件系统，支持邮件携带物品等附件，支持 BC 跨服通知，支持跨服务器查看、发送邮件。

> 插件正在编写中，此页面为插件预估实现的效果

## 简介

我找了一堆免费的、开源的、付费的，支持物品附件的邮件插件，效果都不尽人意。  
又不想用全球市场插件自带的邮件系统，功能太少了。  
于是我计划编写一个对玩家使用友好，功能相对强大的邮件插件。

## 草稿

任何人都可以拥有且仅拥有一个草稿，使用 `/mail draft` 创建或编辑草稿。

邮件的 `收件人`、`标题`、`内容`、`附件` 等均在草稿界面中点击编辑。

管理员在发送`系统邮件`时，还可以设置`发件人显示名称`，以及设置 `泛收件人`，比如
+ 7天内上过线的玩家
+ 当前服务所有在线玩家
+ 通过代理端获取的全服所有在线玩家

等等…

## 发件箱/收件箱

玩家拥有自己的发件箱和收件箱，时间从新到旧排序，可翻页。

收件箱可查看未读邮件列表，可一键领取未领取附件的邮件。

管理员可以使用命令查看他人的发件箱和收件箱。

## 界面配置

所有的界面和文字均可自定义！界面布局使用类似以下配置格式，可高度自定义

```yaml
# 界面布局，每行9个图标，不能超过6行
inventory:
  - '黑黑黑黑黑黑黑黑黑'
  - '黑接图题文高重发黑'
  - '黑黑黑黑黑黑黑黑黑'
  - '黑附附附附附附附黑'
  - '黑黑黑黑黑黑黑黑黑'
# 必选物品
items:
  接:
    # TODO
other-items:
  黑:
    material: 'BLACK_STAINED_GLASS_PANE#10000'
    display: '&0'
```

# 开发者

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
    boolean success = IMail.api()
            .createSystemMail("系统消息")
            .setIcon("BOOK") // 设置图标，详见源码注释
            .setTitle("标题")
            .addContent("邮件内容")
            .addAttachments() // 添加附件
            .send();
}
```
