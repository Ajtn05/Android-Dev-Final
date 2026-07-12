package nellas.labs;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ProgressPhoto extends RealmObject {
    @PrimaryKey
    private String id;
    private String ownerId;
    private String photoPath;
    private Date dateCaptured;
    private double currentWeight;
    private String poseTag;
    private String notes;

    public ProgressPhoto() {}
    public ProgressPhoto(String id, String ownerId, String photoPath, Date dateCaptured, double currentWeight, String poseTag, String notes) {
        this.id = id;
        this.ownerId = ownerId;
        this.photoPath = photoPath;
        this.dateCaptured = dateCaptured;
        this.currentWeight = currentWeight;
        this.poseTag = poseTag;
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

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public Date getDateCaptured() {
        return dateCaptured;
    }

    public void setDateCaptured(Date dateCaptured) {
        this.dateCaptured = dateCaptured;
    }

    public double getCurrentWeight() {
        return currentWeight;
    }

    public void setCurrentWeight(double currentWeight) {
        this.currentWeight = currentWeight;
    }

    public String getPoseTag() {
        return poseTag;
    }

    public void setPoseTag(String poseTag) {
        this.poseTag = poseTag;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
