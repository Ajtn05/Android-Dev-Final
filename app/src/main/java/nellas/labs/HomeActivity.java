package nellas.labs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class HomeActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_UUID = "saved_uuid";

    private TextView welcomeText;
    private ImageView welcomeImageView;
    private TextView emptyText;
    private Button btnNewRoutine;
    private RecyclerView recyclerView;

    private Realm realm;
    private RoutineAdapter adapter;
    private RealmResults<Routine> routines;
    private String currentUserUuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        realm = Realm.getDefaultInstance();

        welcomeText = findViewById(R.id.welcomeText);
        welcomeImageView = findViewById(R.id.welcomeImageView);
        emptyText = findViewById(R.id.emptyText);
        btnNewRoutine = findViewById(R.id.btnNewRoutine);
        recyclerView = findViewById(R.id.recyclerView);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentUserUuid = sharedPreferences.getString(KEY_UUID, "");

        User currentUser = realm.where(User.class).equalTo("uuid", currentUserUuid).findFirst();

        boolean isRemembered = false;
        Intent intent = getIntent();
        if (intent != null) {
            isRemembered = intent.getBooleanExtra("IS_REMEMBERED", false);
        }

        String displayName = (currentUser != null) ? currentUser.getName() : "Guest";
        if (isRemembered) {
            welcomeText.setText("Hello " + displayName);
        } else {
            welcomeText.setText("Hello");
        }

        if (currentUser != null) {
            File file = new File(getExternalCacheDir(), currentUser.getUuid() + ".jpeg");
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
            welcomeImageView.setImageResource(R.mipmap.ic_launcher);
        }

        routines = realm.where(Routine.class).equalTo("ownerId", currentUserUuid).sort("name").findAll();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RoutineAdapter(this, routines, true, realm);
        recyclerView.setAdapter(adapter);

        updateEmptyState();
        routines.addChangeListener(new RealmChangeListener<RealmResults<Routine>>() {
            @Override
            public void onChange(RealmResults<Routine> results) {
                updateEmptyState();
            }
        });

        btnNewRoutine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, RoutineEditActivity.class);
                startActivity(intent);
            }
        });

        NavBar.wire(this);
    }

    private void updateEmptyState() {
        emptyText.setVisibility(routines.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
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
