package nellas.labs;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView rowRecordName;
        TextView rowRecordDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rowRecordName = itemView.findViewById(R.id.rowRecordName);
            rowRecordDetail = itemView.findViewById(R.id.rowRecordDetail);
        }
    }

    private RecordsActivity activity;
    private List<RecordsActivity.RecordEntry> records;

    public RecordAdapter(RecordsActivity activity, List<RecordsActivity.RecordEntry> records) {
        this.activity = activity;
        this.records = records;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = activity.getLayoutInflater().inflate(R.layout.row_record, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final RecordsActivity.RecordEntry entry = records.get(position);

        holder.rowRecordName.setText(entry.exerciseName);
        holder.rowRecordDetail.setText(activity.formatRecordDetail(entry));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.showSourceWorkout(entry);
            }
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }
}
