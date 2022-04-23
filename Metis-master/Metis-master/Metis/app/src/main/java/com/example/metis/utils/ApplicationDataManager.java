package com.example.metis.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.metis.activities.Video;
import com.example.metis.data_types.Patient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.metis.utils.Consts.*;

/**
 * This class is responsible for all read/write files actions.
 */
public class ApplicationDataManager {

    private static Map<String, Object> applicationDataMap;
    private static Map<String, List<String>> loginDetailsMap;
    private static int jsonCounter = 0;
    private static Pair<Boolean, Patient> isEdit = new Pair<>(false, new Patient());
    private static Context context = AppContext.getAppContext();
  
    public static String absolutePath;
    private static Logger logger;
    private RadioGroup group;
    private AlertDialog languageDialog;
    private RadioButton hebrew;
    private RadioButton english;
    private enum Language{Hebrew, English}

    private static String TAG = Video.class.getSimpleName();

    /**
     * This function check if the entry details are correct.
     * @param userName  user name entered by user
     * @param password  password entered by user
     * @return
     */
    public static boolean checkEntryDetails(String userName, String password) {
        logger.writeLog(LogType.INFO, TAG, "Checking entry details from user.");
//        return true;
        if (loginDetailsMap.containsKey(userName)) {
            if (checkPassword(userName, password)) {
                logger.writeLog(LogType.INFO, TAG, "Login succeed.");
                return true;
            }
        }
        logger.writeLog(LogType.INFO, TAG, "User name or password are incorrect. Login failed.");
        return false;
    }

    /**
     * Check if the password is correct.
     * @param userName  user name entered by user
     * @param password  password entered by user
     * @return
     */
    private static boolean checkPassword(String userName, String password) {
        if (String.valueOf(loginDetailsMap.get(userName)).equals(password)) {
            return true;
        }
        return false;
    }

    /**
     * Checking if CreatePatientActivity is creating new patient or edit existing one.
     * @return  true if on edit mode. false otherwise
     */
    public static boolean checkIfEditMode() {
        return isEdit.first;
    }

    public static void setEditPair(Patient patient) {
        isEdit = new Pair<>(true, patient);
    }

    /**
     * This method initializes the logging and data folder for app output.
     * @param absolutePaths absolute path of the app.
     */
    public static void initializeDirs(File absolutePaths) {
        absolutePath = absolutePaths.getPath();
        File dir = new File(absolutePath + DATA_FOLDER);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        logger = Logger.getInstance(APP_FILES_PATH);
        logger.writeLog(LogType.DEBUG, TAG, "Initialized the program's directories.");

    }

    /**
     * Initialize data map.
     */
    public static void initializeMap() {
        logger.writeLog(LogType.DEBUG, TAG, "Initialize data map.");
        applicationDataMap = JsonToMap.createJson(APPLICATION_DATA_FILE, context);
        loginDetailsMap = (Map<String, List<String>>) applicationDataMap.get(LOGIN_KEYWORD);
        jsonCounter = (int) applicationDataMap.get(JSON_COUNTER_KEYWORD);
    }

    /**
     * Getting jsonCounter and update the ApplicationData file
     * @return  jsonCounter
     */
    public static String getJsonCounter() {
        logger.writeLog(LogType.DEBUG, TAG, "Getting jsonCounter.");
        if (isEdit.first) {
            return isEdit.second.getFileNumber();
        } else {
            jsonCounter++;
            if (!updateApplicationDataJson()) {
                return null;
            }
            return String.valueOf(jsonCounter);
        }
    }

