package nellas.labs;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File; // Added

import io.realm.RealmRecyclerViewAdapter;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;

public class UserAdapter extends RealmRecyclerViewAdapter<User, UserAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView rowName;
        TextView rowPassword;
        Button btnEdit;
        Button btnDelete;
        ImageView rowImage; // Added

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rowName = itemView.findViewById(R.id.rowName);
            rowPassword = itemView.findViewById(R.id.rowPassword);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            rowImage = itemView.findViewById(R.id.rowImage); // Added (Ensure R.id.rowImage exists in row_user.xml)
        }
    }

    private AdminActivity activity;
    private Realm realm;

    public UserAdapter(AdminActivity activity, @Nullable OrderedRealmCollection<User> data, boolean autoUpdate) {
        super(data, autoUpdate);
        this.activity = activity;
        this.realm = Realm.getDefaultInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = activity.getLayoutInflater().inflate(R.layout.row_user, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User u = getItem(position);

        if (u != null) {
            holder.rowName.setText(u.getName());
            holder.rowPassword.setText(u.getPassword());

            // --- Picasso Image Loading Implementation ---
            File getImageDir = activity.getExternalCacheDir();
            File file = new File(getImageDir, u.getUuid() + ".jpeg");

            if (file.exists()) {
                Picasso.get()
                        .load(file)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .into(holder.rowImage);
            } else {
                holder.rowImage.setImageResource(R.mipmap.ic_launcher);
            }
            // --------------------------------------------

            holder.btnEdit.setTag(u.getUuid());
            holder.btnDelete.setTag(u.getUuid());

            holder.btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String uuid = (String) v.getTag();
                    Intent intent = new Intent(activity, RegisterActivity.class);
                    intent.putExtra("EDIT_UUID", uuid);
                    activity.startActivity(intent);
                }
            });

            holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String uuid = (String) v.getTag();

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("Confirm Delete");
                    builder.setMessage("Are you sure you want to delete this user?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            realm.beginTransaction();
                            User toDelete = realm.where(User.class).equalTo("uuid", uuid).findFirst();
                            if (toDelete != null) {
                                toDelete.deleteFromRealm();
                                // Optional: Delete local file associated with the user uuid
                                File imgFile = new File(activity.getExternalCacheDir(), uuid + ".jpeg");
                                if (imgFile.exists()) imgFile.delete();
                            }
                            realm.commitTransaction();
                        }
                    });
                    builder.setNegativeButton("No", null);
                    builder.show();
                }
            });
        }
    }
}