# RePlugin 全仓 Review

- 日期：2026-08-26
- 范围：工作树 `main`（相对 `github/main` ahead 3：`5ed38ab` Java 17、`a155167` Compose、`8d5888b` Application 生命周期 / 存储隔离 / extra dex）
- 模块：`replugin-host-library`、`replugin-plugin-library`、`replugin-host-gradle`、`replugin-plugin-gradle`、`replugin-sample`、`docs/`
- 方法：按 Android review checklist 做静态路径追踪。仓库无框架单元测试；修复后用 adb 模拟器做宿主/插件冒烟。
- 原则：只记有代码路径证据的问题。风格偏好不单独成项。

严重度：`P0` 数据丢失/安全/发版阻断；`P1` 崩溃或大面积回归；`P2` 有界正确性/泄漏/兼容；`P3` 有明确收益的可维护性。

---

## 摘要

| 级别 | 数量 | 建议 |
|---|---|---|
| P0 | 0 | 调试器 exported 在 `enableDebugger(true)` 时接近任意代码安装，sample release 已关掉 |
| P1 | 6 | 本轮必须修 |
| P2 | 8 | 本轮尽量修 |
| P3 | 5 | 顺手或记债 |

最危险的是：`8d5888b` 把插件默认进程映射到坑位但 sample 未加入白名单；`PluginApplicationClient` 的 `ComponentCallbacks2` 强引用抵消了 WeakReference，插件 ClassLoader 无法回收；SharedPreferences 改名没有迁移，升级后插件配置丢失。

---

## P1

### [P1] 插件 Application 的 ComponentCallbacks2 强引用导致 ClassLoader 无法回收

- Location: `replugin-host-library/replugin-host-lib/src/main/java/com/qihoo360/replugin/component/app/PluginApplicationClient.java:115-117,205-225`
- Impact: 每加载一个插件就 `registerComponentCallbacks` 一次，匿名回调强持有 `PluginApplicationClient` → `Application` → 插件 `ClassLoader`。`sRunningClients` 虽是 `WeakReference`，但永远不会被 GC。插件无法卸载释内存；多次安装/升级会叠加回调。
- Evidence: `getOrCreate` 在 `sRunningClients.put(pn, new WeakReference<>(pacNew))` 之后调用 `hookHostApplication(pacNew)`。`hookHostApplication` 对每个 client 注册新的 `ComponentCallbacks2`，且从不 `unregisterComponentCallbacks`。Activity 生命周期回调已用 `sLifecycleHooked` 只注册一次，内存/配置回调没有同样处理。`RePlugin.App.onLowMemory/onTrimMemory` 在 API ≥ 14 直接 return，API 14+ 只靠这条注册路径转发。
- Suggestion: 与 Activity 生命周期一样，只注册一次 `ComponentCallbacks2`，在回调里遍历 `sRunningClients` 的弱引用。不要捕获单个 `client`。

### [P1] 默认进程映射翻转，sample 未调用 `addMainPkg`

- Location: `replugin-host-library/replugin-host-lib/src/main/java/com/qihoo360/loader2/Loader.java:549-565`；`RePlugin.java:724-733`；`replugin-sample/host/app/src/main/java/com/qihoo360/replugin/sample/host/SampleApplication.java`
- Impact: 框架默认版本是 4（`RePluginConfig.defaultFrameworkVersion = 4`），`adjustPluginProcess` 会对纯 APK 走 `genDynamicProcessMap`。旧逻辑会把插件包名进程从映射表里删掉（留在宿主 UI 进程）。`8d5888b` 改为默认映射到 `:p0`，只有 `RePlugin.addMainPkg` 白名单才留在 UI。sample 从未调用该 API。demo1 / compose / webview 的默认 Activity 会进插件坑位进程，和宿主 Application、静态状态、部分 WebView/Compose 场景不一致。
- Evidence: 旧代码在收集组件进程后 `processSet.remove(pluginUIProcess)`。新代码先 `processSet.add(pluginUIProcess)`，仅当 `isKeepInUiProcess` 才 remove。`addMainPkg` 注释写明「请在插件加载前调用」，sample 的 `createConfig` / `attachBaseContext` 都没有调用。
- Suggestion: sample 对需要 UI 进程的插件调用 `addMainPkg`（包名和别名都加）。使用文档补上该 API。不要改回「全部留 UI」——这是有意的隔离，但默认必须可被示例跑通。

