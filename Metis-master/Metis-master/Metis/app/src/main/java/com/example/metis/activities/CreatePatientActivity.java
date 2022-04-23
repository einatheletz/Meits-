package com.example.metis.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.metis.data_types.Patient;
import com.example.metis.R;
import com.example.metis.utils.ApplicationDataManager;
import com.example.metis.utils.JsonToMap;
import com.example.metis.utils.Logger;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.example.metis.utils.Consts.*;

/**
 * CreatePatientActivity class is responsible for enabling
 * the user to create and edit patients.
 */
public class CreatePatientActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private String TAG = CreatePatientActivity.class.getSimpleName();

    private Logger logger = Logger.getInstance(APP_FILES_PATH);

    private LinearLayout linearLayout;
    private List<List<CheckBox>> checkBoxesList = new ArrayList<>();
    private EditText idEditText;
    private EditText hrzEditText;
    private String userName;

    private Button createButton;
    private Button nextButton1;
    private Button selectAllButton;
    private Button nextButton2;
    private Button defaultButton;

    private TextInputLayout hrzLayout;

    private Spinner cameraResolutionSpinner;
    private List<String> cameraResolutionList;
    private ArrayAdapter<String> cameraResolutionAdapter;
    private String resolutionPick = "";
    private ArrayList<String> patients;

    private String dominantHand = RIGHT_HAND_KEYWORD;
    private boolean isSelect = true;

    private List<Sensor> sensorList;

    Patient patient = null;
    DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_create_patient);

        logger.writeLog(LogType.INFO, TAG, "Inside CreatePatientActivity");

        nextButton1 = findViewById(R.id.next_button1);
        nextButton1.setOnClickListener(this);

        idEditText = findViewById(R.id.input_id);

        initializeSpinner();

