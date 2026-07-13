package nellas.labs;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;


public class MyApp extends Application {
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);

        // persistent storage for photos since it gets deleted sometimes
        PhotoHelper.migrateCachedPhotos(this);
    }
}

