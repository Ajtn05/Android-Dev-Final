package nellas.labs;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class PhotoViewerActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_UUID = "saved_uuid";

    private TextView viewerDate;
    private TextView viewerPose;
    private ImageView viewerImage;
    private LinearLayout compareLayout;
    private ImageView compareImageA;
    private ImageView compareImageB;
    private TextView compareLabelA;
    private TextView compareLabelB;
    private EditText viewerWeightInput;
    private EditText viewerNotesInput;
    private Button btnSaveMeta;
    private Button btnCompare;
    private Button btnDeletePhoto;

    private Realm realm;
    private String currentUserUuid;
    private String photoId;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_photo_viewer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        realm = Realm.getDefaultInstance();

        viewerDate = findViewById(R.id.viewerDate);
        viewerPose = findViewById(R.id.viewerPose);
        viewerImage = findViewById(R.id.viewerImage);
        compareLayout = findViewById(R.id.compareLayout);
        compareImageA = findViewById(R.id.compareImageA);
        compareImageB = findViewById(R.id.compareImageB);
        compareLabelA = findViewById(R.id.compareLabelA);
        compareLabelB = findViewById(R.id.compareLabelB);
        viewerWeightInput = findViewById(R.id.viewerWeightInput);
        viewerNotesInput = findViewById(R.id.viewerNotesInput);
        btnSaveMeta = findViewById(R.id.btnSaveMeta);
        btnCompare = findViewById(R.id.btnCompare);
        btnDeletePhoto = findViewById(R.id.btnDeletePhoto);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentUserUuid = sharedPreferences.getString(KEY_UUID, "");

        photoId = (getIntent() != null) ? getIntent().getStringExtra("PHOTO_ID") : null;
        ProgressPhoto photo = findPhoto();
        if (photo == null) {
            Toast.makeText(this, "Photo not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewerDate.setText(dateFormat.format(photo.getDateCaptured()));
        viewerPose.setText("Pose: " + photo.getPoseTag());
        viewerWeightInput.setText(formatNumber(photo.getCurrentWeight()));
        viewerNotesInput.setText(photo.getNotes());
        loadInto(photo, viewerImage);

        btnSaveMeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMeta();
            }
        });

        btnDeletePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete();
            }
        });

        btnCompare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleCompare();
            }
        });
    }

    private ProgressPhoto findPhoto() {
        return realm.where(ProgressPhoto.class)
                .equalTo("ownerId", currentUserUuid)
                .equalTo("id", photoId)
                .findFirst();
    }

    private void loadInto(ProgressPhoto photo, ImageView target) {
        File file = new File(getExternalCacheDir(), photo.getPhotoPath());
        if (file.exists()) {
            Picasso.get()
                    .load(file)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(target);
        } else {
            target.setImageResource(R.mipmap.ic_launcher);
        }
    }

    private void saveMeta() {
        ProgressPhoto photo = findPhoto();
        if (photo == null) {
            return;
        }

        String weightStr = viewerWeightInput.getText().toString().trim();
        if (weightStr.isEmpty()) {
            Toast.makeText(this, "Bodyweight must not be blank", Toast.LENGTH_SHORT).show();
            return;
        }
        double weight;
        try {
            weight = Double.parseDouble(weightStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Bodyweight must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        realm.beginTransaction();
        photo.setCurrentWeight(weight);
        photo.setNotes(viewerNotesInput.getText().toString().trim());
        realm.commitTransaction();

        Toast.makeText(this, "Photo details updated!", Toast.LENGTH_SHORT).show();
    }

    private void confirmDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this photo?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ProgressPhoto photo = findPhoto();
                if (photo != null) {
                    File imgFile = new File(getExternalCacheDir(), photo.getPhotoPath());
                    if (imgFile.exists()) {
                        imgFile.delete();
                    }
                    realm.beginTransaction();
                    photo.deleteFromRealm();
                    realm.commitTransaction();
                }
                Toast.makeText(PhotoViewerActivity.this, "Photo deleted", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void toggleCompare() {
        if (compareLayout.getVisibility() == View.VISIBLE) {
            compareLayout.setVisibility(View.GONE);
            viewerImage.setVisibility(View.VISIBLE);
            btnCompare.setText("COMPARE");
            return;
        }

        final ProgressPhoto photo = findPhoto();
        if (photo == null) {
            return;
        }

        final RealmResults<ProgressPhoto> others = realm.where(ProgressPhoto.class)
                .equalTo("ownerId", currentUserUuid)
                .notEqualTo("id", photoId)
                .sort("dateCaptured", Sort.DESCENDING)
                .findAll();

        if (others.isEmpty()) {
            Toast.makeText(this, "No other photos to compare with", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] labels = new String[others.size()];
        for (int i = 0; i < others.size(); i++) {
            ProgressPhoto p = others.get(i);
            labels[i] = dateFormat.format(p.getDateCaptured()) + " — " + p.getPoseTag();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Compare with...");
        builder.setItems(labels, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ProgressPhoto other = others.get(which);
                if (other == null) {
                    return;
                }
                loadInto(photo, compareImageA);
                loadInto(other, compareImageB);
                compareLabelA.setText(dateFormat.format(photo.getDateCaptured())
                        + " — " + formatNumber(photo.getCurrentWeight()) + " lbs");
                compareLabelB.setText(dateFormat.format(other.getDateCaptured())
                        + " — " + formatNumber(other.getCurrentWeight()) + " lbs");
                viewerImage.setVisibility(View.GONE);
                compareLayout.setVisibility(View.VISIBLE);
                btnCompare.setText("SINGLE");
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
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
