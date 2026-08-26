package club.knifelf.androidx.host;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.qihoo360.replugin.RePlugin;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_open_plugin).setOnClickListener(v -> openPlugin());
        if (getIntent().getBooleanExtra("auto_open", false)) {
            findViewById(R.id.btn_open_plugin).post(this::openPlugin);
        }
    }

    private void openPlugin() {
        boolean ok = RePlugin.startActivity(this,
                RePlugin.createIntent("androidx", "club.knifelf.androidx.plugin.MainActivity"));
        if (!ok) {
            Toast.makeText(this, "plugin androidx not installed", Toast.LENGTH_LONG).show();
        }
    }
}
