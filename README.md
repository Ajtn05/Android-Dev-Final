# GainzTracker

A gym workout & physique tracking Android app built with Java, Realm, and RecyclerView.

## Features

- **Build & manage workout routines** — define exercises with target weight, reps, sets
- **Log live workout sessions** — track completed sets, take photos mid-workout
- **View personal records** — aggregate max weight per exercise across all workouts
- **Workout history** — browse past sessions with detailed set logs
- **Progress photos** — capture bodyweight and physique photos, compare side-by-side over time
- **Multi-user support** — each user's data is completely partitioned; no data bleed

## Quick Setup

### Prerequisites

- Android Studio (latest)
- Android SDK installed (API 36+)

### First-Time Setup

1. **Clone the repo**
   ```bash
   git clone https://github.com/Ajtn05/Android-Dev-Final.git
   cd Android-Dev-Final
   ```

2. **Configure your SDK path**
   ```bash
   cp local.properties.example local.properties
   ```
   Then edit `local.properties` and set your SDK path:
   - **macOS:** `sdk.dir=/Users/YOUR_USERNAME/Library/Android/Sdk`
   - **Windows:** `sdk.dir=C:\Users\YOUR_USERNAME\AppData\Local\Android\Sdk`
   - **Linux:** `sdk.dir=/home/YOUR_USERNAME/Android/Sdk`

   (You can find your SDK path in Android Studio: Settings > SDK Manager > SDK Location)

3. **Open in Android Studio**
   - File → Open → select this folder
   - Android Studio will sync Gradle and download dependencies

4. **Build & Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or use Android Studio's Run button (Shift+F10)

## Project Structure

```
app/src/main/
├── java/nellas/labs/
│   ├── User, Exercise, Routine, SetLog, WorkoutLog, ProgressPhoto  (Realm models)
│   ├── LoginActivity, RegisterActivity, AdminActivity              (auth & user mgmt)
│   ├── HomeActivity, RoutineEditActivity, SessionActivity          (routine workflow)
│   ├── RecordsActivity, HistoryActivity                            (stats & logs)
│   ├── PhotosActivity, PhotoViewerActivity                         (progress tracking)
│   ├── *Adapter classes                                            (RecyclerView adapters)
│   └── PhotoHelper, NavBar                                         (shared utilities)
├── res/layout/
│   ├── activity_*.xml                                              (screen layouts)
│   ├── row_*.xml, cell_*.xml                                       (list/grid rows)
│   └── view_bottom_nav.xml                                         (shared navigation bar)
└── AndroidManifest.xml

gradle/
├── wrapper/                                   (gradle build tool)
build.gradle, settings.gradle, gradle.properties
local.properties.example                      (copy to local.properties)
```

## Tech Stack

- **Java 11** (no Kotlin)
- **Realm Classic 10.19.0** — local NoSQL database
- **RecyclerView + RealmRecyclerViewAdapter** — efficient lists
- **Picasso 2.71828** — image loading
- **Dexter 6.2.3** — runtime permissions (camera, storage)
- **Android Image Cropper** — photo crop & resize
- **Material Design 1.14.0** — UI components

## Key Constraints

- All user data is partitioned by `ownerId` (no cross-user data bleed)
- Every Realm query for app data must filter `.equalTo("ownerId", currentUserUuid)`
- Realm database is local; resets on schema version changes (`deleteRealmIfMigrationNeeded()`)
- Photos are saved to `getExternalCacheDir()` as JPEG files

## Testing Multi-User Setup

1. Register two different users
2. Log in as User A → build a routine, run a session, add a progress photo
3. Log out / clear SharedPreferences
4. Log in as User B → verify you see NONE of User A's data

## Troubleshooting

**Build fails with "SDK not found"**
- Check `local.properties`: does the `sdk.dir` path exist on your machine?
- Run `./gradlew --version` to verify Gradle can find the SDK

**Gradle sync fails**
- Make sure you have Android SDK API 36 installed (Android Studio → SDK Manager → Platforms)

**Camera/gallery not working**
- Grant permissions when the app prompts
- On Android 6+, runtime permissions are requested via Dexter

## License

School project — final Android development assignment.
