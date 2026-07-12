package nellas.labs;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Exercise extends RealmObject {
    @PrimaryKey
    private String id;
    private String ownerId;
    private String name;
    private double targetWeight;
    private int targetReps;
    private int targetSets;

    public Exercise() {}
    public Exercise(String id, String ownerId, String name, double targetWeight, int targetReps, int targetSets) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.targetWeight = targetWeight;
        this.targetReps = targetReps;
        this.targetSets = targetSets;
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

    public double getTargetWeight() {
        return targetWeight;
    }

    public void setTargetWeight(double targetWeight) {
        this.targetWeight = targetWeight;
    }

    public int getTargetReps() {
        return targetReps;
    }

    public void setTargetReps(int targetReps) {
        this.targetReps = targetReps;
    }

    public int getTargetSets() {
        return targetSets;
    }

    public void setTargetSets(int targetSets) {
        this.targetSets = targetSets;
    }
}