### [P1] 插件 `exported="false"` 的静态 Receiver 被宿主按 exported 重注册

- Location: `XmlHandler.java`（原先不读 exported）；`PmHostSvc.java:285`
- Impact: 插件声明 `exported="false"` 后，宿主仍 `RECEIVER_EXPORTED` 注册代理。任意应用可向宿主发送该 action，触发插件进程。
- Evidence: Manifest 解析只收 filter；`regReceiver` 固定 `exported=true`。demo1 的 `ACTION1` 就是 `exported="false"`。
- Suggestion: 解析 `android:exported`。显式 false 时用 `RECEIVER_NOT_EXPORTED`；未写或 true 保持 exported（系统广播）。

### [P1] p-n 转换用未消毒的插件名拼路径

- Location: `RePluginInstaller.java:71`
- Impact: `com.qihoo360.plugin.name` 来自 APK 元数据。`new File(filesDir, "p-n-" + name + ".jar")` 在 name 含 `../` 时写出安装目录外。调试器 `install_with_pn` 可走到这里。
- Suggestion: 插件名只允许 `[A-Za-z0-9._-]+`，并用 canonical path 限制在 install 目录内。

### [P1] WebView sample 把调试开关写死为 true

- Location: `plugin-webview/.../env/Env.java:28`
- Impact: `Env.DEBUG = true` 不是 `BuildConfig.DEBUG`，release 插件也会 `setWebContentsDebuggingEnabled(true)`。
- Suggestion: 改为 `BuildConfig.DEBUG`。

### [P1] SharedPreferences 改名前缀，已有插件配置会丢

- Location: `replugin-host-library/replugin-host-lib/src/main/java/com/qihoo360/loader2/PluginContext.java:140-144,292-297`
- Impact: 旧文件名是 `plugin_<name>`（多插件共享同一前缀）。新文件名是 `plugin_<pluginName>_<name>`。升级到含 `8d5888b` 的宿主后，插件读到空 SP，登录态/设置/缓存丢失。
- Evidence: diff 将 `name = "plugin_" + name` 换成 `isolateName(name)`，后者在 `mPlugin` 非空时拼上插件名。没有从旧文件复制或 fallback。
- Suggestion: `getSharedPreferences` 先用新名；若新文件不存在且旧 `plugin_<name>` 存在，把旧文件复制到新名再读。隔离仍然生效，已有数据不丢。

---

## P2

### [P2] 外置存储只隔离了 `getExternalFilesDir`

- Location: `PluginContext.java:145-147`
- Impact: `getExternalCacheDir` / 其他外置目录仍指向宿主目录，插件缓存互相覆盖或读到别人的文件。与本次「隔离插件存储」目标不一致。
- Evidence: 只 override 了 `getExternalFilesDir` → `isolateExternalDir`。没有 `getExternalCacheDir`。
- Suggestion: 对 `getExternalCacheDir` 做同样的子目录隔离。

### [P2] `RePlugin.install` 的 p-n 目录限制是死代码

- Location: `replugin-host-library/replugin-host-lib/src/main/java/com/qihoo360/replugin/RePlugin.java:139-148`
- Impact: 注释要求 p-n 插件必须来自 `getPnInstallDir()`。判断写成 `path.startsWith("p-n-")`，而 `path` 必须是绝对路径，条件永远为 false。任意路径的 `p-n-*` 文件都能装。`PmHostSvc.pluginDownloaded` 用的是 `new File(path).getName()`，说明作者本意是文件名。
- Evidence:

```139:148:replugin-host-library/replugin-host-lib/src/main/java/com/qihoo360/replugin/RePlugin.java
        if (path.startsWith("p-n-")) {
            String installPath = RePlugin.getConfig().getPnInstallDir().getAbsolutePath();
            if (!path.startsWith(installPath)) {
```

- Suggestion: 改为 `file.getName().startsWith("p-n-")`，并用 canonical path 确认位于 install 目录内。

### [P2] 签名校验仍只用 `GET_SIGNATURES` / `PackageInfo.signatures`

