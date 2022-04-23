package com.example.metis.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.metis.data_types.Patient;
import com.example.metis.R;
import com.example.metis.utils.ApplicationDataManager;
import com.example.metis.utils.JsonToMap;
import com.example.metis.utils.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.metis.utils.Consts.*;

/**
 * PatientsListActivity class is responsible for enabling
 * the user to create, edit and select between patients.
 * If a patient is selected (and pressed 'select' button - a session
 * with the patient will start.
 */
public class PatientsListActivity extends AppCompatActivity implements View.OnClickListener {


    private String TAG = this.getClass().getSimpleName();

    private String userName;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> patients;
    private String currentMark = "";
    private Map<String, String> patientsMap;
    private Button selectButton;
    private Button createButton;
    private Button editButton;
    private Button deleteButton;
    private TextView patientsListTitle;
    private ApplicationDataManager AD;
    private Logger logger = Logger.getInstance(APP_FILES_PATH);

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
        setContentView(R.layout.activity_patients_list);

        AD = new ApplicationDataManager();
        logger.writeLog(LogType.INFO, TAG, "Inside PatientsListActivity");
        patientsListTitle = findViewById(R.id.title_patients);

        selectButton = findViewById(R.id.select_button);
        selectButton.setOnClickListener(this);

        createButton = findViewById(R.id.create_button);
        createButton.setOnClickListener(this);

        editButton = findViewById(R.id.edit_button);
        editButton.setOnClickListener(this);

        deleteButton = findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(this);

        listView = findViewById(R.id.patients_listView);

        Intent intent = getIntent();
        userName = (String) intent.getSerializableExtra(USER_NAME_KEYWORD);

        patientsListTitle.setText(userName + "'s Patients:");

        // create a therapist patients list
        patients = createPatientsList();

        logger.writeLog(LogType.INFO, TAG, "The patients of " + userName + " are: " + patients.toString());

        // set up view list
        adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.view_list_text, patients);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // save which line is marked
                currentMark = ((TextView) view).getText().toString();
            }
        });
    }

    /**
     *This function create list of patients of the therapist
     * @return list of the patients names
     */
    public ArrayList<String> createPatientsList() {
        // check if a therapist file is available. if not create an empty one.
        if (AD.checkAndCreateTherapistFile(THERAPIST_PREFIX_KEYWORD + userName + JSON_FILE_EXTENSION_KEYWORD, this)) {
            logger.writeLog(LogType.ERROR, TAG, "Therapist file is available.");
            Map<String, Object> userJson = JsonToMap.createJson(THERAPIST_PREFIX_KEYWORD + userName + JSON_FILE_EXTENSION_KEYWORD, getApplicationContext());
            if (userJson != null) {
                logger.writeLog(LogType.ERROR, TAG, "CreatePatientsList. userJson is not null.");
                if (userJson.get(THERAPIST_IDS_KEYWORD) instanceof String) {
                    patientsMap = new HashMap<>();
                } else {
                    patientsMap = (Map<String, String>) userJson.get(THERAPIST_IDS_KEYWORD);
                }
                if (userJson.get(LANGUAGE) != null) {
                    LANGUAGE_LINE = Integer.valueOf(userJson.get(LANGUAGE).toString());
                } else {
                    logger.writeLog(LogType.DEBUG, TAG, "CreatePatientsList. userJson language value is null. default value added.");
                    userJson.put(LANGUAGE, 0); // put default language in user json.
                }
            } else {
                logger.writeLog(LogType.ERROR, TAG, "CreatePatientsList. userJson is null.");
            }
            if(userJson != null && userJson.get(DEFAULT_SENSORS_KEYWORD) != null) {
                createDefaultSensors(userJson.get(DEFAULT_SENSORS_KEYWORD));
            }
            patients = new ArrayList<>();
            patients.addAll(patientsMap.keySet());
            return patients;
        }
        return new ArrayList<String>();
    }

    private void createDefaultSensors(Object o) {
        if (o != null && o instanceof List) {
            List<String> sensors = (ArrayList<String>) o;
            if(sensors.get(0).charAt(0) != '^') {
                addRegexSymbols(sensors);
            }
        }
    }

    private void addRegexSymbols(List<String> sensors) {
        ArrayList<String> newSensors = new ArrayList<>();
        for(String sensor: sensors) {
            newSensors.add("^" + sensor + "$");
        }
        DEFAULT_SENSORS_LIST = newSensors;
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        Patient patient;
        switch (v.getId()) {
            case R.id.select_button:
                if (currentMark != "") {
                    patient = new Patient(JsonToMap.createJson(PATIENT_PREFIX_KEYWORD + patientsMap.get(currentMark) + JSON_FILE_EXTENSION_KEYWORD, getApplicationContext()), userName, patientsMap.get(currentMark));
                    logger.writeLog(LogType.INFO, TAG, "The chosen patient is: " + patient.toString());
                    intent = new Intent(this, MenuActivity.class);
                    intent.putExtra(PATIENT_KEYWORD, (Serializable) patient);
                    startActivity(intent);
                }
                break;
            case R.id.edit_button:
                if (currentMark != "") {
                    patient = new Patient(JsonToMap.createJson(PATIENT_PREFIX_KEYWORD + patientsMap.get(currentMark) + JSON_FILE_EXTENSION_KEYWORD,
                            getApplicationContext()), userName, patientsMap.get(currentMark));
                    logger.writeLog(LogType.INFO, TAG,
                            "Editing patient: " + patient.toString());
                    ApplicationDataManager.setEditPair(patient);
                    intent = new Intent(this, CreatePatientActivity.class);
                    intent.putExtra(PATIENT_KEYWORD, (Serializable) patient);
                    startActivity(intent);
                }
                break;
            case R.id.create_button:
                logger.writeLog(LogType.INFO, TAG, "Create new patient");
                intent = new Intent(this, CreatePatientActivity.class);
                intent.putExtra(USER_NAME_KEYWORD, (Serializable) userName);
                intent.putExtra(PATIENTS_KEYWORD, (Serializable)patients);
                startActivity(intent);
                break;
            case R.id.delete_button:
                if (currentMark != "") {
                    logger.writeLog(LogType.INFO, TAG, "Delete patient: " + patientsMap.get(currentMark));
                    ApplicationDataManager.deletePatient(currentMark, THERAPIST_PREFIX_KEYWORD + userName + JSON_FILE_EXTENSION_KEYWORD);
                    recreate();
                }
                break;
        }
    }
}
