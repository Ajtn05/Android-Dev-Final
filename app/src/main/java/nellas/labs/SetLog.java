package nellas.labs;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class SetLog extends RealmObject {
    @PrimaryKey
    private String id;
    private String ownerId;
    private String exerciseName;
    private double weight;
    private int reps;
    private boolean completed;

    public SetLog() {}
    public SetLog(String id, String ownerId, String exerciseName, double weight, int reps, boolean completed) {
        this.id = id;
        this.ownerId = ownerId;
        this.exerciseName = exerciseName;
        this.weight = weight;
        this.reps = reps;
        this.completed = completed;
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

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