- Location: `PluginManagerServer.java:131-132,210-211`；`CertUtils.java:52-59`；`V5FileInfo.java:365`
- Impact: API 28+ 应使用 `GET_SIGNING_CERTIFICATES` 和 `signingInfo`。部分 ROM / v3 签名 APK 上 `signatures` 为空，`CertUtils` 直接判失败。release sample 会 `setVerifySign(true)`，外置插件安装会误失败。文档还把证书写成 SHA1，实际是签名字节的 MD5 大写十六进制。
- Evidence: `verifySignEnable` 时 flags 只有 `GET_META_DATA | GET_SIGNATURES`。`isPluginSignatures` 只读 `info.signatures`。`docs/使用文档.md:293` 写「证书SHA1」。
- Suggestion: API 28+ 加上 `GET_SIGNING_CERTIFICATES`，从 `signingInfo.getApkContentsSigners()` 取值，空则回退 `signatures`。文档改成 MD5。

### [P2] `DebuggerReceivers` 以 exported 注册，任意应用可装/卸插件

- Location: `DebuggerReceivers.java:109,196-216`；`ReceiverCompat.java:37-43`；`SampleApplication.java:39`
- Impact: `enableDebugger(true)` 后，动作为 `<pkg>.replugin.install` 等的导出广播可被其他 App 发送，从而安装任意路径 APK 或卸载插件。sample 仅在 `BuildConfig.DEBUG` 打开，但接入方若在 release 打开则是任意代码执行面。
- Evidence: `registerReceiver(..., true)` → API 33+ `RECEIVER_EXPORTED`。`onReceive` 只限制常驻进程，不校验调用方。
- Suggestion: 保持 ADB 可用（需要 exported），但拒绝空/`..` path；文档强调 release 必须关闭。不要在 library 里写死 `BuildConfig.DEBUG`（library 的 DEBUG 不是宿主的）。

### [P2] `ServiceRecord.toString` 运算符优先级错误，service 为空会 NPE

- Location: `replugin-host-library/replugin-host-lib/src/main/java/com/qihoo360/replugin/component/service/server/ServiceRecord.java:112-113`
- Impact: `"[srv=" + service == null` 先做字符串拼接再比较，左边永不为 null，于是走进 `service.getClass()`。dump / 日志在 service 尚未 attach 时崩溃。
- Suggestion: `" [srv=" + (service == null ? "null" : service.getClass().getName()) + ...`

### [P2] `ParcelUtils.createFromParcelable(pa, loader, cln)` 不回收 Parcel

- Location: `replugin-plugin-library/replugin-plugin-lib/src/main/java/com/qihoo360/replugin/utils/ParcelUtils.java:83-101`
- Impact: 内部 `Parcel.obtain()` 后没有 `recycle()`。跨 ClassLoader 反序列化走这条路径会漏 native 内存。
- Suggestion: try/finally 里 recycle。

### [P2] 预 Lollipop extra dex 写出路径未规范化

- Location: `PluginDexClassLoader.java:335-347`
- Impact: 仅过滤 `../`，`new File(dir, name)` 在 `name` 为绝对路径时忽略 `dir`。`name.contains(".dex")` 过宽。minSdk 19，Android 4.4 上恶意 APK 可写到 extra dex 目录外。
- Suggestion: 只用 `new File(name).getName()`，且必须落在 extra dex 目录的 canonical 路径下。

### [P2] sample 安装演示：ProgressDialog 泄漏 + finally NPE

- Location: `replugin-sample/host/app/src/main/java/com/qihoo360/replugin/sample/host/MainActivity.java:116-124,185-208`
- Impact: 1 秒后 `pd.dismiss()`，Activity 已销毁则 WindowLeaked。`copyAssetsFileToAppFiles` 的 finally 在 open 失败时对 null 的 `is`/`fos` 调 `close()`。安装本身在主线程（postDelayed 仍是 main），大 APK 会卡 UI。
- Suggestion: 用 `isFinishing`/`isDestroyed` 再 dismiss；finally 分别判空；复制和 `RePlugin.install` 放到后台，成功后再回主线程 startActivity。

---

## P3

### [P3] 使用文档未写 `addMainPkg`，证书算法写错

- Location: `docs/使用文档.md:289-294,296-311`
- Suggestion: 补 UI 进程白名单；证书改为「签名字节 MD5 大写十六进制」。

### [P3] `enableDebugger` 在 `enable=false` 时不能反注册

- Location: `RePlugin.java:222-227`
- Suggestion: 记录已注册的 receiver，`enable=false` 时 unregister。当前至少保证 release 不要调用 `enable=true`。

### [P3] 框架模块没有单元测试

