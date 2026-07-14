package nellas.labs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import io.realm.Realm;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private Button signInButton;
    private Button signUpButton;
    private Button adminButton;
    private Button clearButton;
    private CheckBox checkBox;

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_UUID = "saved_uuid";
    private static final String REMEMBERED = "remember_me_state";

    private Realm realm;

    //dead ass can't lie, copied from labs because it works
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        signInButton = findViewById(R.id.signInButton);
        signUpButton = findViewById(R.id.signUpButton);
        adminButton = findViewById(R.id.adminButton);
        clearButton = findViewById(R.id.clearButton);
        checkBox = findViewById(R.id.checkBox);

        realm = Realm.getDefaultInstance();

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String savedUuid = sharedPreferences.getString(KEY_UUID, "");
        String remembered = sharedPreferences.getString(REMEMBERED, "false");

        if (remembered.equals("true") && !savedUuid.isEmpty()) {
            User rememberedUser = realm.where(User.class).equalTo("uuid", savedUuid).findFirst();
            if (rememberedUser != null) {
                usernameInput.setText(rememberedUser.getName());
                passwordInput.setText(rememberedUser.getPassword());
                checkBox.setChecked(true);
            }
        }

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String typedUser = usernameInput.getText().toString().trim();
                String typedPass = passwordInput.getText().toString().trim();

                User existingUser = realm.where(User.class).equalTo("name", typedUser).findFirst();

                if (existingUser == null) {
                    Toast.makeText(LoginActivity.this, "No User Found", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (typedPass.equals(existingUser.getPassword())) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        intent.putExtra("IS_REMEMBERED", checkBox.isChecked());

                        editor.putString(KEY_UUID, existingUser.getUuid());

                        if (checkBox.isChecked()) {
                            editor.putString(REMEMBERED, "true");
                        }
                        else {
                            editor.putString(REMEMBERED, "false");
                        }
                        editor.apply();

                        startActivity(intent);
                        finish();
                    }
                    else  {
                        Toast.makeText(LoginActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // No EDIT_UUID -> RegisterActivity runs in create mode.
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        //note admin functionality is hidden for the moment
        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                startActivity(intent);
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();

                usernameInput.setText("");
                passwordInput.setText("");
                checkBox.setChecked(false);

                Toast.makeText(LoginActivity.this, "Preferences Cleared", Toast.LENGTH_SHORT).show();
            }
        });
    }
}