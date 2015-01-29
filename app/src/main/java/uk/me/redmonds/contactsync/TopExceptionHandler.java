package uk.me.redmonds.contactsync;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.io.*;

public class TopExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler defaultUEH;

    private Activity app = null;

    public TopExceptionHandler(Activity app) {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        this.app = app;
    }

    public void uncaughtException(Thread t, Throwable e)
    {
        String report = generateReport(e);

        try {
            FileOutputStream trace = app.openFileOutput(
                    "stack.trace", Context.MODE_PRIVATE);
            trace.write(report.getBytes());
            trace.close();
        } catch(IOException ioe) {
// ...
        }

        defaultUEH.uncaughtException(t, e);
    }

    public static String generateReport(Throwable e) {
        StackTraceElement[] arr = e.getStackTrace();
        String report = e.toString()+"\n\n";
        report += "-------------------------------\n\n";
        report += "--------- Device ---------\n\n";
        report += "Brand: " + Build.BRAND + "\n";
        report += "Device: " + Build.DEVICE + "\n";
        report += "Model: " + Build.MODEL + "\n";
        report += "Id: " + Build.ID + "\n";
        report += "Product: " + Build.PRODUCT + "\n";
        report += "-------------------------------\n\n";
        report += "--------- Firmware ---------\n\n";
        report += "Release: " + Build.VERSION.RELEASE + "\n";
        report += "Incremental: " + Build.VERSION.INCREMENTAL + "\n";
        report += "-------------------------------\n\n";
        report += "--------- Stack trace ---------\n\n";

        for (int i=0; i<arr.length; i++)
        {
            report += "    "+arr[i].toString()+"\n";
        }
        report += "-------------------------------\n\n";

// If the exception was thrown in a background thread inside
// AsyncTask, then the actual exception can be found with getCause
        report += "--------- Cause ---------\n\n";
        Throwable cause = e.getCause();
        if(cause != null) {
            report += cause.toString() + "\n\n";
            arr = cause.getStackTrace();
            for (int i=0; i<arr.length; i++)
            {
                report += "    "+arr[i].toString()+"\n";
            }
        }
        report += "-------------------------------\n\n";

        return report;
    }

    public static void sendReport (Activity main, String report) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        String subject = "Error report";
        String body =
                "Mail this to ojredmond@gmail.com: "+
                        "\n\n"+
                        report +
                        "\n\n";

        sendIntent.putExtra(Intent.EXTRA_EMAIL,
                new String[] {"ojredmond@gmail.com"});
        sendIntent.putExtra(Intent.EXTRA_TEXT, body);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sendIntent.setType("message/rfc822");

        main.startActivity(
                Intent.createChooser(sendIntent, "Title:"));

    }
}