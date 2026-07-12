package nellas.labs;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SessionSetAdapter extends RecyclerView.Adapter<SessionSetAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView setLabel;
        EditText setWeightInput;
        EditText setRepsInput;
        CheckBox setCompletedCheck;
        TextWatcher weightWatcher;
        TextWatcher repsWatcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            setLabel = itemView.findViewById(R.id.setLabel);
            setWeightInput = itemView.findViewById(R.id.setWeightInput);
            setRepsInput = itemView.findViewById(R.id.setRepsInput);
            setCompletedCheck = itemView.findViewById(R.id.setCompletedCheck);
        }
    }

    private SessionActivity activity;
    private List<SessionActivity.SetEntry> entries;

    public SessionSetAdapter(SessionActivity activity, List<SessionActivity.SetEntry> entries) {
        this.activity = activity;
        this.entries = entries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = activity.getLayoutInflater().inflate(R.layout.row_session_set, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final SessionActivity.SetEntry entry = entries.get(position);

        // Detach old listeners before re-binding so recycled rows don't write to the wrong entry
        if (holder.weightWatcher != null) {
            holder.setWeightInput.removeTextChangedListener(holder.weightWatcher);
        }
        if (holder.repsWatcher != null) {
            holder.setRepsInput.removeTextChangedListener(holder.repsWatcher);
        }
        holder.setCompletedCheck.setOnCheckedChangeListener(null);

        holder.setLabel.setText(entry.exerciseName + " — Set " + entry.setNumber);
        holder.setWeightInput.setText(entry.weightStr);
        holder.setRepsInput.setText(entry.repsStr);
        holder.setCompletedCheck.setChecked(entry.completed);

        holder.weightWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                entry.weightStr = s.toString();
            }
        };
        holder.setWeightInput.addTextChangedListener(holder.weightWatcher);

        holder.repsWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                entry.repsStr = s.toString();
            }
        };
        holder.setRepsInput.addTextChangedListener(holder.repsWatcher);

        holder.setCompletedCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                entry.completed = isChecked;
            }
        });
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }
}
