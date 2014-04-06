package com.akavrt.worko.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.akavrt.worko.MainActivity;
import com.akavrt.worko.R;

/**
 * @author Victor Balabanov <akavrt@gmail.com>
 */
public class Notificator {
    public static final String STATUS_BAR_NOTIFICATION =
            Notificator.class.getPackage() + "status_bar_notification";
    private static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    private Context mContext;

    public Notificator(Context context) {
        this.mContext = context;

        mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public Notification build() {
        Intent activityIntent = new Intent(mContext, MainActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activityIntent.putExtra(STATUS_BAR_NOTIFICATION, true);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                mContext,
                0,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_stat_counting)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setContentTitle(mContext.getString(R.string.notification_title))
                .setContentText(mContext.getString(R.string.notification_default_text));

        return builder.build();
    }

    public int getId() {
        return NOTIFICATION_ID;
    }

}
