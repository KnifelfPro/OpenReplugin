<p align="center">
  <img alt="OpenReplugin Logo" src="https://github.com/Qihoo360/RePlugin/wiki/img/RePlugin.png" width="400"/>
</p>

[![license](http://img.shields.io/badge/license-Apache2.0-brightgreen.svg?style=flat)](./LICENSE)

[![Release Version](https://img.shields.io/badge/release-3.2.0-brightgreen.svg)](https://github.com/KnifelfPro/RePlugin/releases)

## 使用文档

**3.2.0** 已发布到 Maven Central，坐标为 `io.github.knifelfpro`。完整接入步骤见 **[docs/使用文档.md](./docs/使用文档.md)**。

```gradle
pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}

plugins {
    id 'io.github.knifelfpro.replugin-host-gradle' version '3.2.0' apply false
    id 'io.github.knifelfpro.replugin-plugin-gradle' version '3.2.0' apply false
}

dependencies {
    implementation 'io.github.knifelfpro:replugin-host-lib:3.2.0'    // host
    implementation 'io.github.knifelfpro:replugin-plugin-lib:3.2.0'  // plugin
}
```

| 用途 | 坐标 | Gradle Plugin ID |
|---|---|---|
| 宿主 Gradle 插件 | `io.github.knifelfpro:replugin-host-gradle:3.2.0` | `io.github.knifelfpro.replugin-host-gradle` |
| 宿主 Library | `io.github.knifelfpro:replugin-host-lib:3.2.0` | — |
| 插件 Gradle 插件 | `io.github.knifelfpro:replugin-plugin-gradle:3.2.0` | `io.github.knifelfpro.replugin-plugin-gradle` |
| 插件 Library | `io.github.knifelfpro:replugin-plugin-lib:3.2.0` | — |

## OpenReplugin —— A flexible, stable, easy-to-use Android Plug-in Framework

OpenReplugin is a complete Android plug-in solution based on the original RePlugin project, suitable for general use.

（[文档，还是中文的好](./README_CN.md)）

It is major strengths are:
* **Extreme flexibility**: Apps do not need to be upgraded to support new components, **even brand new plug-ins**.
* **Extraordinary stability**: With only **ONE** hook (ClassLoader), **NO BINDER HOOK**. OpenReplugin’s Crash ratio is **as low as Ten thousandth (0.01%)**. In addition, OpenReplugin is compatible with almost **ALL Android ROMs** in the market.
* **Rich features**: OpenReplugin supports **almost all features seamlessly as an installed application**, including static Receiver, Task-Affinity, user-defined Theme, AppCompat, DataBinding, Jetpack Compose, etc.
* **Easy integration**: It takes only couple lines to access, whether plug-ins or main programs. 
* **Mature management**:　OpenReplugin owns stable plug-in management solution which supports installation, upgrade, uninstallation and version management. Process communication, protocol versions and security check are also included. 
* **Battle-tested at scale**: OpenReplugin has powered hundreds of millions of installs in production for years, with a crash ratio **as low as 0.01%**.

### We support:

| Feature | Description |
|:-------------:|:-------------:|
| Components | **Activity, Service, Provider, Receiver(Including static)** |
| Not need to upgrade when brand a new Plug-in | **Supported** |
| Android Feature | **Supported almost all features** |
| TaskAffinity & Multi-Process | **Perfect supported!** |
| Support Plug-in Type | **Built-in (Only Two Step) and External(Download)** |
| Plug-in Coupling | **Binder, Class Loader, Resources, etc.** |
| Interprocess communication | **Sync, Async, Binder and Cross-plug-in broadcast** |
| User-Defined Theme & AppComat | **Supported** |
| DataBinding | **Supported** |
| Jetpack Compose | **Supported** (plugin `minSdk` 23+) |
| Safety check when installed | **Supported** |
| Resources Solution | **Independent Resources + Context pass(No Adaptation ROM)** |
| Android Version | **API Level 19 – 37 (Android 4.4 through Android 17)** |

## Our Vision
Make OpenReplugin be used in all kinds of ordinary Apps; and provide stable, flexible, liberal plug-ins which adopt for both large and small projects.

## Latest features

**3.2.0** targets API 19–37 (Android 4.4 through Android 17), lets host and plugin app code use Java 17, and builds with Gradle 9 / Android Gradle Plugin 9. This release adds Jetpack Compose plugins, forwards plugin Application lifecycle, isolates plugin storage, and hardens install paths and static Receiver registration.

## OpenReplugin Architecture

<p align="center">
  <img alt="OpenReplugin Framework" src="https://github.com/Qihoo360/RePlugin/wiki/img/RePluginFramePic.jpeg" height="600" />
</p>

## How to Use OpenReplugin
Using OpenReplugin is very simple. Under most conditions, using it is no different than developing an App.

See **[docs/使用文档.md](./docs/使用文档.md)** for the full guide, or check the **[sample projects](./sample)** for concrete usage.

## Plug-ins Accessed in OpenReplugin

For your reference, plug-ins accessed can be classified into following categories: 

* **Expo plug-ins**: Safe Home Page, physical examination, information flow, etc. 
* **Business plug-ins**: cleaning, disturbance intercept, floating window, etc.
* **Cooperation plug-ins**: App Lock, free Wi-Fi, security desktop, etc.
* **Background plug-ins**: Push, service management, Protobuf, etc.
* **Base plug-ins**: Security WebView, share, location service, etc.

By the end of June 2017, we already have 102 plug-ins like these. We look forward to you becoming a part of OpenReplugin family!

## Contribute Your Share
We sincerely welcome and appreciate your contribution of any kind. You can submit code, raise suggestions, write documentation, etc. See **[CONTRIBUTING.md](./CONTRIBUTING.md)** for more information.


## License

OpenReplugin is [Apache v2.0 licensed](./LICENSE).

(Thanks Xiezihan（谢子晗） for providing the translations.)