- Location: 全仓仅 sample 里的 `TestItem` / `TestMultiDex`，不是 JUnit。
- Suggestion: 先给 `CertUtils`、`isolateName`、p-n 路径判断、`ServiceRecord.toString` 补 JVM 可跑的测试。本轮不阻塞。

### [P3] host lint `abortOnError false`，无 CI 测试任务

- Location: `replugin-host-library/replugin-host-lib/build.gradle:48-50`
- Suggestion: 发版前至少跑 `:replugin-host-lib:assembleRelease`。

### [P3] 命名/注释历史债（不改行为）

- `DebuggerReceivers`：`sDebugerReceiver`、`imeediately`
- `Validate.java`：整份 commons-lang 拷贝，仅 `ReflectUtils` 使用其中几个方法
- 调试器必须 exported 才能 `adb shell am broadcast`；release 不要 `enableDebugger(true)`，`enable=false` 仍不能反注册

---

## 已核对、不作为缺陷

- 宿主 Manifest 坑位组件均为 `exported=false`。
- Native SO 释放过滤了 `../`。
- `HiddenApiCompat` 先走 LSPosed HiddenApiBypass 6.1，再走 meta-reflection。
- sample 调试器仅 DEBUG 打开；release 打开签名校验。
- WebView sample 关闭了 file 协议跨源（`setAllowFileAccess(false)` 等）。
- `PluginInfoUpdater` 的 LocalBroadcast 随进程生命周期，不算 Activity 泄漏。
- 静态 `sAppContext` 持有 Application，符合框架进程模型。

---

## 补充：host-lib 第二轮

已就地修补：`fetchViewByLayoutName` 空 Context 直接返回；加载失败重置 `mInitialized`；ClassLoader 创建失败时恢复 dex 可写；Service Handler 持 `LOCKER`；调试安装拒绝空/`..` path。

仍未改：`enableDebugger` 不校验 `FLAG_DEBUGGABLE`、锁失败仍继续加载、坑位 `startService` 在后台被吞掉。

---

## 补充：plugin-lib / Gradle（第二轮审查）

以下由 plugin-lib + Gradle 审查确认，本轮继续修：

- `RePluginInternal.createActivityContext` 失败返回 null → 插件 Activity NPE
- `preload(PluginInfo)` 用了插件 ClassLoader 的 `PluginInfo.class`，宿主方法永远找不到
- `PluginProviderClient` 用 `Boolean.class`/`Integer.class` 反射宿主 primitive 方法，observer/URI 静默失效
- 宿主 Gradle 把 `applicationIdSuffix` 拼了两次；`processManifest` 挂在 output 上，AGP 8/9 上可能为 null
- `GetIdentifierExprEditor` 无条件改写第三参，系统资源 `android` 会查错
- AndroidX `startActivityFromFragment` 在 `mIndex` 缺失时把 requestCode 变成 -1
- `getRunningPlugins()` 解析了 Parcel 却固定 `return null`

---

## 未完全核实

- AGP 9 + Javassist 改写 Java 17 / Compose 类，只看了 `ReClassProcessor` 流程，没有对编出来的 APK 做字节码核对。
- Android 15+ 对隐式 Intent / 前台服务类型的限制：框架自身不 start FGS。
- 16 KB 页对齐的预编译 .so：框架不带业务 so。
- `replugin-sample-extra/fresco` 未做同等深度阅读。

---

## 本轮修正任务（按文件拆分，可并行）

| Task | 文件 | 处理条目 |
|---|---|---|
| A | `PluginApplicationClient.java` | P1 回调泄漏 |
| B | `PluginContext.java` | P1 SP 迁移；P2 `getExternalCacheDir` |
| C | `RePlugin.java`、`PluginManagerServer.java`、`CertUtils.java`、`V5FileInfo.java`、`ServiceRecord.java`、`ParcelUtils.java`、`PluginDexClassLoader.java` | P2 安全/兼容/NPE/Parcel/zip |
| D | `SampleApplication.java`、`MainActivity.java`（host）、`docs/使用文档.md` | P1 addMainPkg；P2 sample 泄漏；P3 文档 |
| E | `RePluginInstaller`、`XmlHandler`/`ManifestParser`/`PmHostSvc`、`Env.java`、FileProvider XML | 安全审查补出的路径穿越、Receiver exported、WebView 调试、root-path |

不做：删除 `Validate.java`、改 lint、补完整单测套件、把 `verifySign` 默认改成 true（会破坏现有接入方）。
