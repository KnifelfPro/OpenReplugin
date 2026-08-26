<p align="center">
  <img alt="OpenReplugin Logo" src="https://github.com/Qihoo360/RePlugin/wiki/img/RePlugin.png" width="400"/>
</p>

[![license](http://img.shields.io/badge/license-Apache2.0-brightgreen.svg?style=flat)](./LICENSE)

[![Release Version](https://img.shields.io/badge/release-3.2.0-brightgreen.svg)](https://github.com/KnifelfPro/RePlugin/releases)

## OpenReplugin —— 历经多年考验、数亿设备使用的稳定占坑类插件化方案

OpenReplugin 基于原 RePlugin 持续维护，是一套完整的、稳定的、适合全面使用的占坑类插件化方案，也是业内首个提出”全面插件化“（全面特性、全面兼容、全面使用）的方案。

其主要优势有：
* **极其灵活**：主程序无需升级（无需在Manifest中预埋组件），即可支持新增的四大组件，甚至全新的插件
* **非常稳定**：Hook点**仅有一处（ClassLoader），无任何Binder Hook**！如此可做到其**崩溃率仅为“万分之一”，并完美兼容市面上近乎所有的Android ROM**
* **特性丰富**：支持近乎所有在“单品”开发时的特性。**包括静态Receiver、Task-Affinity坑位、自定义Theme、进程坑位、AppCompat、DataBinding、Jetpack Compose等**
* **易于集成**：无论插件还是主程序，**只需“数行”就能完成接入**
* **管理成熟**：拥有成熟稳定的“插件管理方案”，支持插件安装、升级、卸载、版本管理，甚至包括进程通讯、协议版本、安全校验等
* **久经考验**：**数亿**用户量级的生产环境验证，崩溃率低至**万分之一（0.01%）**，确保App用到的方案是最稳定、最适合使用的

### 我们还支持以下特性

| 特性 | 描述 |
|:-------------:|:-------------:|
| 组件 | **四大组件（含静态Receiver）** |
| 升级无需改主程序Manifest | **完美支持** |
| Android特性 | **支持近乎所有（包括SO库等）** |
| TaskAffinity & 多进程 | **支持（*坑位方案*）** |
| 插件类型 | **支持自带插件（*自识别*）、外置插件** |
| 插件间耦合 | **支持Binder、Class Loader、资源等** |
| 进程间通讯 | **支持同步、异步、Binder、广播等** |
| 自定义Theme & AppComat | **支持** |
| DataBinding | **支持** |
| Jetpack Compose | **支持**（插件 `minSdk` 23+） |
| 安全校验 | **支持** |
| 资源方案 | **独立资源 + Context传递（相对稳定）** |
| Android 版本 | **API Level 19 – 37（Android 4.4 至 Android 17）** |

## 愿景

让插件化能**飞入寻常应用家**，做到稳定、灵活、自由，大小项目兼用。

## 最新特性

**3.2.0** 已发布到 Maven Central。支持 API 19–37（Android 4.4 至 Android 17），宿主与插件应用代码可用 Java 17，构建工具为 Gradle 9 / Android Gradle Plugin 9。本版增加 Jetpack Compose 插件、转发插件 Application 生命周期、隔离插件存储，并加固安装路径和静态 Receiver 注册。

## OpenReplugin 架构图

<p align="center">
  <img alt="OpenReplugin Framework" src="https://github.com/Qihoo360/RePlugin/wiki/img/RePluginFramePic.jpeg" height="600" />
</p>

以典型宿主应用为例：

* **系统层——Android**：为Android Framework层。**只有ClassLoader是Hook的**，而AMS、Resources等都没有做Hook，确保了其稳定性。
* **框架层——OpenReplugin 框架**：OpenReplugin 框架层，**只有 `RePlugin` API 是对“上层完全公开”的**，其余均为Internal，或“动态编译方案”生效后的调用，对开发者而言是“无需关心”的。
* **插件层——各插件**：“标蓝部分”是各插件，包括大部分的业务插件（如体检、清理、桌面插件等）。而其中“标黄部分”是支撑一个应用的各种基础插件，如WebView、Download、Share，甚至Protobuf都能成为基础插件。

## 使用方法

当前版本 **3.2.0** 已发布到 Maven Central（坐标 `io.github.knifelfpro`）。接入步骤、宿主/插件配置和常用 API 见 **[docs/使用文档.md](./docs/使用文档.md)**。

```gradle
pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}

plugins {
    id 'io.github.knifelfpro.replugin-host-gradle' version '3.2.0' apply false
    id 'io.github.knifelfpro.replugin-plugin-gradle' version '3.2.0' apply false
}

dependencies {
    implementation 'io.github.knifelfpro:replugin-host-lib:3.2.0'    // 宿主
    implementation 'io.github.knifelfpro:replugin-plugin-lib:3.2.0'  // 插件
}
```

| 用途 | 坐标 | Gradle Plugin ID |
|---|---|---|
| 宿主 Gradle 插件 | `io.github.knifelfpro:replugin-host-gradle:3.2.0` | `io.github.knifelfpro.replugin-host-gradle` |
| 宿主 Library | `io.github.knifelfpro:replugin-host-lib:3.2.0` | — |
| 插件 Gradle 插件 | `io.github.knifelfpro:replugin-plugin-gradle:3.2.0` | `io.github.knifelfpro.replugin-plugin-gradle` |
| 插件 Library | `io.github.knifelfpro:replugin-plugin-lib:3.2.0` | — |

大部分情况下和“单品”开发无异，也可直接参考 [sample](./sample) 示例工程。


## 已接入 OpenReplugin 的插件

目前已有的插件，可以分为以下几类，供各App开发者参考：
* **展示插件**：如**卫士首页**（是的，你没看错）、体检、信息流等
* **业务插件**：如清理、骚扰拦截、悬浮窗等
* **合作插件**：如程序锁、免费WiFi、安全桌面等
* **后台插件**：如Push、服务管理、Protobuf等
* **基础插件**：如安全WebView、分享、定位等

截止2017年6月底，这样的插件，我们有**103**个。衷心希望您能成为这个数字中的新的一员！

## 贡献自己的力量

我们欢迎任何形式的贡献，并致以诚挚的感谢！

你可以贡献代码、提出问题、编写文档等。有关“贡献”相关的内容，请阅读 **[CONTRIBUTING.md](./CONTRIBUTING.md)**。

## License

OpenReplugin is [Apache v2.0 licensed](./LICENSE).
