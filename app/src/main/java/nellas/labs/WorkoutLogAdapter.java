package nellas.labs;

import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;

public class WorkoutLogAdapter extends RealmRecyclerViewAdapter<WorkoutLog, WorkoutLogAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView rowLogRoutine;
        TextView rowLogDate;
        TextView rowLogVolume;
        TextView rowLogNotes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rowLogRoutine = itemView.findViewById(R.id.rowLogRoutine);
            rowLogDate = itemView.findViewById(R.id.rowLogDate);
            rowLogVolume = itemView.findViewById(R.id.rowLogVolume);
            rowLogNotes = itemView.findViewById(R.id.rowLogNotes);
        }
    }

    private HistoryActivity activity;
    private Realm realm;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US);

    public WorkoutLogAdapter(HistoryActivity activity, @Nullable OrderedRealmCollection<WorkoutLog> data, boolean autoUpdate, Realm realm) {
        super(data, autoUpdate);
        this.activity = activity;
        this.realm = realm;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = activity.getLayoutInflater().inflate(R.layout.row_workout_log, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutLog log = getItem(position);

        if (log != null) {
            final String logId = log.getId();
            final String logOwnerId = log.getOwnerId();

            holder.rowLogRoutine.setText(log.getRoutineName());
            holder.rowLogDate.setText(dateFormat.format(log.getDateCompleted()));
            holder.rowLogVolume.setText("Total volume: " + formatNumber(log.getTotalVolume()) + " lbs");
            String notes = log.getNotes();
            holder.rowLogNotes.setText((notes == null || notes.isEmpty()) ? "" : "Notes: " + notes);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDetailDialog(logId, logOwnerId);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    confirmDelete(logId, logOwnerId);
                    return true;
                }
            });
        }
    }

    private void showDetailDialog(String logId, String logOwnerId) {
        WorkoutLog log = realm.where(WorkoutLog.class)
                .equalTo("ownerId", logOwnerId)
                .equalTo("id", logId)
                .findFirst();
        if (log == null) {
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append(dateFormat.format(log.getDateCompleted()));
        message.append("\nTotal volume: ").append(formatNumber(log.getTotalVolume())).append(" lbs\n");
        String notes = log.getNotes();
        if (notes != null && !notes.isEmpty()) {
            message.append("Notes: ").append(notes).append("\n");
        }
        message.append("\nSets:\n");
        for (SetLog set : log.getSets()) {
            message.append(set.getExerciseName())
                    .append(" — ").append(formatNumber(set.getWeight())).append(" lbs × ").append(set.getReps())
                    .append(set.isCompleted() ? " ✓" : " ✗")
                    .append("\n");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(log.getRoutineName());
        builder.setMessage(message.toString());
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void confirmDelete(final String logId, final String logOwnerId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this workout log?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                realm.beginTransaction();
                WorkoutLog toDelete = realm.where(WorkoutLog.class)
                        .equalTo("ownerId", logOwnerId)
                        .equalTo("id", logId)
                        .findFirst();
                if (toDelete != null) {
                    toDelete.getSets().deleteAllFromRealm();
                    toDelete.deleteFromRealm();
                }
                realm.commitTransaction();
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private String formatNumber(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}
