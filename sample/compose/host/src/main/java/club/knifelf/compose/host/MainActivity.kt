package club.knifelf.compose.host

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.qihoo360.replugin.RePlugin

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<android.view.View>(R.id.btn_open_plugin).setOnClickListener { openPlugin() }
        if (intent.getBooleanExtra("auto_open", false)) {
            findViewById<android.view.View>(R.id.btn_open_plugin).post { openPlugin() }
        }
    }

    private fun openPlugin() {
        val ok = RePlugin.startActivity(
            this,
            RePlugin.createIntent("compose", "club.knifelf.compose.plugin.MainActivity")
        )
        if (!ok) {
            Toast.makeText(this, "plugin compose not installed", Toast.LENGTH_LONG).show()
        }
    }
}
