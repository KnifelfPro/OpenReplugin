package club.knifelf.androidx.host;

import android.content.Context;

import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.RePluginApplication;
import com.qihoo360.replugin.RePluginConfig;

public class HostApp extends RePluginApplication {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        RePlugin.enableDebugger(base, BuildConfig.DEBUG);
        RePlugin.addMainPkg("androidx");
        RePlugin.addMainPkg("club.knifelf.androidx.plugin");
    }

    @Override
    protected RePluginConfig createConfig() {
        RePluginConfig c = new RePluginConfig();
        c.setUseHostClassIfNotFound(true);
        c.setVerifySign(false);
        return c;
    }
}
