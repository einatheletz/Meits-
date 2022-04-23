package com.example.metis.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import static com.example.metis.utils.Consts.*;
import android.view.MotionEvent;

/**
 * Class EventWriter is used in order to write
 * events and strings to files. It is touchevent driven.
 */
public class EventWriter {

    /**
     * This method is used in order to write a string to a given file
     * by using the activity name.
     * @param toWrite string to be written to the file.
     * @param file file for data.
     * @param activityName TAG of the activity.
     */
    public static void writeString(String toWrite, File file, String activityName) {
        String currentDateString = DATE_FORMAT.format(Calendar.getInstance().getTime());
        try {
            FileWriter writer = new FileWriter(file, true);
            writer.write(currentDateString + "," + activityName + "," + toWrite + "\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used in order to write given events
     * to a file with the TAG of the activity.
     * @param event touch event to be written.
     * @param file file fot writing the data.
     * @param activityName TAG of the starting activity.
     * @param isView if the touch came from a specific view - true, else false.
     */
    public static void writeEvent(MotionEvent event, File file, String activityName, boolean isView) {
        // get the touch coordinates.
        float x = event.getX();
        float y = event.getY();
        String eventString = "Raw,";
        // if the touch was on a view (intended) - "Event", else - "Raw" data.
        if (isView) {
            eventString = "Event,";
        }

        int motionAction = event.getAction();
        switch(motionAction) {
            case (MotionEvent.ACTION_DOWN) :
                eventString += "Action DOWN,x:"+x+",y:"+y;
                System.out.println("Action DOWN on (x,y):"+"("+x+","+y+")");
                break;
            case (MotionEvent.ACTION_MOVE) :
                eventString += "Action MOVE,x:"+x+",y:"+y;
                System.out.println("Action MOVE on (x,y):"+"("+x+","+y+")");
                break;
            case (MotionEvent.ACTION_UP) :
                eventString += "Action UP,x:"+x+",y:"+y;
                System.out.println("Action UP on (x,y):"+"("+x+","+y+")");
                break;
            case (MotionEvent.ACTION_CANCEL) :
                eventString += "Action CANCEL,x:"+x+",y:"+y;
                System.out.println("Action CANCEL on (x,y):"+"("+x+","+y+")");
                break;
            case (MotionEvent.ACTION_OUTSIDE) :
                eventString += "Action OUTSIDE OF BOUNDS,x:"+x+",y:"+y;
                System.out.println("Action OUTSIDE OF BOUNDS on (x,y):"+"("+x+","+y+")");
                break;
            default :
                break;
        }

        writeString(eventString, file, activityName);
    }
}