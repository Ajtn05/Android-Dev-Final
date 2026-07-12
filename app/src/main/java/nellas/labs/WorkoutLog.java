package nellas.labs;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class WorkoutLog extends RealmObject {
    @PrimaryKey
    private String id;
    private String ownerId;
    private String routineName;
    private Date dateCompleted;
    private double totalVolume;
    private String notes;
    private RealmList<SetLog> sets;

    public WorkoutLog() {}
    public WorkoutLog(String id, String ownerId, String routineName, Date dateCompleted, double totalVolume, String notes) {
        this.id = id;
        this.ownerId = ownerId;
        this.routineName = routineName;
        this.dateCompleted = dateCompleted;
        this.totalVolume = totalVolume;
        this.notes = notes;
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

    public String getRoutineName() {
        return routineName;
    }

    public void setRoutineName(String routineName) {
        this.routineName = routineName;
    }

    public Date getDateCompleted() {
        return dateCompleted;
    }

    public void setDateCompleted(Date dateCompleted) {
        this.dateCompleted = dateCompleted;
    }

    public double getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(double totalVolume) {
        this.totalVolume = totalVolume;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public RealmList<SetLog> getSets() {
        return sets;
    }

    public void setSets(RealmList<SetLog> sets) {
        this.sets = sets;
    }
}
