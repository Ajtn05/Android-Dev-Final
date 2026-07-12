package nellas.labs;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;

public class RoutineAdapter extends RealmRecyclerViewAdapter<Routine, RoutineAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView rowRoutineName;
        TextView rowFocus;
        TextView rowLastTracked;
        Button btnEdit;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rowRoutineName = itemView.findViewById(R.id.rowRoutineName);
            rowFocus = itemView.findViewById(R.id.rowFocus);
            rowLastTracked = itemView.findViewById(R.id.rowLastTracked);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    private HomeActivity activity;
    private Realm realm;

    public RoutineAdapter(HomeActivity activity, @Nullable OrderedRealmCollection<Routine> data, boolean autoUpdate, Realm realm) {
        super(data, autoUpdate);
        this.activity = activity;
        this.realm = realm;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = activity.getLayoutInflater().inflate(R.layout.row_routine, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Routine r = getItem(position);

        if (r != null) {
            final String routineId = r.getId();
            final String routineOwnerId = r.getOwnerId();

            holder.rowRoutineName.setText(r.getName());
            holder.rowFocus.setText(r.getTargetFocus());
            holder.rowLastTracked.setText(lastTrackedText(r.getLastPerformed()));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, SessionActivity.class);
                    intent.putExtra("ROUTINE_ID", routineId);
                    activity.startActivity(intent);
                }
            });

            holder.btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, RoutineEditActivity.class);
                    intent.putExtra("EDIT_ROUTINE_ID", routineId);
                    activity.startActivity(intent);
                }
            });

            holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("Confirm Delete");
                    builder.setMessage("Are you sure you want to delete this routine?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            realm.beginTransaction();
                            Routine toDelete = realm.where(Routine.class)
                                    .equalTo("ownerId", routineOwnerId)
                                    .equalTo("id", routineId)
                                    .findFirst();
                            if (toDelete != null) {
                                toDelete.getExercises().deleteAllFromRealm();
                                toDelete.deleteFromRealm();
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

    private String lastTrackedText(Date lastPerformed) {
        if (lastPerformed == null) {
            return "Never tracked";
        }
        long days = (System.currentTimeMillis() - lastPerformed.getTime()) / (1000L * 60 * 60 * 24);
        if (days <= 0) {
            return "Last tracked: today";
        } else if (days == 1) {
            return "Last tracked: 1 day ago";
        } else {
            return "Last tracked: " + days + " days ago";
        }
    }
}
