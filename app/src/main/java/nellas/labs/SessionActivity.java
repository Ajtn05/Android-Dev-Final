package nellas.labs;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;

public class SessionActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_UUID = "saved_uuid";
    public static final int REQUEST_CODE_IMAGE_SCREEN = 102;

    public static class SetEntry {
        public String exerciseName;
        public int setNumber;
        public String weightStr;
        public String repsStr;
        public boolean completed;
    }

    private TextView sessionHeader;
    //clock
    private Chronometer chronometer;
    private EditText notesInput;
    private Button btnTakePhoto;
    private Button btnFinishWorkout;
    private RecyclerView recyclerView;

    private Realm realm;
    private String currentUserUuid;
    private String routineId;
    private final List<SetEntry> setEntries = new ArrayList<>();
    private SessionSetAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_session);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        realm = Realm.getDefaultInstance();

        sessionHeader = findViewById(R.id.sessionHeader);
        chronometer = findViewById(R.id.chronometer);
        notesInput = findViewById(R.id.notesInput);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnFinishWorkout = findViewById(R.id.btnFinishWorkout);
        recyclerView = findViewById(R.id.recyclerView);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentUserUuid = sharedPreferences.getString(KEY_UUID, "");

        routineId = (getIntent() != null) ? getIntent().getStringExtra("ROUTINE_ID") : null;
        Routine routine = realm.where(Routine.class)
                .equalTo("ownerId", currentUserUuid)
                .equalTo("id", routineId)
                .findFirst();
        if (routine == null) {
            Toast.makeText(this, "Routine not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        sessionHeader.setText(routine.getName());
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();

        for (Exercise ex : routine.getExercises()) {
            for (int s = 1; s <= ex.getTargetSets(); s++) {
                SetEntry entry = new SetEntry();
                entry.exerciseName = ex.getName();
                entry.setNumber = s;
                entry.weightStr = formatNumber(ex.getTargetWeight());
                entry.repsStr = String.valueOf(ex.getTargetReps());
                entry.completed = false;
                setEntries.add(entry);
            }
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SessionSetAdapter(this, setEntries);
        recyclerView.setAdapter(adapter);

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> permissionsList = new ArrayList<>();
                permissionsList.add(Manifest.permission.CAMERA);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    permissionsList.add(Manifest.permission.READ_MEDIA_IMAGES);
                } else {
                    permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                    permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }

                Dexter.withContext(SessionActivity.this)
                        .withPermissions(permissionsList)
                        .withListener(new BaseMultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
                                if (report.areAllPermissionsGranted()) {
                                    Intent intent = new Intent(SessionActivity.this, ImageActivity.class);
                                    startActivityForResult(intent, REQUEST_CODE_IMAGE_SCREEN);
                                } else {
                                    Toast.makeText(SessionActivity.this,
                                            "Camera and Storage permissions are required to add a photo.",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        }).check();
            }
        });

        btnFinishWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishWorkout();
            }
        });
    }

    private void finishWorkout() {
        boolean anyCompleted = false;
        double totalVolume = 0;

        for (int i = 0; i < setEntries.size(); i++) {
            SetEntry entry = setEntries.get(i);
            if (entry.completed) {
                anyCompleted = true;
                try {
                    double weight = Double.parseDouble(entry.weightStr.trim());
                    int reps = Integer.parseInt(entry.repsStr.trim());
                    totalVolume += weight * reps;
                } catch (NumberFormatException e) {
                    Toast.makeText(this, entry.exerciseName + " Set " + entry.setNumber
                            + " has an invalid weight/reps", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        if (!anyCompleted) {
            Toast.makeText(this, "Complete at least one set first!", Toast.LENGTH_SHORT).show();
            return;
        }

        realm.beginTransaction();

        Routine routine = realm.where(Routine.class)
                .equalTo("ownerId", currentUserUuid)
                .equalTo("id", routineId)
                .findFirst();

        WorkoutLog log = realm.createObject(WorkoutLog.class, UUID.randomUUID().toString());
        log.setOwnerId(currentUserUuid);
        log.setRoutineName(sessionHeader.getText().toString());
        log.setDateCompleted(new Date());
        log.setTotalVolume(totalVolume);
        log.setNotes(notesInput.getText().toString().trim());

        for (SetEntry entry : setEntries) {
            SetLog setLog = realm.createObject(SetLog.class, UUID.randomUUID().toString());
            setLog.setOwnerId(currentUserUuid);
            setLog.setExerciseName(entry.exerciseName);
            setLog.setWeight(parseDoubleOrZero(entry.weightStr));
            setLog.setReps(parseIntOrZero(entry.repsStr));
            setLog.setCompleted(entry.completed);
            log.getSets().add(setLog);
        }

        if (routine != null) {
            routine.setLastPerformed(new Date());
        }

        realm.commitTransaction();

        chronometer.stop();
        Toast.makeText(this, "Workout logged! Total volume: " + formatNumber(totalVolume) + " lbs", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMAGE_SCREEN && resultCode == ImageActivity.RESULT_CODE_IMAGE_TAKEN) {
            if (data != null && data.hasExtra("rawJpeg")) {
                byte[] rawJpegData = data.getByteArrayExtra("rawJpeg");
                PhotoHelper.promptAndSave(this, realm, rawJpegData, currentUserUuid);
            }
        }
    }

    private double parseDoubleOrZero(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int parseIntOrZero(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String formatNumber(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}
