package nellas.labs;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Routine extends RealmObject {
    @PrimaryKey
    private String id;
    private String ownerId;
    private String name;
    private String targetFocus;
    private Date lastPerformed;
    private RealmList<Exercise> exercises;

    public Routine() {}
    public Routine(String id, String ownerId, String name, String targetFocus) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.targetFocus = targetFocus;
        this.lastPerformed = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTargetFocus() {
        return targetFocus;
    }

    public void setTargetFocus(String targetFocus) {
        this.targetFocus = targetFocus;
    }

    public Date getLastPerformed() {
        return lastPerformed;
    }

    public void setLastPerformed(Date lastPerformed) {
        this.lastPerformed = lastPerformed;
    }

    public RealmList<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(RealmList<Exercise> exercises) {
        this.exercises = exercises;
    }
}
