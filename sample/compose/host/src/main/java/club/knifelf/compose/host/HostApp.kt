package club.knifelf.compose.host

import android.content.Context
import com.qihoo360.replugin.RePlugin
import com.qihoo360.replugin.RePluginApplication
import com.qihoo360.replugin.RePluginConfig

class HostApp : RePluginApplication() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        RePlugin.enableDebugger(base, BuildConfig.DEBUG)
        RePlugin.addMainPkg("compose")
        RePlugin.addMainPkg("club.knifelf.compose.plugin")
    }

    override fun createConfig(): RePluginConfig {
        return RePluginConfig().apply {
            setUseHostClassIfNotFound(true)
            setVerifySign(false)
        }
    }
}
