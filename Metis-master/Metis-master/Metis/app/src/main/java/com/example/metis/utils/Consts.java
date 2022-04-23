package com.example.metis.utils;


import android.media.CamcorderProfile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class Consts is responsible for storing
 * all of the const values for the package
 * for one common source of usage.
 */
public class Consts {
    public static final String APP_NAME = "Metis";

    public static final String THERAPIST_PREFIX_KEYWORD = "user_";
    public static final String PATIENT_PREFIX_KEYWORD = "patient_";
    public static final String PATIENTS_KEYWORD = "patients";
    public static final String ID_KEYWORD = "id";
    public static final String HRZ_KEYWORD = "hrz";
    public static final String SENSORS_KEYWORD = "sensors";
    public static final String VIDEOS_KEYWORD = "videos";
    public static final String THERAPIST_IDS_KEYWORD = "ids";
    public static final String DOMINANT_HAND_KEYWORD = "hand";
    public static final String LANGUAGE = "language";
    public static final String LOGIN_KEYWORD = "login";
    public static final String JSON_COUNTER_KEYWORD = "jsonCounter";
    public static final String DEFAULT_SENSORS_KEYWORD = "default_sensors";
    public static final String APPLICATION_DATA_FILE = "applicationData.json";

    public static final String PATIENT_KEYWORD = "patient";

    public static final String JSON_FILE_EXTENSION_KEYWORD = ".json";
    public static final String DATA_FOLDER = "/data/";
    public static final String LOG_FOLDER = "/log/";
    public static final String JSON_FOLDER = "/JSONs/";
    public static final String DEFAULT_IMAGE_PATH = "default_image/";

    public static File APP_FILES_PATH;
    public static String GAMES_IMAGES_PATH = "/games/images";

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss:SSS");
    public static final String SERIES_KEYWORD = "series";
    public static final String IMAGES_KEYWORD = "images";
    public static final String NAMES_KEYWORD = "names";

    public static final int SUCCESS_PERMISSION_CODE = 1;
    public final static float IMAGE_BTN_TEXT_SIZE = 20;
    public final static int IMAGE_SIZE = 600;
    public final static int FRAMES_PER_ROW = 3;
    public final static int LANDSCAPE_CAMERA_ORIENTATION = 0;
    public final static int REVERSE_LANDSCAPE_CAMERA_ORIENTATION = 180;

    public static final String USER_NAME_KEYWORD = "userName";
    public static final String PATH_TO_VIDEO_KEYWORD = "path_to_vid";
    public static final String SENSORS_FILE_PATH = "sensorsFilePath";
    public static final String CURRENT_DATE_STRING = "currentDateString";
    public static final String TOUCH_EVENTS_FILE_PATH = "touchEventsFilePath";

    public static final String LANDSCAPE_ORIENTATION_KEYWORD = "landscape";
    public static final String ORIENTATION_KEYWORD = "orientation";

    public static List<String> DEFAULT_SENSORS_LIST = Arrays.asList("^icm42605m accelerometer$", "^icm42605m gyroscope$", "^ak09918 magnetometer$");

    public static final int DEFAULT_SAMPLING_RATE = 100;
    public static final int ONE_SECOND_IN_MICROSECOND = 1000000;
  
    public static final String BASIC_ERROR_MESSAGE = "An error occurred.";
    public static int LANGUAGE_LINE = 1;
    public static final String [][] LANGUAGE_ARRAY =
            {{"בחר את רמת הקושי", "תורך!", "בינוני" ,"קל", "קשה מאוד", "קשה",
                "לחיצה לא נכונה. מספר הלחיצות הנכונות: ", "כן", "לא", "לשנות את רמת הקושי?",
                    "לא ניתן להשתמש במקש זה כרגע", ", תרצה להמשיך לשחק?", "נקודות : ","                שיא : "},
            {"Select a difficulty level       ", "Your Turn", "Medium", "Easy", "Very Hard", "Hard",
             "Wrong click. Number of right click: ", "yes", "no", "Change the difficulty level?",
                    "This key cannot be used at this time", ", Do you want to keep playing? ",
                    "Current score: ", "           High score: " }};

    public enum LogType {
        ERROR, INFO, DEBUG
    }
    public enum Messages {
        Select_a_difficulty_level, Your_Turn, Medium, Easy, Very_Hard, Hard,
        Wrong_click, Yes, No, Change_the_difficulty_level, This_key_cannot_be_used_at_this_time,
        Do_you_want_to_keep_playing, Current_score, High_score
    }

    public static final String CAMERA_RESOLUTION_KEYWORD = "cameraResolution";
    public static final String CAMERA_RESOLUTION_480 = "480p";
    public static final String CAMERA_RESOLUTION_720 = "720p";
    public static final String CAMERA_RESOLUTION_1080 = "1080p";

    public static final Map<String, Integer> CAMERA_RESOLUTION_MAP = new HashMap<String, Integer>() {{
        put(CAMERA_RESOLUTION_480, CamcorderProfile.QUALITY_480P);
        put(CAMERA_RESOLUTION_720, CamcorderProfile.QUALITY_720P);
        put(CAMERA_RESOLUTION_1080, CamcorderProfile.QUALITY_1080P);
    }};

    public static final String RIGHT_HAND_KEYWORD = "right";
    public static final String LEFT_HAND_KEYWORD = "left";


}
