package club.knifelf.fresco.plugin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!Fresco.hasBeenInitialized()) {
            Fresco.initialize(getApplicationContext());
        }
        SimpleDraweeView image = findViewById(R.id.image);
        image.setImageURI(Uri.parse("https://www.gstatic.com/webp/gallery/1.jpg"));

    }
}
