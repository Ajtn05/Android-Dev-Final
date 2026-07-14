package nellas.labs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class HistoryActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_UUID = "saved_uuid";

    private TextView emptyText;
    private RecyclerView recyclerView;

    private Realm realm;
    private WorkoutLogAdapter adapter;
    private RealmResults<WorkoutLog> logs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        realm = Realm.getDefaultInstance();

        emptyText = findViewById(R.id.emptyText);
        recyclerView = findViewById(R.id.recyclerView);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String currentUserUuid = sharedPreferences.getString(KEY_UUID, "");

        //gets workouts from realm that match user info, sorted chrnonologically
        logs = realm.where(WorkoutLog.class)
                .equalTo("ownerId", currentUserUuid)
                .sort("dateCompleted", Sort.DESCENDING)
                .findAll();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WorkoutLogAdapter(this, logs, true, realm);
        recyclerView.setAdapter(adapter);

        updateEmptyState();
        logs.addChangeListener(new RealmChangeListener<RealmResults<WorkoutLog>>() {
            @Override
            public void onChange(RealmResults<WorkoutLog> results) {
                updateEmptyState();
            }
        });

        //inserts navbar class into screen
        NavBar.wire(this);
    }

    private void updateEmptyState() {
        emptyText.setVisibility(logs.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}
