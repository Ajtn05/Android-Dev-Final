package nellas.labs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.UUID;

import io.realm.Realm;

public class RoutineEditActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_UUID = "saved_uuid";

    private TextView headerText;
    private EditText routineNameInput;
    private EditText targetFocusInput;
    private LinearLayout exerciseContainer;
    private Button btnAddExercise;
    private Button saveButton;
    private Button cancelButton;

    private Realm realm;
    private String currentUserUuid;
    private String editRoutineId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_routine_edit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        realm = Realm.getDefaultInstance();

        headerText = findViewById(R.id.headerText);
        routineNameInput = findViewById(R.id.routineNameInput);
        targetFocusInput = findViewById(R.id.targetFocusInput);
        exerciseContainer = findViewById(R.id.exerciseContainer);
        btnAddExercise = findViewById(R.id.btnAddExercise);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentUserUuid = sharedPreferences.getString(KEY_UUID, "");

        if (getIntent() != null && getIntent().hasExtra("EDIT_ROUTINE_ID")) {
            editRoutineId = getIntent().getStringExtra("EDIT_ROUTINE_ID");
            Routine editRoutine = realm.where(Routine.class)
                    .equalTo("ownerId", currentUserUuid)
                    .equalTo("id", editRoutineId)
                    .findFirst();
            if (editRoutine != null) {
                headerText.setText("Edit routine");
                routineNameInput.setText(editRoutine.getName());
                targetFocusInput.setText(editRoutine.getTargetFocus());
                for (Exercise ex : editRoutine.getExercises()) {
                    addExerciseRow(ex);
                }
            }
        } else {
            addExerciseRow(null);
        }

        btnAddExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addExerciseRow(null);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRoutine();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void addExerciseRow(Exercise exercise) {
        final View row = getLayoutInflater().inflate(R.layout.row_exercise_edit, exerciseContainer, false);

        if (exercise != null) {
            EditText exNameInput = row.findViewById(R.id.exNameInput);
            EditText exWeightInput = row.findViewById(R.id.exWeightInput);
            EditText exRepsInput = row.findViewById(R.id.exRepsInput);
            EditText exSetsInput = row.findViewById(R.id.exSetsInput);
            exNameInput.setText(exercise.getName());
            exWeightInput.setText(formatNumber(exercise.getTargetWeight()));
            exRepsInput.setText(String.valueOf(exercise.getTargetReps()));
            exSetsInput.setText(String.valueOf(exercise.getTargetSets()));
        }

        Button btnRemoveExercise = row.findViewById(R.id.btnRemoveExercise);
        btnRemoveExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exerciseContainer.removeView(row);
            }
        });

        exerciseContainer.addView(row);
    }

    private void saveRoutine() {
        String name = routineNameInput.getText().toString().trim();
        String focus = targetFocusInput.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Routine name must not be blank", Toast.LENGTH_SHORT).show();
            return;
        }
        if (exerciseContainer.getChildCount() == 0) {
            Toast.makeText(this, "Add at least one exercise", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate every exercise row before touching Realm
        int rowCount = exerciseContainer.getChildCount();
        String[] exNames = new String[rowCount];
        double[] exWeights = new double[rowCount];
        int[] exReps = new int[rowCount];
        int[] exSets = new int[rowCount];

        for (int i = 0; i < rowCount; i++) {
            View row = exerciseContainer.getChildAt(i);
            EditText exNameInput = row.findViewById(R.id.exNameInput);
            EditText exWeightInput = row.findViewById(R.id.exWeightInput);
            EditText exRepsInput = row.findViewById(R.id.exRepsInput);
            EditText exSetsInput = row.findViewById(R.id.exSetsInput);

            String exName = exNameInput.getText().toString().trim();
            if (exName.isEmpty()) {
                Toast.makeText(this, "Exercise " + (i + 1) + " needs a name", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                exNames[i] = exName;
                exWeights[i] = Double.parseDouble(exWeightInput.getText().toString().trim());
                exReps[i] = Integer.parseInt(exRepsInput.getText().toString().trim());
                exSets[i] = Integer.parseInt(exSetsInput.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Exercise " + (i + 1) + " has an invalid weight/reps/sets", Toast.LENGTH_SHORT).show();
                return;
            }
            if (exReps[i] < 1 || exSets[i] < 1) {
                Toast.makeText(this, "Exercise " + (i + 1) + " needs at least 1 rep and 1 set", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Duplicate routine name for this user (excluding the routine being edited)
        Routine existingRoutine = realm.where(Routine.class)
                .equalTo("ownerId", currentUserUuid)
                .equalTo("name", name)
                .findFirst();
        if (existingRoutine != null && !existingRoutine.getId().equals(editRoutineId)) {
            Toast.makeText(this, "You already have a routine with that name", Toast.LENGTH_SHORT).show();
            return;
        }

        realm.beginTransaction();

        Routine routine;
        if (editRoutineId != null) {
            routine = realm.where(Routine.class)
                    .equalTo("ownerId", currentUserUuid)
                    .equalTo("id", editRoutineId)
                    .findFirst();
            if (routine == null) {
                realm.cancelTransaction();
                Toast.makeText(this, "Routine no longer exists", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            routine.getExercises().deleteAllFromRealm();
        } else {
            routine = realm.createObject(Routine.class, UUID.randomUUID().toString());
            routine.setOwnerId(currentUserUuid);
        }

        routine.setName(name);
        routine.setTargetFocus(focus);

        for (int i = 0; i < rowCount; i++) {
            Exercise ex = realm.createObject(Exercise.class, UUID.randomUUID().toString());
            ex.setOwnerId(currentUserUuid);
            ex.setName(exNames[i]);
            ex.setTargetWeight(exWeights[i]);
            ex.setTargetReps(exReps[i]);
            ex.setTargetSets(exSets[i]);
            routine.getExercises().add(ex);
        }

        realm.commitTransaction();

        Toast.makeText(this, (editRoutineId != null) ? "Routine updated!" : "Routine saved!", Toast.LENGTH_SHORT).show();
        finish();
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
