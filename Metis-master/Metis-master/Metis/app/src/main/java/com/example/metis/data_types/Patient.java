package com.example.metis.data_types;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static com.example.metis.utils.Consts.*;

/**
 * This class represents a patient
 */
public class Patient implements Serializable {

    private String id;
    private String therapist;
    private String fileNumber;
    private String hrz;
    private List<String> sensors;
    private List<String> series;
    private String cameraResolution;
    private String dominantHand;

    /**
     * This method returns a string representation of the patient.
     * @return patient string representation
     */
    @Override
    public String toString() {
        return "{" +
                ", id='" + id + '\'' +
                ", therapist='" + therapist + '\'' +
                ", fileNumber='" + fileNumber + '\'' +
                ", hrz='" + hrz + '\'' +
                ", sensors=" + sensors +
                ", series=" + series +
                '}';
    }

    public Patient(String hrz, String id, String fileNumber, List<String> sensors, List<String> series, String therapist) {
        this.hrz = hrz;
        this.id = id;
        this.fileNumber = fileNumber;
        this.sensors = sensors;
        this.series = series;
        this.therapist = therapist;
    }

    /**
     * Default constructor of patient object.
     */
    public Patient(){}

    /**
     * Constructor of patient.
     * @param json data json with all of the patient data.
     * @param therapist therapist of the patient.
     * @param fileNumber number of the patient file which is saved on phone storage.
     */
    public Patient(Map<String, Object> json, String therapist, String fileNumber) {
        this.therapist = therapist;
        this.fileNumber = fileNumber;

        Object tempObject = json.get(ID_KEYWORD);
        if (tempObject != null) {
            id = String.valueOf(tempObject);
        }

        tempObject = json.get(HRZ_KEYWORD);
        if (tempObject != null) {
            hrz = String.valueOf(tempObject);
        }

        tempObject = json.get(SENSORS_KEYWORD);
        if (tempObject != null) {
            sensors = (List<String>)(tempObject);
        }
        tempObject = json.get(VIDEOS_KEYWORD);
        if (tempObject != null) {
            series = (List<String>)(tempObject);
        }

        tempObject = json.get(CAMERA_RESOLUTION_KEYWORD);
        if (tempObject != null) {
            cameraResolution = tempObject.toString();
        }
        else {
            cameraResolution = CAMERA_RESOLUTION_480;
        }

        tempObject = json.get(DOMINANT_HAND_KEYWORD);
        if (tempObject != null) {
            dominantHand = tempObject.toString();
        }
        else {
            dominantHand = RIGHT_HAND_KEYWORD;
        }
    }

    /**
     * This function get return the camera resolution.
     * @return camera resolution.
     */
    public String getCameraResolution() {
        return cameraResolution;
    }

    /**
     * This function return list of selected sensors.
     * @return list of sensors.
     */
    public List<String> getSensors() {
        return sensors;
    }

    /**
     * This function chang the sensors list to another list.
     * @param sensors list of sensors.
     */
    public void setSensors(List<String> sensors) {
        this.sensors = sensors;
    }

    /**
     * This function return list of selected series.
     * @return list of series.
     */
    public List<String> getSeries() {
        return series;
    }

    /**
     * This function chang the series list to another list of series.
     * @param series list of series.
     */
    public void setSeries(List<String> series) {
        this.series = series;
    }

    /**
     * This function return the file number.
     * @return file number.
     */
    public String getFileNumber() {
        return fileNumber;
    }

    /**
     * This function chang the filer number to another file number.
     * @param fileNumber filer number.
     */
    public void setFileNumber(String fileNumber) {
        this.fileNumber = fileNumber;
    }

    /**
     * This function return the therapist name.
     * @return therapist name.
     */
    public String getTherapistName() {
        return therapist;
    }

    /**
     * This function chang the therapist name.
     * @param therapist therapist name.
     */
    public void setTherapist(String therapist) {
        this.therapist = therapist;
    }

    /**
     * This function return patient ID.
     * @return patient ID.
     */
    public String getPatientId() {
        return id;
    }

    /**
     * This function chang patient ID to another.
     * @param id patient ID.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * This function return the HRZ number.
     * @return HRZ number
     */
    public String getHrz() {
        return hrz;
    }

    /**
     * This function chang the HRZ number.
     * @param hrz number.
     */
    public void setHrz(String hrz) {
        this.hrz = hrz;
    }

    public String getDominantHand() {
        return dominantHand;
    }
}
