package nellas.labs;

import android.content.Intent; // Added
import android.graphics.Bitmap; // Added
import android.graphics.BitmapFactory; // Added
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView; // Added
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.MemoryPolicy; // Added
import com.squareup.picasso.NetworkPolicy; // Added
import com.squareup.picasso.Picasso; // Added
import android.Manifest;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;
import java.util.List;

import java.io.File; // Added
import java.io.FileOutputStream; // Added
import java.io.IOException; // Added
import java.util.UUID;
import io.realm.Realm;


public class RegisterActivity extends AppCompatActivity {

    private EditText registerUsernameInput;
    private EditText registerPasswordInput;
    private EditText confirmPasswordInput;
    private Button saveButton;
    private Button cancelButton;
    private Realm realm;
    private String editUuid = null;
    private TextView headerText;

    // Added fields
    private ImageView registerImage;
    private byte[] rawJpegData = null;
    public static final int REQUEST_CODE_IMAGE_SCREEN = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        realm = Realm.getDefaultInstance();

        registerUsernameInput = findViewById(R.id.registerUsernameInput);
        registerPasswordInput = findViewById(R.id.registerPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        headerText = findViewById(R.id.headerText);
        registerImage = findViewById(R.id.registerImage); // Added (Ensure this matches your layout XML layout ID)

        if (getIntent() != null && getIntent().hasExtra("EDIT_UUID")) {
            headerText.setText("EDIT");
            editUuid = getIntent().getStringExtra("EDIT_UUID");
            User editUser = realm.where(User.class).equalTo("uuid", editUuid).findFirst();
            if (editUser != null) {
                registerUsernameInput.setText(editUser.getName());
                registerPasswordInput.setText(editUser.getPassword());
                confirmPasswordInput.setText(editUser.getPassword());

                // Load existing photo if it exists
                File file = new File(getExternalCacheDir(), editUuid + ".jpeg");
                if (file.exists()) {
                    Picasso.get()
                            .load(file)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .into(registerImage);
                }
            }
        }

        // Tap image view to open photo/crop screen
        registerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, ImageActivity.class);
                startActivityForResult(intent, REQUEST_CODE_IMAGE_SCREEN);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = registerUsernameInput.getText().toString().trim();
                String password = registerPasswordInput.getText().toString().trim();
                String confirmPassword = confirmPasswordInput.getText().toString().trim();

                if (username.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Name must not be blank", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Confirm password does not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                realm.beginTransaction();
                String finalUuid = editUuid;

                if (editUuid != null) {
                    User userToEdit = realm.where(User.class).equalTo("uuid", editUuid).findFirst();
                    if (userToEdit != null) {
                        userToEdit.setName(username);
                        userToEdit.setPassword(password);
                    }
                    // Validate distinct naming rule (excluding self)
                    User existingUser = realm.where(User.class).equalTo("name", username).findFirst();
                    if (existingUser != null && !existingUser.getUuid().equals(editUuid)) {
                        realm.cancelTransaction();
                        Toast.makeText(RegisterActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    realm.commitTransaction();
                    Toast.makeText(RegisterActivity.this, "User details updated!", Toast.LENGTH_SHORT).show();
                } else {
                    User existingUser = realm.where(User.class).equalTo("name", username).findFirst();
                    if (existingUser != null) {
                        realm.cancelTransaction();
                        Toast.makeText(RegisterActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    finalUuid = UUID.randomUUID().toString();
                    User newUser = realm.createObject(User.class, finalUuid);
                    newUser.setName(username);
                    newUser.setPassword(password);
                    realm.commitTransaction();

                    long totalUsers = realm.where(User.class).count();
                    Toast.makeText(RegisterActivity.this, "New User saved. Total: " + totalUsers, Toast.LENGTH_SHORT).show();
                }

                // Save image file if a new image was taken
                if (rawJpegData != null) {
                    try {
                        saveFile(rawJpegData, finalUuid + ".jpeg");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                finish();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // Replace your current registerImage.setOnClickListener with this updated logic:
        registerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Build the list of required permissions dynamically based on the device's Android version
                java.util.List<String> permissionsList = new java.util.ArrayList<>();
                permissionsList.add(Manifest.permission.CAMERA);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    // Android 13 (API 33) and above uses granular media permissions
                    permissionsList.add(Manifest.permission.READ_MEDIA_IMAGES);
                } else {
                    // Android 12 and below use the classic storage permissions
                    permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                    permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }

                // Pass the dynamically built list to Dexter
                Dexter.withContext(RegisterActivity.this)
                        .withPermissions(permissionsList)
                        .withListener(new BaseMultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
                                if (report.areAllPermissionsGranted()) {
                                    // Launch ImageActivity safely
                                    Intent intent = new Intent(RegisterActivity.this, ImageActivity.class);
                                    startActivityForResult(intent, REQUEST_CODE_IMAGE_SCREEN);
                                } else {
                                    Toast.makeText(RegisterActivity.this,
                                            "Camera and Storage permissions are required to add a photo.",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        }).check();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMAGE_SCREEN && resultCode == ImageActivity.RESULT_CODE_IMAGE_TAKEN) {
            if (data != null && data.hasExtra("rawJpeg")) {
                rawJpegData = data.getByteArrayExtra("rawJpeg");
                // Update presentation thumbnail immediately
                Bitmap bitmap = BitmapFactory.decodeByteArray(rawJpegData, 0, rawJpegData.length);
                registerImage.setImageBitmap(bitmap);
            }
        }
    }

    private File saveFile(byte[] jpeg, String filename) throws IOException {
        File getImageDir = getExternalCacheDir();
        File savedImage = new File(getImageDir, filename);
        FileOutputStream fos = new FileOutputStream(savedImage);
        fos.write(jpeg);
        fos.close();
        return savedImage;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}