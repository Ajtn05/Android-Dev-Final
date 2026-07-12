package nellas.labs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView; // Added
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.MemoryPolicy;   // Added
import com.squareup.picasso.NetworkPolicy;  // Added
import com.squareup.picasso.Picasso;        // Added

import java.io.File; // Added

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    private TextView welcomeTextView;
    private ImageView welcomeImageView; // Added
    private Realm realm;

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_UUID = "saved_uuid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        realm = Realm.getDefaultInstance();

        welcomeTextView = findViewById(R.id.mainText);
        welcomeImageView = findViewById(R.id.welcomeImageView);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedUuid = sharedPreferences.getString(KEY_UUID, "");

        User currentUser = realm.where(User.class).equalTo("uuid", savedUuid).findFirst();

        Intent intent = getIntent();
        boolean isRemembered = false;
        if (intent != null) {
            isRemembered = intent.getBooleanExtra("IS_REMEMBERED", false);
        }

        String displayName = (currentUser != null) ? currentUser.getName() : "Guest";

        if (isRemembered) {
            welcomeTextView.setText("Welcome " + displayName + ", you'll be remembered");
        } else {
            welcomeTextView.setText("Welcome " + displayName);
        }

        // --- Picasso Welcome Image Display ---
        if (currentUser != null) {
            File getImageDir = getExternalCacheDir();
            File file = new File(getImageDir, currentUser.getUuid() + ".jpeg");

            if (file.exists()) {
                Picasso.get()
                        .load(file)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .into(welcomeImageView);
            } else {
                welcomeImageView.setImageResource(R.mipmap.ic_launcher);
            }
        } else {
            welcomeImageView.setImageResource(R.mipmap.ic_launcher); // Guest fallback
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}