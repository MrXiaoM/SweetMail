# SweetMail 配套资源包

这个分支将用于分发 SweetMail 的配套资源包与资源包配套配置。

资源包仅支持 `1.16+` 的 `Paper` 及其衍生服务端。

资源包位于 `resourcepack` 文件夹，插件配置位于 `SweetMail` 文件夹

资源包设计目标为原版使用，如果你使用 ItemsAdder、Oraxen 等资源包管理插件，请翻到“效果展示”后面。

资源包需要占用从 `8731` 开始递增的 CustomModelData 数值，选了个这么偏僻的数值，应该不会有其它包跟本插件冲突。  
如果和你的服务器现有物品存在冲突，请自行修改配置解决。

> 这个分支的插件配置**不完整**，仅包含需要**修改、添加  **的条目，请勿直接覆盖文件！

## 下载

服务器管理者
+ [Github](https://github.com/MrXiaoM/SweetMail/archive/refs/heads/resourcepacks.zip)
+ [moeyy 镜像](https://github.moeyy.xyz/https://github.com/MrXiaoM/SweetMail/archive/resourcepacks.zip)
+ [ghproxy 镜像](https://ghproxy.net/https://github.com/MrXiaoM/SweetMail/archive/resourcepacks.zip)

开发者
```shell
git clone https://github.com/MrXiaoM/SweetMail.git SweetMail.resourcepack
cd SweetMail.resourcepack
git branch resourcepack
```

## 效果展示

草稿界面

![](https://pic1.imgdb.cn/item/67c59a63d0e0a243d40add4a.png)

## ItemsAdder

根据[文档](https://itemsadder.devs.beer/plugin-usage/merge-resourcepacks)所说，有以下两种方法，任选一种即可

### 第一种方法
将 `resourcepack` 文件夹里面的内容，复制到 `plugins/ItemsAdder/contents/sweetmail/resourcepack/` 文件夹即可。

### 第二种方法
将 `resourcepack` 文件夹里面的内容，复制到 `plugins/SweetMail/resourcepack/` 文件夹，然后编辑 ItemsAdder 的 `config.yml`
```yaml
# 3.6.4 版本是在这里，其它版本可能有变动，详见 ItemsAdder 文档
resource-pack:
  zip:
    merge_other_plugins_resourcepacks_folders:
    # 在这里添加一句这个
    - 'SweetMail/resourcepack'
```

## Oraxen

根据[文档](https://docs.oraxen.com/faq#oraxen-is-using-its-own-resource-pack-can-i-still-use-mine)所说，将 `resourcepack/assets` 文件夹里面的内容，复制到 `plugins/Oraxen/pack/assets/` 文件夹即可。
