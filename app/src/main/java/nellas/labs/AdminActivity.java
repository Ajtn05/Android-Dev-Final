package nellas.labs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.realm.Realm;
import io.realm.RealmResults;

//note, im leaving this here incase we'll need it at some point but rn it's not being used
public class AdminActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button btnAddUser;
    private Button btnClearRealm;
    private Realm realm;
    private UserAdapter adapter;
    private RealmResults<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //default stuffs
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerView);
        btnAddUser = findViewById(R.id.btnAddUser);
        btnClearRealm = findViewById(R.id.btnClearRealm);

        realm = Realm.getDefaultInstance();
        users = realm.where(User.class).findAll();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(this, users, true, realm);
        recyclerView.setAdapter(adapter);

        btnAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        btnClearRealm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realm.beginTransaction();
                realm.delete(User.class);
                realm.commitTransaction();
                adapter.notifyDataSetChanged();
            }
        });
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