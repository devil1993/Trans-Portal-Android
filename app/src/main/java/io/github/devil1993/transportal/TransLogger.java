package io.github.devil1993.transportal;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Sam Ghatak on 07-02-2018.
 */

public class TransLogger {
    public static String LOG = "LOG";
    public static String WARN = "WARNING";
    public static String ERROR = "ERROR";
    public static void appendLog(String msg, String type){
        appendLog(new Exception(msg),type);
    }
    public static void appendLog(Exception ex,String type)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String text = ex.getMessage();
        String trace = sw.toString();
        String root = Environment.getExternalStorageDirectory().getPath();
        String logDirectory = root + "/TransPortal/";
        SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy");
        String currentDate = sdf.format(new Date());
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss.SSS");

        File logFile = new File(logDirectory+currentDate+".log");
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
            if(type.equals(TransLogger.ERROR)) {
                buf.append(trace);
                buf.newLine();
            }
            //buf.close();
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
