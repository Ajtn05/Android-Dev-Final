package nellas.labs;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

// Wires the shared Routines | Records | History | Photos bottom nav bar.
// Home is the hub: other screens finish themselves when navigating so
// the back stack always leads straight back to Home.
public class NavBar {

    public static final String EXTRA_OWNER_ID = "EXTRA_OWNER_ID";

    public static void wire(final Activity activity, final String ownerId) {
        final boolean isHome = activity instanceof HomeActivity;

        BottomNavigationView bottomNav = activity.findViewById(R.id.bottomNav);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_routines) {
                if (!isHome) {
                    activity.finish();
                    activity.overridePendingTransition(0, 0);
                }
                return true;
            }

            Class<?> target;
            if (id == R.id.nav_records) {
                target = RecordsActivity.class;
            } else if (id == R.id.nav_history) {
                target = HistoryActivity.class;
            } else if (id == R.id.nav_photos) {
                target = PhotosActivity.class;
            } else {
                return false;
            }

            if (!activity.getClass().equals(target)) {
                Intent intent = new Intent(activity, target);
                intent.putExtra(EXTRA_OWNER_ID, ownerId);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
                if (!isHome) {
                    activity.finish();
                }
            }
            return true;
        });

        refresh(activity);
    }

    // highlight the tab for the screen currently shown.
    public static void refresh(Activity activity) {
        BottomNavigationView bottomNav = activity.findViewById(R.id.bottomNav);
        if (activity instanceof HomeActivity) {
            bottomNav.setSelectedItemId(R.id.nav_routines);
        } else if (activity instanceof RecordsActivity) {
            bottomNav.setSelectedItemId(R.id.nav_records);
        } else if (activity instanceof HistoryActivity) {
            bottomNav.setSelectedItemId(R.id.nav_history);
        } else if (activity instanceof PhotosActivity) {
            bottomNav.setSelectedItemId(R.id.nav_photos);
        }
    }
}
