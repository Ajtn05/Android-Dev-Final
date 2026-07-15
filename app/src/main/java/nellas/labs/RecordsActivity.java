package nellas.labs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

public class RecordsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_UUID = "saved_uuid";

    public static class RecordEntry {
        public String exerciseName;
        public double weight;
        public int reps;
        public Date date;
        public String workoutLogId;
    }

    private Button btnSortToggle;
    private TextView emptyText;
    private RecyclerView recyclerView;

    private Realm realm;
    private String currentUserUuid;
    private final List<RecordEntry> records = new ArrayList<>();
    private RecordAdapter adapter;
    private int sortMode = 0; // 0 = weight desc, 1 = alphabetical, 2 = date desc

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_records);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        realm = Realm.getDefaultInstance();

        btnSortToggle = findViewById(R.id.btnSortToggle);
        emptyText = findViewById(R.id.emptyText);
        recyclerView = findViewById(R.id.recyclerView);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentUserUuid = sharedPreferences.getString(KEY_UUID, "");

        buildRecords();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecordAdapter(this, records);
        recyclerView.setAdapter(adapter);

        emptyText.setVisibility(records.isEmpty() ? View.VISIBLE : View.GONE);

        //lets you sort based on preference. uses sortMode variable ah
        btnSortToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortMode = (sortMode + 1) % 3;
                applySort();
                adapter.notifyDataSetChanged();
                if (sortMode == 0) {
                    btnSortToggle.setText("Sort: Weight");
                } else if (sortMode == 1) {
                    btnSortToggle.setText("Sort: A-Z");
                } else {
                    btnSortToggle.setText("Sort: Date");
                }
            }
        });

        NavBar.wire(this);
    }

    // get PR
    private void buildRecords() {
        records.clear();
        Map<String, RecordEntry> best = new HashMap<>();

        RealmResults<WorkoutLog> logs = realm.where(WorkoutLog.class)
                .equalTo("ownerId", currentUserUuid)
                .findAll();

        for (WorkoutLog log : logs) {
            for (SetLog set : log.getSets()) {
                if (!set.isCompleted()) {
                    continue;
                }
                RecordEntry current = best.get(set.getExerciseName());
                if (current == null || set.getWeight() > current.weight) {
                    RecordEntry entry = new RecordEntry();
                    entry.exerciseName = set.getExerciseName();
                    entry.weight = set.getWeight();
                    entry.reps = set.getReps();
                    entry.date = log.getDateCompleted();
                    entry.workoutLogId = log.getId();
                    best.put(set.getExerciseName(), entry);
                }
            }
        }

        records.addAll(best.values());
        applySort();
    }

    private void applySort() {
        if (sortMode == 0) {
            Collections.sort(records, new Comparator<RecordEntry>() {
                @Override
                public int compare(RecordEntry a, RecordEntry b) {
                    return Double.compare(b.weight, a.weight);
                }
            });
        } else if (sortMode == 1) {
            Collections.sort(records, new Comparator<RecordEntry>() {
                @Override
                public int compare(RecordEntry a, RecordEntry b) {
                    return a.exerciseName.compareToIgnoreCase(b.exerciseName);
                }
            });
        } else {
            Collections.sort(records, new Comparator<RecordEntry>() {
                @Override
                public int compare(RecordEntry a, RecordEntry b) {
                    return b.date.compareTo(a.date);
                }
            });
        }
    }

    //called by RecordAdapter when a record row is tapped.
    public void showSourceWorkout(RecordEntry entry) {
        WorkoutLog log = realm.where(WorkoutLog.class)
                .equalTo("ownerId", currentUserUuid)
                .equalTo("id", entry.workoutLogId)
                .findFirst();
        if (log == null) {
            Toast.makeText(this, "Workout no longer exists", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append(dateFormat.format(log.getDateCompleted()));
        message.append("\nTotal volume: ").append(formatNumber(log.getTotalVolume())).append(" lbs\n\nSets:\n");
        for (SetLog set : log.getSets()) {
            message.append(set.getExerciseName())
                    .append(" — ").append(formatNumber(set.getWeight())).append(" lbs × ").append(set.getReps())
                    .append(set.isCompleted() ? " ✓" : " ✗")
                    .append("\n");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(log.getRoutineName());
        builder.setMessage(message.toString());
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    public String formatRecordDetail(RecordEntry entry) {
        return formatNumber(entry.weight) + " lbs × " + entry.reps + " — " + dateFormat.format(entry.date);
    }

    private String formatNumber(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
