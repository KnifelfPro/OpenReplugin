# OpenReplugin samples

Each folder is one Gradle project with two applications (`:host` and `:plugin`).
Dependencies come from Maven Central / Google (`io.github.knifelfpro:3.3.0`).

| Folder | Stack | Host / plugin id |
|---|---|---|
| `android-support` | Support Library AppCompat | `club.knifelf.support.*` |
| `androidx` | AndroidX AppCompat | `club.knifelf.androidx.*` |
| `kotlin` | Kotlin + AndroidX | `club.knifelf.kotlin.*` |
| `compose` | Jetpack Compose (minSdk 23) | `club.knifelf.compose.*` |
| `fresco` | AndroidX + Fresco 3.6.0 (minSdk 21) | `club.knifelf.fresco.*` |

One command builds the plugin, copies it to `host/src/main/assets/plugins/{alias}.jar`, then builds the host:

```bash
cd sample/androidx
./gradlew assembleSample
```

Install on a connected device / emulator:

```bash
./gradlew installSample
```

`:host:assembleDebug` and `:host:installDebug` do the same pipeline (`preBuild` depends on `:plugin:copyPluginToHost`).

`android-support` excludes `animated-vector-drawable` because AGP 9 rejects Support 28.0.0's duplicate `android.support.graphics.drawable` namespace. `support-vector-drawable` stays so `AppCompatActivity` can resolve `VectorDrawableCompat`.
