package com.example.metis.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import static com.example.metis.utils.Consts.*;

/**
 * Class EventWriter is used in order to log the program
 * and get an idea for what were the problems, or just
 * getting information about the application.
 */
public class Logger {

    private static Logger instance = null;

    private String TAG = this.getClass().getSimpleName();

    private File file;
    private FileWriter writer;
    public String absolutePath;

    private Logger(File absolutePaths)
    {
        try {
            absolutePath = absolutePaths.getPath();
            File dir = new File(absolutePath + LOG_FOLDER);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            file = new File(absolutePath + LOG_FOLDER + "log.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            writer = new FileWriter(file, true);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

//    public static void initializeLogDir(File absolutePaths) {
//        absolutePath = absolutePaths.getPath();
//        File dir = new File(absolutePath + LOG_FOLDER);
//        if (!dir.exists()) {
//            dir.mkdirs();
//        }
//    }

    /**
     * This function write to the log file
     * @param logType the type of the log
     * @param TAG tag
     * @param logMessage the message
     */
    public void writeLog(LogType logType, String TAG, String logMessage) {
        try {
            writer.append(DATE_FORMAT.format(Calendar.getInstance().getTime()) + " " + logType + " " + TAG + ": " + logMessage + "\n");
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Logger is singleton, therefore creating one is only through this method.
     * @return instance of the logger.
     */
    public static Logger getInstance(File absolutePaths)
    {
        if (instance == null)
            instance = new Logger(absolutePaths);

        return instance;
    }

    /**
     * This method closes the writer used by the logger.
     */
    public void closeWriter() {
        this.writeLog(LogType.INFO, TAG, "closeWriter. Closing logger writer.");
        try {
            writer.close();
        } catch (IOException e) {
            this.writeLog(LogType.ERROR, TAG, "closeWriter. Could not close logger writer.");
            e.printStackTrace();
        }
    }
}
