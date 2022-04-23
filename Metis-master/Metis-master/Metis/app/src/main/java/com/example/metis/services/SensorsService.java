package com.example.metis.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.metis.utils.EventWriter;
import com.example.metis.data_types.Patient;
import com.example.metis.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.metis.utils.Consts.*;

/**
 * SensorsService class is responsible for recording every sensor value change
 * through time. It stores the sensor data in a CSV file.
 */
@SuppressLint("Registered")
public class SensorsService extends Service implements SensorEventListener {

    private String TAG = this.getClass().getSimpleName();

    Logger logger = Logger.getInstance(APP_FILES_PATH);

    private SensorManager sensorManager;
    private List<Sensor> sensorList;
    private static File file;

    /**
     * This function defines what happens when the service is started.
     * @return int START_STICKY.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Patient patient = (Patient) intent.getSerializableExtra(PATIENT_KEYWORD);

        file = (File) intent.getSerializableExtra(SENSORS_FILE_PATH);

        // get sensors to track and data sampling rate from the patient info.
        int samplingRate = DEFAULT_SAMPLING_RATE;
        try {
            if (patient != null) {
                samplingRate = Integer.parseInt(patient.getHrz());
            }
        } catch (Exception e) {
            logger.writeLog(LogType.ERROR, TAG,
                    "onStartCommand. Could not parse patient sampling rate to integer: " + e.getLocalizedMessage());
        }
        logger.writeLog(LogType.INFO, TAG,
                "onStartCommand. Sensors sampling rate for patient: " + samplingRate);

        List<String> sensors = new ArrayList<>();
        if (patient != null) {
            sensors = patient.getSensors();
        }

        // create new sensors data file.
        try {
            file.createNewFile();
        } catch (IOException e) {
            logger.writeLog(LogType.ERROR, TAG,
                    "onStartCommand. Could not create sensors data file: " + e.getLocalizedMessage());
            e.printStackTrace();
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // register all available sensors to sensorManager listener with patient's sampling rate.
        sensorList = getSensorsList(sensors);
        for (int i = 0; i < sensorList.size(); i++) {
            sensorManager.registerListener(this, sensorList.get(i)
                    , ONE_SECOND_IN_MICROSECOND / samplingRate);
        }
        return START_STICKY;
    }

    /**
     * This method is used in order to get sensors list out of the
     * sensors string list of the patient.
     * @param sensors String list of sensors.
     * @return List<Sensor> list of sensors for tracking.
     */
    private List<Sensor> getSensorsList(List<String> sensors) {
        List<Sensor> chosenSensorsList = new ArrayList<>();
        List<Sensor> allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor tempSensor : allSensors) {
            if (sensors.contains(tempSensor.getName())) {
                chosenSensorsList.add(tempSensor);
            }
        }
        return chosenSensorsList;
    }

    /**
     * This function defines what happens when the service is destroyed.
     * Unregister all of the sensors from the sensor manager.
     */
    @Override
    public void onDestroy() {
        logger.writeLog(LogType.INFO, TAG, "onDestroy. Unregistering sensors");
        // unregister all of the sensors from sensorManager listener.
        for (Sensor s : sensorList) {
            sensorManager.unregisterListener(this, s);
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * This function defines what happens when registered sensors change.
     * It writes the changed sensors to the sensors data file.
     * @param event the event of the sensor change.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        String sensorsString = event.sensor.getName() + ",";
        for (float val : event.values) {
            sensorsString += val + ",";
        }
        logger.writeLog(LogType.INFO, TAG, "onSensorChanged. Sensor changed: " + sensorsString);
        EventWriter.writeString(sensorsString, file, TAG);
    }

    /**
     * This function defines what happens when registered sensors accuracy changes.
     * @param sensor Sensor which changed it's accuracy.
     * @param accuracy new accuracy of the sensor.
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        logger.writeLog(LogType.INFO, TAG, "onSensorChanged. Accuracy changed for sensor: " +
                sensor + " new accuracy: " + accuracy);
        System.out.println("Accuracy changed for sensor:" + sensor.getName());
    }
}