    /**
     * Updating application data JSON.
     * @return  true if succeed writing, false otherwise
     */
    private static boolean updateApplicationDataJson() {
        logger.writeLog(LogType.DEBUG, TAG, "Updating ApplicationData file.");
        JSONObject jsonObject = new JSONObject();
        JSONObject loginDetailsJson = new JSONObject(loginDetailsMap);
        try {
            jsonObject.put(LOGIN_KEYWORD, loginDetailsJson);
            jsonObject.put(JSON_COUNTER_KEYWORD, jsonCounter);
            writeNewFile(APPLICATION_DATA_FILE, jsonObject.toString());
        } catch (JSONException e) {
            logger.writeLog(LogType.ERROR, TAG, "An error has occurred: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Writing new file or updating an existing one.
     * @param fileName  file name
     * @param content   the file content
     * @return  true if succeed writing, false otherwise
     */
    public static boolean writeNewFile(String fileName, String content) {
        logger.writeLog(LogType.DEBUG, TAG, "Writing file. Name: " + fileName + " File's content: " + content);
        try {
            File outputFile = new File(absolutePath + JSON_FOLDER + fileName);
            FileWriter writer = new FileWriter(outputFile, false);
            writer.append(content);
            writer.flush();
            writer.close();
            return true;
        } catch (Exception e) {
            logger.writeLog(LogType.ERROR, TAG, "An error has occurred: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates the therapist file with the new patient.
     * @param therapistName the therapist's name (file name)
     * @param patientId the patient id
     * @param patientFileID the name of the patient's file
     * @return  true if succeed writing, false otherwise
     */
    public static boolean writeTherapistFile(String therapistName,
                                             String patientId, String patientFileID) {
        Map<String, Object> therapistJson = JsonToMap.createJson(THERAPIST_PREFIX_KEYWORD + therapistName, context);
        Map<String, String> ids;
        // the ids will be empty string when it just created and null if it's the creation
        if (therapistJson != null && therapistJson.get(THERAPIST_IDS_KEYWORD) instanceof Map) {
            ids = (Map<String, String>) therapistJson.get(THERAPIST_IDS_KEYWORD);
        } else {
            ids = new HashMap<>();
        }
        ids.put(patientId, patientFileID);
        JSONObject jsonObject = new JSONObject();
        JSONObject idsJson = new JSONObject(ids);
        try {
            jsonObject.put(THERAPIST_IDS_KEYWORD, idsJson);
            jsonObject.put(LANGUAGE, LANGUAGE_LINE);
            return writeNewFile(THERAPIST_PREFIX_KEYWORD + therapistName, jsonObject.toString());
        } catch (JSONException e) {
            logger.writeLog(LogType.ERROR, TAG, "An error has occurred: " + e.getMessage());
        }
        return false;
    }

    /**
     * This function reads a file and return it as a String
     * @param fileName  file name
     * @return  the file's value
     */
    public static String readFile(String fileName) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(new
                    File(absolutePath + JSON_FOLDER + fileName)));
            String read;
            StringBuilder builder = new StringBuilder("");
            while ((read = bufferedReader.readLine()) != null) {
                builder.append(read);
            }
            bufferedReader.close();
            return builder.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * This function initialize the therapist file
     * @param fileName  file name (therapist name)
     * @param activityContext   application context
     * @return  true if succeed writing, false otherwise
     */
    public  boolean checkAndCreateTherapistFile(String fileName, Context activityContext) {
        File file = new File(absolutePath + JSON_FOLDER + fileName);
        if (!file.exists()) {
            final LinearLayout dialogLayout = createDialogLayout(activityContext);
            languageDialog = createDialog(dialogLayout, activityContext);
            languageDialog.show();
            Map<String, Object> therapistJson = new HashMap();
            therapistJson.put(THERAPIST_IDS_KEYWORD, "");
            therapistJson.put(LANGUAGE,String.valueOf(LANGUAGE_LINE) );
            therapistJson.put(DEFAULT_SENSORS_KEYWORD, DEFAULT_SENSORS_LIST);
            JSONObject json = new JSONObject(therapistJson);
            writeNewFile(fileName, json.toString());
            return false;
        }
        return true;
    }

    /**
     * Pops up an error message to the screen
     * @param context   application context
     * @param errorMessage  the error message
     * @param listener  the "OK" button listener
     */
    public static void getErrorMessageDialog(AppCompatActivity context, String errorMessage, DialogInterface.OnClickListener listener) {
        if(!context.isFinishing() && !context.isDestroyed()) {
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle("Error Occurred")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("OK", listener)
                    .setMessage(errorMessage)
                    .create();
            //Here's the magic..
            //Set the dialog to not focusable (makes navigation ignore us adding the window)
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

            //Show the dialog!
            dialog.show();

            //Set the dialog to immersive
            dialog.getWindow().getDecorView().setSystemUiVisibility(
                    context.getWindow().getDecorView().getSystemUiVisibility());

            //Clear the not focusable flag from the window
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }
    }

    /**
     * Defines pair which define exit of edit mode.
     */
    public static void exitEditMode() {
        isEdit = new Pair<>(false, new Patient());
    }

    /**
     * This method creates a linear layout of a dialog.
     * @return LinearLayout dialog layout.
     */
    private LinearLayout createDialogLayout(Context activityContext){
        LinearLayout dialogLayout = new LinearLayout(activityContext);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        group = new RadioGroup(activityContext);
        hebrew = new RadioButton(activityContext);
        hebrew.setText("עברית");
        group.addView(hebrew);
        english = new RadioButton(activityContext);
        english.setText("English");
        group.addView(english);
        dialogLayout.addView(group);
        return dialogLayout;
    }

    /**
     * This method create dialog windows to get the app language
     * @param dialogLayout linear layout
     * @param activityContext context
     * @return dialog windows
     */
    private  AlertDialog createDialog(View dialogLayout, Context activityContext){
        return new AlertDialog.Builder(activityContext)
            .setMessage("Please choose language.")
            .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    boolean res = group.getCheckedRadioButtonId() != -1;
                    // one of the radio buttons is checked
                    if (res) {
                        if (hebrew.isChecked()) {
                            LANGUAGE_LINE = Language.Hebrew.ordinal();
                        }
                        else if(english.isChecked()) {
                            LANGUAGE_LINE = Language.English.ordinal();
                        }
                    } else {
                        Toast.makeText(activityContext, "please choose language.",
                                Toast.LENGTH_SHORT).show();
                        //show dialog again for retry.
                        languageDialog.show();
                    }
                }
            }).setView(dialogLayout)
            .create();
    }

    /**
     * This function deletes a patient.
     * @param patientId the ID of the patient to delete
     * @param therapistName the therapist's name
     */
    public static void deletePatient(String patientId, String therapistName) {
        Map<String, Object> therapistJson = JsonToMap.createJson(therapistName, context);
        Map<String, String> therapistIds = (Map<String, String>) therapistJson.get(THERAPIST_IDS_KEYWORD);
        therapistIds.remove(patientId);
        therapistJson.put(THERAPIST_IDS_KEYWORD, therapistIds);
        writeNewFile(therapistName, new JSONObject(therapistJson).toString());
    }

    /**
     * This method sets the view of a given activity window to be fullscreen mode.
     * @param activityWindow given window to be full screened.
     */
    public static void fullScreenView(Window activityWindow) {
        View view = activityWindow.getDecorView();
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        });
    }
    @SuppressLint("SourceLockedOrientationActivity")
    public static void setOrientation(Patient patient, AppCompatActivity context){
        logger.writeLog(LogType.DEBUG,context.getClass().getSimpleName(),"Changing screen orientation" +
                " according to user's dominant hand.");
        //default orientation is portrait.
        int wantedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        Object orientation = applicationDataMap.get(ORIENTATION_KEYWORD);
        //check if orientation field initialized, if not just fix orientation and return.
        if(orientation == null){
            if(wantedOrientation != context.getResources().getConfiguration().orientation) {
                context.setRequestedOrientation(wantedOrientation);
            }
            return;
        }
        //in case wanted orientation is landscape check if patient left or right handed.
        if(LANDSCAPE_ORIENTATION_KEYWORD.equalsIgnoreCase((String)applicationDataMap.get(ORIENTATION_KEYWORD))) {
            //if left handed, set reverse landscape
            if (LEFT_HAND_KEYWORD.equalsIgnoreCase(patient.getDominantHand())) {
                wantedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            } else {
                wantedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }
        }
        //change orientation only if necessary.
        if(wantedOrientation != context.getResources().getConfiguration().orientation) {
            context.setRequestedOrientation(wantedOrientation);
        }
    }
}
