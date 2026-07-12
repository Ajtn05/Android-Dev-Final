package nellas.labs;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class PhotoAdapter extends RealmRecyclerViewAdapter<ProgressPhoto, PhotoAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cellPhotoImage;
        TextView cellPhotoDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cellPhotoImage = itemView.findViewById(R.id.cellPhotoImage);
            cellPhotoDate = itemView.findViewById(R.id.cellPhotoDate);
        }
    }

    private PhotosActivity activity;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);

    public PhotoAdapter(PhotosActivity activity, @Nullable OrderedRealmCollection<ProgressPhoto> data, boolean autoUpdate) {
        super(data, autoUpdate);
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = activity.getLayoutInflater().inflate(R.layout.cell_photo, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProgressPhoto photo = getItem(position);

        if (photo != null) {
            final String photoId = photo.getId();

            File file = new File(activity.getExternalCacheDir(), photo.getPhotoPath());
            if (file.exists()) {
                Picasso.get()
                        .load(file)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .into(holder.cellPhotoImage);
            } else {
                holder.cellPhotoImage.setImageResource(R.drawable.ic_placeholder);
            }

            holder.cellPhotoDate.setText(dateFormat.format(photo.getDateCaptured()));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, PhotoViewerActivity.class);
                    intent.putExtra("PHOTO_ID", photoId);
                    activity.startActivity(intent);
                }
            });
        }
    }
}
