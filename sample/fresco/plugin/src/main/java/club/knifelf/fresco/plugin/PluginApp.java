package club.knifelf.fresco.plugin;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

public class PluginApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (!Fresco.hasBeenInitialized()) {
            Fresco.initialize(this);
        }
    }
}
