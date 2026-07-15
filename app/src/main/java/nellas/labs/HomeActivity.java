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
    private TextView tapHint;
    private Button btnNewRoutine;
    private RecyclerView recyclerView;

    private Realm realm;
    private RoutineAdapter adapter;
    private RealmResults<Routine> routines;
    private String currentUserUuid;
    private boolean isRemembered = false;

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
        tapHint = findViewById(R.id.tapHint);
        btnNewRoutine = findViewById(R.id.btnNewRoutine);
        recyclerView = findViewById(R.id.recyclerView);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentUserUuid = sharedPreferences.getString(KEY_UUID, "");

        Intent intent = getIntent();
        if (intent != null) {
            isRemembered = intent.getBooleanExtra("IS_REMEMBERED", false);
        }

        refreshProfileHeader();

        //fetches routines that match user info
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

        //button for starting new routine, opens routine edit chuchu
        btnNewRoutine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, RoutineEditActivity.class);
                startActivity(intent);
            }
        });

        //lets you click on profile pic to edit profile
        welcomeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfileMenu();
            }
        });

        NavBar.wire(this);
    }

    // edits greeting and picture
    private void refreshProfileHeader() {
        User currentUser = realm.where(User.class).equalTo("uuid", currentUserUuid).findFirst();

        String displayName = (currentUser != null) ? currentUser.getName() : "Guest";
        if (isRemembered) {
            welcomeText.setText(getString(R.string.label_hello) + " " + displayName);
        } else {
            welcomeText.setText(getString(R.string.label_hello));
        }

        if (currentUser != null) {
            File file = new File(PhotoHelper.getPhotoDir(this), currentUser.getUuid() + ".jpeg");
            if (file.exists()) {
                Picasso.get()
                        .load(file)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .into(welcomeImageView);
            } else {
                welcomeImageView.setImageResource(R.drawable.ic_placeholder);
            }
        } else {
            welcomeImageView.setImageResource(R.drawable.ic_placeholder);
        }
    }

    private void updateEmptyState() {
        boolean empty = routines.isEmpty();
        emptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
        tapHint.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    //handles editing user info functionality
    private void showProfileMenu() {
        final User currentUser = realm.where(User.class).equalTo("uuid", currentUserUuid).findFirst();

        //not my implementation so edit with care
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.profile_options_title);

        if (currentUser != null) {
            String[] options = {
                    getString(R.string.action_edit_profile),
                    getString(R.string.action_sign_out)
            };
            builder.setItems(options, new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    if (which == 0) {
                        Intent intent = new Intent(HomeActivity.this, RegisterActivity.class);
                        intent.putExtra("EDIT_UUID", currentUserUuid);
                        startActivity(intent);
                    } else {
                        signOut();
                    }
                }
            });
        } else {
            String[] options = { getString(R.string.action_sign_out) };
            builder.setItems(options, new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    signOut();
                }
            });
        }
        builder.show();
    }

    private void signOut() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();

        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshProfileHeader();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        NavBar.refresh(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}
