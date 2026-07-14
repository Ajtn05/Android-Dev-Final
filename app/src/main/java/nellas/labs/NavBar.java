package nellas.labs;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

// Wires the shared Routines | Records | History | Pics button bar.
// Home is the hub: other screens finish themselves when navigating so
// the back stack always leads straight back to Home.
public class NavBar {


    public static void wire(final Activity activity) {
        final boolean isHome = activity instanceof HomeActivity;

        Button btnRoutines = activity.findViewById(R.id.btnNavRoutines);
        btnRoutines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isHome) {
                    activity.finish();
                }
            }
        });

        Button btnRecords = activity.findViewById(R.id.btnNavRecords);
        btnRecords.setOnClickListener(navTo(activity, RecordsActivity.class, isHome));

        Button btnHistory = activity.findViewById(R.id.btnNavHistory);
        btnHistory.setOnClickListener(navTo(activity, HistoryActivity.class, isHome));

        Button btnPics = activity.findViewById(R.id.btnNavPics);
        btnPics.setOnClickListener(navTo(activity, PhotosActivity.class, isHome));

        // highlight the tab for the screen currently shown.
        btnRoutines.setSelected(isHome);
        btnRecords.setSelected(activity instanceof RecordsActivity);
        btnHistory.setSelected(activity instanceof HistoryActivity);
        btnPics.setSelected(activity instanceof PhotosActivity);
    }

    //usual intent implementation but if-elsed to avoid duplication
    private static View.OnClickListener navTo(final Activity activity, final Class<?> target, final boolean isHome) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity.getClass().equals(target)) {
                    return;
                }
                activity.startActivity(new Intent(activity, target));
                if (!isHome) {
                    activity.finish();
                }
            }
        };
    }
}
