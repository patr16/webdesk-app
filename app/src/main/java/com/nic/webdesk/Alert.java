package com.nic.webdesk;

import android.content.DialogInterface;
import android.os.Handler;
import android.os.HandlerThread;
import android.content.Context;
import androidx.appcompat.app.AlertDialog;

public class Alert {

    public interface YesNoCallback {
        void onResult(boolean yes);
    }
    /*
    public interface FlagButtonCallback {
        void onButtonClicked(int value);
    }
*/
    // --------------------------------------------------Alert normal
    //public static void alertDialog(final ActivityNewUser context, String title, String message) {public static void alertDialog(final Context context, String title, String message) {
    public static void alertDialog(final Context context, String title, String message,int milliSec) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        HandlerThread handlerThread = new HandlerThread("DismissDialogThread");
        handlerThread.start();
        new Handler(handlerThread.getLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
            }
        }, milliSec); // 10000 millisecondi = 10 secondi
    }

    // --------------------------------------------------Alert with choice yes/no
    /*
    import YesNoCallback
    Alert.alertYesNoDialog(this, "title...", "message...?", new Alert.YesNoCallback() {
        @Override
        public void onResult(boolean yes) {
            if (yes) {
                webdeskDao.deleteWebdesk(siteId);
                finish();
            }
        }
    });
     */
    public static void alertYesNoDialog(final Context context, String title, String message, final YesNoCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Sì", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        callback.onResult(true);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        callback.onResult(false);
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}

