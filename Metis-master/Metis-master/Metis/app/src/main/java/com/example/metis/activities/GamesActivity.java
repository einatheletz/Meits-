package com.example.metis.activities;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.constraintlayout.widget.Constraints;
import androidx.core.widget.ImageViewCompat;
import androidx.core.widget.NestedScrollView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.metis.data_types.Margins;
import com.example.metis.data_types.Patient;
import com.example.metis.utils.ApplicationDataManager;
import com.example.metis.utils.EventWriter;
import com.example.metis.R;
import com.example.metis.utils.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.metis.utils.Consts.*;

/**
 * GamesActivity class is responsible for showing available
 * games for the patient, and allowing to choose between them.
 */
public class GamesActivity extends AppCompatActivity
        implements
        GestureDetector.OnGestureListener {

    private String TAG = this.getClass().getSimpleName();

    private File touchEventsFile;
    private GestureDetector gestureDetector;


    Logger logger = Logger.getInstance(APP_FILES_PATH);
    DialogInterface.OnClickListener errorListener;

    private String defaultImagePath = DEFAULT_IMAGE_PATH;
    private Patient patient;
    /***
     * in this function there is a list of game activities,
     * and a list of the paths to each game activity.
     * if you want to add a game to the application:
     *      1. Add the activity to the application.
     *      2. Add the activity's class to the classes list.
     *      3. Add an image of the game to files->games->images.
     *      4. Add the path of the new image to the list of images.
     * @param savedInstanceState saved instance of the intent.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ApplicationDataManager.fullScreenView(getWindow());

        //Games Images full Path
        final String LOCAL_GAMES_IMAGES_PATH = APP_FILES_PATH + GAMES_IMAGES_PATH;
        //get patient
        patient = (Patient) getIntent().getSerializableExtra(PATIENT_KEYWORD);
        errorListener =  new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };
        try {
            //initialize the path to the image of the default empty image.
            defaultImagePath = APP_FILES_PATH + "/" + defaultImagePath;
            defaultImagePath = defaultImagePath + getImagesPaths(defaultImagePath, true).get(0);

        } catch (IndexOutOfBoundsException e){
            logger.writeLog(LogType.ERROR, TAG, "Error: There is no 'default_image' folder or there are no images in this folder.");

                ApplicationDataManager.getErrorMessageDialog(this,
                        "There is no 'default_image' folder or there are no images in this folder.", errorListener);

        } catch (Exception e) {
            logger.writeLog(LogType.ERROR, TAG, "Error: An error has occurred with games images.");

                ApplicationDataManager.getErrorMessageDialog(this,
                        "An error has occurred with games images.", errorListener);

        }
        setContentView(R.layout.activity_games);
        ApplicationDataManager.setOrientation(patient, this);
        touchEventsFile = (File) getIntent().getSerializableExtra(TOUCH_EVENTS_FILE_PATH);
        gestureDetector = new GestureDetector(this, this);

        /**
         * hashmap between String and Class.
         * add entries of new game activities and their name (the name of their image).
         * just by adding a line that put the entry in the map.
         * for example:
         * HashMap<String,Class> classes = new HashMap<String,Class>(){{
         *             put("simon",SimonActivity.class);
         *             put("newGame",newGameActivity.class);
         * }};
         */
        HashMap<String, Class> namesToClassMap = new HashMap<String, Class>() {{
            put("simon", SimonActivity.class);
        }};
        /**
         * End of initialization.
         */

        //replace names with paths from images folder.
        //image name should be the same as game name.
        ArrayList<String> imagesPaths = (ArrayList<String>) getImagesPaths(LOCAL_GAMES_IMAGES_PATH, false);
        HashSet<String> imagesNames = (HashSet<String>) getImagesPaths(LOCAL_GAMES_IMAGES_PATH, true)
                .stream().map(name -> name.substring(0, name.indexOf('.'))).collect(Collectors.toSet());
        ArrayList<String> newImgsPathList = new ArrayList<>();
        ArrayList<Class> newClassesList = new ArrayList<>();
        ArrayList<String> newImgsNameList = new ArrayList<>();
        // for each game - add the game's image.
        for (Map.Entry<String, Class> game : namesToClassMap.entrySet()) {
            // if the game image is unavailable - use the default image.
            if (imagesPaths.isEmpty() || !imagesNames.contains(game.getKey())) {
                newImgsPathList.add(defaultImagePath);
            } else {
                newImgsPathList.add(getImagePath(game.getKey(), imagesPaths));
            }
            newClassesList.add(game.getValue());
            newImgsNameList.add(game.getKey());
        }

        //get resources and displayMetrics
        Resources r = getResources();
        DisplayMetrics displayMetrics = r.getDisplayMetrics();


        LinearLayout scrollAndBackLinearLayout = new LinearLayout(this);
        scrollAndBackLinearLayout.setOrientation(LinearLayout.VERTICAL);
        scrollAndBackLinearLayout.setGravity(Gravity.CENTER);

        //init the root view as scrollView and set params
        NestedScrollView rootView = new NestedScrollView(this);
        rootView.setBackground(getDrawable(R.drawable.background_gradient));
        NestedScrollView.LayoutParams lp = new NestedScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rootView.setLayoutParams(lp);

        //declare frame dimensions.
        final float DP_VID_IMG_PADDING = r.getDimension(R.dimen.video_image_padding) / displayMetrics.density;
        final float DP_VID_IMG_MRGIN_IN = r.getDimension(R.dimen.video_image_margin_in) / displayMetrics.density;
        final float DP_VID_IMG_MRGIN_OUT = r.getDimension(R.dimen.video_image_margin_out) / displayMetrics.density;

        //get reference of this activity
        AppCompatActivity GamesActivityReference = this;

        ArrayList<TableRow> rows = new ArrayList<>();
        //set dimensions in pixels for margins
        int paddingPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DP_VID_IMG_PADDING, displayMetrics);
        int mrginInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DP_VID_IMG_MRGIN_IN, displayMetrics);
        int mrginOutPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DP_VID_IMG_MRGIN_OUT, displayMetrics);
        //create TableLayout with shrink/strecth all columns properties.
        TableLayout table = new TableLayout(this);
        table.setShrinkAllColumns(true);

        //for every game in the list - create the button which transfers to it.
        for (int gameNum = 0; gameNum < newImgsNameList.size(); gameNum++) {
            if (gameNum % FRAMES_PER_ROW == 0) {
                TableRow row = new TableRow(this);
                row.setGravity(Gravity.CENTER);
                rows.add(row);
            }
            LinearLayout linear = new LinearLayout(this);
            linear.setOrientation(LinearLayout.VERTICAL);
            linear.setGravity(Gravity.CENTER);
            //create image button.
            AppCompatImageButton btn = new AppCompatImageButton(this);
            //get the margins.
            Margins margins = new Margins(gameNum, newImgsNameList.size(), FRAMES_PER_ROW, mrginOutPx, mrginInPx);

            //set layout params and margins.
            TableRow.LayoutParams trlp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            trlp.setMargins(margins.getLeft(), margins.getTop(), margins.getRight(), margins.getBottom());
            linear.setLayoutParams(trlp);

            //set image resource and scaling.
            Bitmap bitmap = BitmapFactory.decodeFile(newImgsPathList.get(gameNum));
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true);
            btn.setImageBitmap(resized);
            btn.setScaleType(ImageView.ScaleType.CENTER_CROP);
            btn.setAdjustViewBounds(true);

            //set button border.
            btn.setBackground(getDrawable(R.drawable.border));
            //set button padding.
            btn.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
            //set button id.
            btn.setId(gameNum);
            //set the button tag (game name).
            btn.setTag(newImgsNameList.get(gameNum));
            //set button OnClick and OnTouch.
            setOnClickAndTouch(btn, new Intent(this, newClassesList.get(gameNum)));

            //create video text.
            TextView text = new TextView(this);
            text.setText(newImgsNameList.get(gameNum));
            text.setTextSize(IMAGE_BTN_TEXT_SIZE);
            text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            text.setBackground(getDrawable(R.drawable.border));

            //add btn to layout.
            linear.addView(btn);
            //add text to layout.
            linear.addView(text);
            //add frame to row.
            rows.get(gameNum / FRAMES_PER_ROW).addView(linear);
        }
        //for every row that was created add it to TableLayout
        for (TableRow row : rows) {
            table.addView(row);
        }
        //add TableLayout to the NestedScrollingView and set the content view.
        rootView.addView(table);
        //set rootView onTouchListener so that it would call onTouchEvent of this activity.
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                logger.writeLog(LogType.INFO, TAG, "onTouch. Touch event in GamesActivity.");
                rootView.onTouchEvent(event);
                GamesActivityReference.onTouchEvent(event);
                return true;
            }
        });

        /**
         * Dynamically adding back button to the view.
         **/
        LinearLayout linearLayout = new LinearLayout(this);
        //create image button.
        AppCompatImageButton backButton = new AppCompatImageButton(this);
        //set button image.
        backButton.setBackground(getDrawable(R.drawable.back_button));
        backButton.setMaxHeight(10);
        backButton.setMaxWidth(10);
        backButton.setLayoutParams(new ViewGroup.LayoutParams(150,150));

        View view = new View(this);
        linearLayout.addView(backButton);
        linearLayout.addView(view);

        backButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    float x = event.getX();
                    float y = event.getY();
                    EventWriter.writeString("Event,Back button pressed,x:" + x + ",y:" + y, touchEventsFile, TAG);
                    EventWriter.writeEvent(event, touchEventsFile, TAG, true);
                    gestureDetector.onTouchEvent(event);
                    onBackPressed();
                    return true;
                } else {
                    return false;
                }
            }
        });

        rootView.setLayoutParams(new NestedScrollView.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT));
        scrollAndBackLinearLayout.addView(rootView);

        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT));
        scrollAndBackLinearLayout.addView(linearLayout);

        scrollAndBackLinearLayout.setBackground(getDrawable(R.drawable.background_gradient));
        setContentView(scrollAndBackLinearLayout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ApplicationDataManager.fullScreenView(getWindow());
    }

    @Override
    protected void onStart() {
        super.onStart();
        ApplicationDataManager.fullScreenView(getWindow());
    }

    /**
     * This method is used in order to receive path of the game image.
     */
    private String getImagePath(String key, ArrayList<String> imagesPaths) {
        return imagesPaths.stream().filter(p -> p.contains(key)).collect(Collectors.toList()).get(0);
    }

    private List<String> getImagesPaths(String path, boolean onlyNames) {
        File[] folder = new File(path).listFiles();
        //if the folder is empty add an ugly fix
        if (folder == null || folder.length <= 0) {
            return new ArrayList<>();
        }
        return Arrays.stream(folder)
                .map(f -> onlyNames ? f.getName() : path + "/" + f.getName())
                .collect(Collectors.toList());
    }

    /**
     * This method is used in order to set OnClick and OnTouch callbacks with parameters.
     * @param btn The button which the callbacks will be assigned to.
     * @param intent The intent of the game (SimonActivity, etc..)
     */
    private void setOnClickAndTouch(final AppCompatImageButton btn, final Intent intent) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logger.writeLog(LogType.INFO, TAG, "onClick. Game chosen: " + (String)v.getTag());
                intent.putExtra(TOUCH_EVENTS_FILE_PATH, touchEventsFile);
                intent.putExtra(PATIENT_KEYWORD, patient);
                startActivity(intent);
            }
        });
        btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    EventWriter.writeString("Event,Game chosen: " + (String) v.getTag() +
                            ",x:" + event.getX() + ",y:" + event.getY(), touchEventsFile, TAG);
                    EventWriter.writeEvent(event, touchEventsFile, TAG, true);
                    gestureDetector.onTouchEvent(event);
                    return true;
                }
                if(event.getAction() == MotionEvent.ACTION_UP && !(event.getAction() == MotionEvent.ACTION_SCROLL)) {
                    v.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * This method is invoked when an unrelated touch occurred
     * @param event motion event of the touch.
     * @return boolean
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        logger.writeLog(LogType.INFO, TAG, "onTouchEvent. event: " + event);
        EventWriter.writeEvent(event, touchEventsFile, TAG, false);
        gestureDetector.onTouchEvent(event); // detect the type of the touch event.
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        EventWriter.writeString("Touch Details,Down Press", touchEventsFile, TAG);
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        EventWriter.writeString("Touch Details,Show Press", touchEventsFile, TAG);
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        EventWriter.writeString("Touch Details,Single Tap", touchEventsFile, TAG);
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        EventWriter.writeString("Touch Details,Scroll Motion: Distance X: " + distanceX
                + " Distance Y: " + distanceY, touchEventsFile, TAG);
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        EventWriter.writeString("Touch Details,Long Press", touchEventsFile, TAG);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        EventWriter.writeString("Touch Details,Fling Motion: Velocity X: " + velocityX
                + " Velocity Y: " + velocityY, touchEventsFile, TAG);
        return false;
    }
}