//        isLeftHandedCheckBox = findViewById(R.id.is_left_hand_check_box);

        Intent intent = getIntent();
        createCheckBoxes();
        if (ApplicationDataManager.checkIfEditMode()) {
            patient = (Patient) intent.getSerializableExtra(PATIENT_KEYWORD);
            logger.writeLog(LogType.INFO, TAG, "Editing patient: " + patient.toString());
            if (patient != null) {
                userName = patient.getTherapistName();
                enterToEditMode(patient);
            } else {
                logger.writeLog(LogType.ERROR, TAG, "onCreate. Patient is null.");
            }
        } else {
            userName = (String) intent.getSerializableExtra(USER_NAME_KEYWORD);
            patients = (ArrayList<String>) intent.getSerializableExtra(PATIENTS_KEYWORD);
        }
    }

    /**
     * Initialize a Spinner layout for choosing the video resolution.
     */
    private void initializeSpinner() {
        logger.writeLog(LogType.DEBUG, TAG, "Initialize user resolution's Spinner.");
        cameraResolutionSpinner = findViewById(R.id.camera_resolution_spinner);
        cameraResolutionSpinner.setOnItemSelectedListener(this);
        cameraResolutionList = createCameraResolutionList();
        cameraResolutionAdapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_spinner_item, cameraResolutionList);
        cameraResolutionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data cameraResolutionAdapter to spinner
        cameraResolutionSpinner.setAdapter(cameraResolutionAdapter);
    }

    /**
     * Create a list of camera resolutions values
     * @return camera resolutions values
     */
    private List<String> createCameraResolutionList() {
        return Arrays.asList(new String[]{CAMERA_RESOLUTION_480, CAMERA_RESOLUTION_720, CAMERA_RESOLUTION_1080});
    }

    /**
     * This method enters edit mode for the given patient.
     * It is used in order to change patient information:
     * series allowed, sensors, sampling rate.
     *
     * @param patient given patient for editing.
     */
    private void enterToEditMode(Patient patient) {
        logger.writeLog(LogType.DEBUG, TAG, "Entering into edit mode.");
        idEditText.setEnabled(false);
        idEditText.setText(patient.getPatientId());
        cameraResolutionSpinner.setSelection(cameraResolutionList.indexOf(patient.getCameraResolution()));
        checkCheckBoxes(patient);
        markDominantHand(patient.getDominantHand());
    }

    /**
     * This function mark the dominant hand for the patient is edited.
     * @param dominantHand button to mark
     */
    private void markDominantHand(String dominantHand) {
        switch (dominantHand) {
            case RIGHT_HAND_KEYWORD:
                findViewById(R.id.right_radio_button).performClick();
                break;
            case LEFT_HAND_KEYWORD:
                findViewById(R.id.left_radio_button).performClick();
                break;
        }
    }

    /**
     * Initialize check boxes for choosing series and sensors.
     * @param checkBoxTexts the check boxes texts
     */
    private void initializeCheckBoxes(List<String> checkBoxTexts) {
        logger.writeLog(LogType.DEBUG, TAG, "Initializing the series folders check boxes.");
        List<CheckBox> tempCheckBoxList = new ArrayList<>();
        for (int i = 0; i < checkBoxTexts.size(); i++) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setId(i);
            checkBox.setText(checkBoxTexts.get(i));
            checkBox.setTextSize(25);
            tempCheckBoxList.add(checkBox);
        }
        checkBoxesList.add(tempCheckBoxList);
    }

    /**
     * On edit mode, mark the check boxes as marked on the patient's creation.
     * @param tempCheckBoxList the patient
     */
    private void checkCheckBoxes(Patient tempCheckBoxList) {
        markCheckBoxes(tempCheckBoxList.getSeries(), checkBoxesList.get(0));
        markCheckBoxes(tempCheckBoxList.getSensors(), checkBoxesList.get(1));
    }

    /**
     * Mark the check boxes received.
     * @param checkBoxesNames the patient's check boxes
     * @param allCheckBoxes the existing check boxes
     */
    private void markCheckBoxes(List<String> checkBoxesNames, List<CheckBox> allCheckBoxes) {
        for (String currentCheckBox : checkBoxesNames) {
            for (CheckBox tempCheckBox : allCheckBoxes) {
                if (currentCheckBox.equals(tempCheckBox.getText())) {
                    tempCheckBox.setChecked(true);
                }
            }
        }
    }

    /**
     * Creating the videos and sensors check boxes.
     */
    public void createCheckBoxes() {
        //initializing the series check boxes.
        logger.writeLog(LogType.DEBUG, TAG, "Creating the check boxes.");
        initializeCheckBoxes(getSeriesFoldersNames());

        logger.writeLog(LogType.DEBUG, TAG, "Getting all of the sensors of the current system.");
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);

        initializeCheckBoxes(createSensorsList(sensorList));
        logger.writeLog(LogType.DEBUG, TAG, "Finished creating the check boxes.");
    }

    /**
     * on button click function.
     * @param v     the button clicked
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next_button1:
                logger.writeLog(LogType.INFO, TAG, "Next button clicked. Moving to series choose page.");
                if (checkForm()) {
                    moveToVideosFormPage();
                }
                break;
            case R.id.next_button2:
                logger.writeLog(LogType.INFO, TAG, "Next button clicked. Moving to sensors choose page.");
                moveToFinalFormPage();
                break;
            case R.id.create_new_button:
                logger.writeLog(LogType.INFO, TAG, "Create button clicked. Adding a new patient");
                createPatient();
                break;
            case R.id.select_all_button:
                if (isSelect) {
                    logger.writeLog(LogType.INFO, TAG, "Select all sensors button clicked.");
                    selectAll();
                }
                else {
                    logger.writeLog(LogType.INFO, TAG, "Unselect all sensors button clicked.");
                    unselectAll();
                }
                break;
            case R.id.default_button:
                logger.writeLog(LogType.INFO, TAG, "Default sensors button clicked. ");
                chooseDefaultSensors(); // TODO: when changing phone / version upgrade use the uncomment code instead of the hardcoded.
                break;
        }
    }

    /**
     * Checking if the ID field is legal.
     * @return  true if it is legal. false otherwise
     */
    private boolean checkForm() {
        if (!ApplicationDataManager.checkIfEditMode()) {
            logger.writeLog(LogType.INFO, TAG, "Checking if the patient creating form is legal.");
            String id = String.valueOf(idEditText.getText());
            if (id.equals("")) {
                logger.writeLog(LogType.INFO, TAG, "The ID field is empty, cannot continue with the patient creation.");
                ApplicationDataManager.getErrorMessageDialog(this, "You have to enter an ID.", null);
                return false;
            } else if (checkIfIdExist(id)) {
                logger.writeLog(LogType.INFO, TAG, "The ID entered already exist.");
                ApplicationDataManager.getErrorMessageDialog(this, "Patient already exist. \nPlease change the ID entered or return to the previous screen.", null);
                return false;
            }
        }
        return true;
    }

    /**
     * This method checks if a given patient ID already exists in files.
     * @param id patient ID
     * @return true if already exists, else false.
     */
    private boolean checkIfIdExist(String id) {
        return patients.contains(id);
    }

    /**
     * This method is used in order to unselect all of the checkboxes.
     */
    private void unselectAll() {
        logger.writeLog(LogType.DEBUG, TAG, "Unmarking all check boxes.");
        selectAllButton.setText("Select All");
        int checkBoxesListIndex = 0;
        if (!nextButton2.isClickable()) {
            checkBoxesListIndex = 1;
        }
        for (CheckBox checkBox : checkBoxesList.get(checkBoxesListIndex)) {
            checkBox.setChecked(false);
        }
        isSelect = true;
    }

    /**
     * Choosing all default sensors defined in "Consts" class.
     */
    private void chooseDefaultSensors() {
        logger.writeLog(LogType.DEBUG, TAG, "Marking all default sensors's check boxes defined.");
        String sensorName;
        for (Sensor sensor : sensorList) {
            sensorName = sensor.getName().toLowerCase();
            for (String defaultSensor : DEFAULT_SENSORS_LIST) {
                if (Pattern.compile(defaultSensor.toLowerCase()).matcher(sensorName).find()) {
                    for (CheckBox checkBox : checkBoxesList.get(1)) {
                        if (Pattern.compile(defaultSensor.toLowerCase()).matcher(checkBox.getText().toString().toLowerCase()).find()){
                            checkBox.setChecked(true);
                        }
                    }
                }
            }
        }
    }

    /**
     * On RadioButton click function
     * @param v the radio button clicked
     */
    public void onRadioButtonClick(View v) {
        if(((RadioButton)v).isChecked()) {
            switch (v.getId()) {
                case R.id.right_radio_button:
                        dominantHand = RIGHT_HAND_KEYWORD;
                    break;
                case R.id.left_radio_button:
                        dominantHand = LEFT_HAND_KEYWORD;
                    break;
            }
        }
    }

    /**
     * This method is used in order to select all of the checkboxes.
     */
    private void selectAll() {
        logger.writeLog(LogType.DEBUG, TAG, "Marking all check boxes as selected.");
        selectAllButton.setText("Unselect All");
        int checkBoxesListIndex = 0;
        if (!nextButton2.isClickable()) {
            checkBoxesListIndex = 1;
        }
        for (CheckBox checkBox : checkBoxesList.get(checkBoxesListIndex)) {
            checkBox.setChecked(true);
        }
        isSelect = false;
    }

    /**
     * This function is preparing the page for displaying the series pick.
     * The function disables all unnecessary UI components and adds the relevant ones.
     */
    private void moveToVideosFormPage() {
        logger.writeLog(LogType.DEBUG, TAG, "Initialize series pick UI components.");
        setContentView(R.layout.activity_create_patient_2);
        createButton = findViewById(R.id.create_new_button);
        createButton.setOnClickListener(this);
        nextButton2 = findViewById(R.id.next_button2);
        nextButton2.setOnClickListener(this);
        selectAllButton = findViewById(R.id.select_all_button);
        selectAllButton.setOnClickListener(this);
        linearLayout = findViewById(R.id.sensors_list_linear_layout);
        defaultButton = findViewById(R.id.default_button);
        defaultButton.setOnClickListener(this);

        hrzLayout = findViewById(R.id.hrz_layout);
        hrzEditText = findViewById(R.id.input_hrz);
        hrzLayout.setVisibility(View.INVISIBLE);

        nextButton2.setClickable(true);
        nextButton2.setVisibility(View.VISIBLE);

        createButton.setVisibility(View.INVISIBLE);
        createButton.setClickable(false);
        selectAllButton.setVisibility(View.INVISIBLE);
        selectAllButton.setClickable(false);
        defaultButton.setVisibility(View.INVISIBLE);
        defaultButton.setClickable(false);

        addCheckBoxesToLayout(0);

        linearLayout.setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.title_form)).setText("Choose series to display:");
    }

    /**
     * The function adds check boxes to the layout.
     * @param i     index of the list of the check boxes to add
     */
    private void addCheckBoxesToLayout(int i) {
        logger.writeLog(LogType.DEBUG, TAG, "Adding the following check boxes to the view: " + checkBoxesList.get(i));
        for (CheckBox checkBox : checkBoxesList.get(i)) {
            linearLayout.addView(checkBox);
        }
    }

    /**
     * This function finds every existing folder name.
     * @return  list of all the folder's names
     */
    private List<String> getSeriesFoldersNames() {
        logger.writeLog(LogType.DEBUG, TAG, "Finding all the series folders names");
        List<String> seriesNames = new ArrayList<>();
        try {
            File[] folder = new File(APP_FILES_PATH + "/" + SERIES_KEYWORD).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });
            if (folder == null) {
                return new ArrayList<>();
            }
            seriesNames = Arrays.stream(folder).map(File::getName).collect(Collectors.toList());
        } catch (Exception e){
            logger.writeLog(LogType.ERROR, TAG, "Failed reading series folders. Exception " + e + "thrown.");
            ApplicationDataManager.getErrorMessageDialog(this, BASIC_ERROR_MESSAGE, errorListener);
        }
        return seriesNames;
    }

    /**
     * This function is preparing the page for displaying the sensors pick.
     * The function disables all unnecessary UI components and adds the relevant ones.
     */
    private void moveToFinalFormPage() {
        logger.writeLog(LogType.DEBUG, TAG, "Initialize sensors pick UI components.");
        if(patient != null && patient.getSensors() != null && sensorList.size() == patient.getSensors().size()) {
            selectAllButton.setText("Unselect All");
            isSelect = false;
        }
        if (ApplicationDataManager.checkIfEditMode()) {
            hrzEditText.setText(patient.getHrz());
        }
        else {
            hrzEditText.setText("100");
        }
        hrzLayout.setVisibility(View.VISIBLE);
        linearLayout.removeAllViews();
        nextButton2.setVisibility(View.INVISIBLE);
        nextButton2.setClickable(false);
        createButton.setVisibility(View.VISIBLE);
        createButton.setClickable(true);

        selectAllButton.setVisibility(View.VISIBLE);
        selectAllButton.setClickable(true);
        defaultButton.setVisibility(View.VISIBLE);
        defaultButton.setClickable(true);

        addCheckBoxesToLayout(1);
        linearLayout.setVisibility(View.VISIBLE);

        ((TextView) findViewById(R.id.title_form)).setText("Choose sensors to monitor:");
    }

    /**
     * Creating a list of all sensors names.
     * @param sensorList    the current device sensors list
     * @return              list of the sensor's names
     */
    private List<String> createSensorsList(List<Sensor> sensorList) {
        logger.writeLog(LogType.DEBUG, TAG, "Creating sensors list.");
        List<String> sensors = new ArrayList<>();
        for (Sensor sensor : sensorList) {
            sensors.add(sensor.getName());
        }
        return sensors;
    }

    /**
     * This function is the last step of the patient creation.
     * The function gets all the data from the therapist, creates
     * new patient's JSON file and updates the therapist's file.
     */
    private void createPatient() {
        logger.writeLog(LogType.DEBUG, TAG, "Creating new patient.");
        List<List<String>> checkedSensors = findCheckedSensors();
        JSONObject patientJson = new JSONObject();
        try {
            patientJson.put(ID_KEYWORD, idEditText.getText().toString());
            patientJson.put(HRZ_KEYWORD, hrzEditText.getText().toString());
            patientJson.put(VIDEOS_KEYWORD, new JSONArray(checkedSensors.get(0)));
            patientJson.put(SENSORS_KEYWORD, new JSONArray(checkedSensors.get(1)));
            patientJson.put(CAMERA_RESOLUTION_KEYWORD, resolutionPick);
            patientJson.put(DOMINANT_HAND_KEYWORD, dominantHand);
            Context context = getApplicationContext();
            String jsonCounter = ApplicationDataManager.getJsonCounter();
            if (jsonCounter == null) {
                logger.writeLog(LogType.ERROR, TAG, "An error has occurred while updating the ApplicationData file");
                finish();
            }
            String message = "The patient " + new Patient(JsonToMap.toMap(patientJson), userName, jsonCounter).toString() + " has been ";
            if (ApplicationDataManager.checkIfEditMode()) {
                message += "edited successfully.";
                ApplicationDataManager.exitEditMode();
            } else {
                message += "created successfully.";
            }
            logger.writeLog(LogType.INFO, TAG, message);
            logger.writeLog(LogType.DEBUG, TAG, "Moving back to PatientsListActivity");
            // checks if a patient file is exist. if not, createPatient one. if exist, update it with the new data
            if (updateOrCreatePatientAndTherapistFiles(patientJson, context, jsonCounter)) {
                Intent intent = new Intent(this, PatientsListActivity.class);
                intent.putExtra(USER_NAME_KEYWORD, (Serializable) userName);
                startActivity(intent);
            } else {
                logger.writeLog(LogType.ERROR, this.getClass().getSimpleName(), "an error occurred while updating or creating therapist file");
                    ApplicationDataManager.getErrorMessageDialog(this, BASIC_ERROR_MESSAGE, errorListener);

            }
        } catch (JSONException e) {
            logger.writeLog(LogType.ERROR, this.getClass().getSimpleName(),
                    "an error occurred while updating or creating therapist file." + "Exception " + e + " is thrown.");
                ApplicationDataManager.getErrorMessageDialog(this, BASIC_ERROR_MESSAGE, errorListener);
        }
    }

    /**
     * the function creates a patient file and update (or create if needed) a therapist file.
     * @param patientJson   new patient JSON
     * @param context       application context
     * @param jsonCounter   name of the patient's file
     * @return
     */
    private boolean updateOrCreatePatientAndTherapistFiles(JSONObject patientJson, Context context, String jsonCounter) {
        // createPatient patient file and update therapist file
            return (ApplicationDataManager.writeNewFile(PATIENT_PREFIX_KEYWORD + jsonCounter + JSON_FILE_EXTENSION_KEYWORD, patientJson.toString())) &&
                (ApplicationDataManager.writeTherapistFile(userName + JSON_FILE_EXTENSION_KEYWORD,
                        idEditText.getText().toString(), jsonCounter));
    }

    /**
     * This method is used in order to get all of
     * the sensors which were checked by the user.
     *
     * @return list of sensors which were checked by user.
     */
    private List<List<String>> findCheckedSensors() {
        logger.writeLog(LogType.DEBUG, TAG, "Finding the checked sensors by user on the layout.");
        List<List<String>> checkedSensors = new ArrayList<>();
        for (List<CheckBox> checkBoxList : checkBoxesList) {
            List<String> checkBoxTexts = new ArrayList<>();
            for (CheckBox checkBox : checkBoxList) {
                if (checkBox.isChecked()) {
                    checkBoxTexts.add(checkBox.getText().toString());
                }
            }
            checkedSensors.add(checkBoxTexts);
        }
        return checkedSensors;
    }

    /**
     * This method is called when a new item is selected on the camera resolution's Spinner.
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        resolutionPick = parent.getItemAtPosition(position).toString();
        ((TextView)parent.getChildAt(0)).setTextColor(Color.WHITE);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
