package nellas.labs;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class PhotosActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_UUID = "saved_uuid";
    public static final int REQUEST_CODE_IMAGE_SCREEN = 103;

    private TextView latestWeightText;
    private TextView emptyText;
    private Button btnAddPhoto;
    private RecyclerView recyclerView;

    private Realm realm;
    private String currentUserUuid;
    private PhotoAdapter adapter;
    private RealmResults<ProgressPhoto> photos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_photos);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        realm = Realm.getDefaultInstance();

        latestWeightText = findViewById(R.id.latestWeightText);
        emptyText = findViewById(R.id.emptyText);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        recyclerView = findViewById(R.id.recyclerView);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentUserUuid = sharedPreferences.getString(KEY_UUID, "");

        photos = realm.where(ProgressPhoto.class)
                .equalTo("ownerId", currentUserUuid)
                .sort("dateCaptured", Sort.ASCENDING)
                .findAll();

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new PhotoAdapter(this, photos, true);
        recyclerView.setAdapter(adapter);

        updateEmptyState();
        updateLatestWeight();
        photos.addChangeListener(new RealmChangeListener<RealmResults<ProgressPhoto>>() {
            @Override
            public void onChange(RealmResults<ProgressPhoto> results) {
                updateEmptyState();
                updateLatestWeight();
            }
        });

        btnAddPhoto.setOnClickListener(new View.OnClickListener() {
            //checks permissions lang before adding
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

                Dexter.withContext(PhotosActivity.this)
                        .withPermissions(permissionsList)
                        .withListener(new BaseMultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
                                if (report.areAllPermissionsGranted()) {
                                    Intent intent = new Intent(PhotosActivity.this, ImageActivity.class);
                                    startActivityForResult(intent, REQUEST_CODE_IMAGE_SCREEN);
                                } else {
                                    Toast.makeText(PhotosActivity.this,
                                            "Camera and Storage permissions are required to add a photo.",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        }).check();
            }
        });

        NavBar.wire(this);
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

    private void updateEmptyState() {
        emptyText.setVisibility(photos.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateLatestWeight() {
        ProgressPhoto latest = realm.where(ProgressPhoto.class)
                .equalTo("ownerId", currentUserUuid)
                .sort("dateCaptured", Sort.DESCENDING)
                .findFirst();
        if (latest != null) {
            latestWeightText.setText(getString(R.string.label_latest_bodyweight) + " "
                    + formatNumber(latest.getCurrentWeight()) + " " + getString(R.string.unit_lbs));
        } else {
            latestWeightText.setText(getString(R.string.label_no_bodyweight));
        }
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
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        updateLatestWeight();
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
