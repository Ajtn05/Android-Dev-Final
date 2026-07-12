package nellas.labs;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import io.realm.Realm;

// Shared save path for progress photos: write the cropped JPEG to the external
// cache dir, then collect bodyweight/pose/notes before creating the ProgressPhoto.
public class PhotoHelper {

    public static void promptAndSave(final Activity activity, final Realm realm,
                                     final byte[] jpeg, final String ownerId) {
        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_photo_details, null);
        final EditText weightInput = dialogView.findViewById(R.id.photoWeightInput);
        final EditText poseInput = dialogView.findViewById(R.id.photoPoseInput);
        final EditText notesInput = dialogView.findViewById(R.id.photoNotesInput);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Progress Photo Details");
        builder.setView(dialogView);
        builder.setPositiveButton("Save", null);
        builder.setNegativeButton("Cancel", null);

        final AlertDialog dialog = builder.create();
        dialog.show();

        // Positive button set manually so invalid input keeps the dialog open
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String weightStr = weightInput.getText().toString().trim();
                String pose = poseInput.getText().toString().trim();
                String notes = notesInput.getText().toString().trim();

                if (weightStr.isEmpty()) {
                    Toast.makeText(activity, "Enter your current bodyweight", Toast.LENGTH_SHORT).show();
                    return;
                }
                double weight;
                try {
                    weight = Double.parseDouble(weightStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(activity, "Bodyweight must be a number", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (pose.isEmpty()) {
                    pose = "Untagged";
                }

                String filename = "photo_" + UUID.randomUUID().toString() + ".jpeg";
                try {
                    File savedImage = new File(activity.getExternalCacheDir(), filename);
                    FileOutputStream fos = new FileOutputStream(savedImage);
                    fos.write(jpeg);
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(activity, "Failed to save photo file", Toast.LENGTH_SHORT).show();
                    return;
                }

                realm.beginTransaction();
                ProgressPhoto photo = realm.createObject(ProgressPhoto.class, UUID.randomUUID().toString());
                photo.setOwnerId(ownerId);
                photo.setPhotoPath(filename);
                photo.setDateCaptured(new Date());
                photo.setCurrentWeight(weight);
                photo.setPoseTag(pose);
                photo.setNotes(notes);
                realm.commitTransaction();

                Toast.makeText(activity, "Progress photo saved!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }
}
