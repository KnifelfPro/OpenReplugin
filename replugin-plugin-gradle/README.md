# OpenReplugin Plugin Gradle

OpenReplugin Plugin Gradle 是一个 Gradle 插件，由 **插件** 负责引入。

该 Gradle 插件主要负责在插件的编译期中做一些事情，是“动态编译方案”的主要实现者。此外，开发者可通过修改其属性而做一些自定义的操作。

大致包括：

* 动态修改主要调用代码，改为调用 OpenReplugin Plugin Gradle（如 Activity 的继承、Provider 的重定向等）

开发者需要依赖此 Gradle 插件，以实现对 OpenReplugin 的接入。请参见 [docs/使用文档.md](../docs/使用文档.md) 以了解接入方法。
