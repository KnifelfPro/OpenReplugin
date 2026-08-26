# OpenReplugin Host Gradle

OpenReplugin Host Gradle 是一个 Gradle 插件，由 **主程序** 负责引入。

该 Gradle 插件主要负责在主程序的编译期中做一些事情，此外，开发者可通过修改其属性而做一些自定义的操作。

大致包括：

* 生成带 OpenReplugin 插件坑位的 AndroidManifest.xml（允许自定义数量）
* 生成 HostBuildConfig 类，方便插件框架读取并自定义其属性

开发者需要依赖此 Gradle 插件，以实现对 OpenReplugin 的接入。请参见 [docs/使用文档.md](../docs/使用文档.md) 以了解接入方法。
