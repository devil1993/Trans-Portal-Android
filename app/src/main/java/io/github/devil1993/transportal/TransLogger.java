package io.github.devil1993.transportal;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Sam Ghatak on 07-02-2018.
 */

public class TransLogger {
    public static String LOG = "LOG";
    public static String WARN = "WARNING";
    public static String ERROR = "ERROR";
    public static void appendLog(String text,String type)
    {
        String root = Environment.getExternalStorageDirectory().getPath();
        String logDirectory = root + "/TransPortal/";
        SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy");
        String currentDateandTime = sdf.format(new Date());
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss.SSS");

        File logFile = new File(logDirectory+currentDateandTime+".txt");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        BufferedWriter buf = null;
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append("==================***"+type+"***===============***"+sdf2.format(new Date())+"***==================***");
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            if(buf != null){
                try {
                    buf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
