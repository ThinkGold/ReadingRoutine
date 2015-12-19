package com.agenthun.readingroutine.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.agenthun.readingroutine.R;
import com.agenthun.readingroutine.activities.MainActivity;
import com.agenthun.readingroutine.datastore.BookInfo;
import com.agenthun.readingroutine.datastore.db.BookDatabaseUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class AlarmNoiserIntentService extends IntentService {
    private static final String TAG = "IntentService";

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_NOTIFICATION = "com.agenthun.readingroutine.services.action.NOTIFICATION";
    public static final String ACTION_BAZ = "com.agenthun.readingroutine.services.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.agenthun.readingroutine.services.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.agenthun.readingroutine.services.extra.PARAM2";

    private SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public AlarmNoiserIntentService() {
        super("AlarmNoiserIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionNotification(Context context, String param1, String param2) {
        Intent intent = new Intent(context, AlarmNoiserIntentService.class);
        intent.setAction(ACTION_NOTIFICATION);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, AlarmNoiserIntentService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_NOTIFICATION.equals(action)) {
                BookInfo bookInfo = getNext();
                if (bookInfo != null) {
                    Log.d(TAG, "onHandleIntent() returned: " + bookInfo.getBookName());
                    Log.d(TAG, "onHandleIntent() returned: " + bookInfo.getBookAlarmTime());
                    final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                    final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                    handleActionNotification(param1, param2);
                }
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionNotification(String param1, String param2) {
        // TODO: Handle action Foo
        Log.d(TAG, "handleActionNotification() returned: ");
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_reading_routine_white_no_annulus_48dp)
                .setContentTitle(getString(R.string.text_notification_title))
                .setContentText(param1)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
//        .setStyle(new NotificationCompat.BigTextStyle().bigText(param1));

        Intent intent = new Intent(this, AlarmNoiserIntentService.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        Log.d(TAG, "handleActionBaz() returned: ");
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    private BookInfo getNext() {
        ArrayList<BookInfo> mDataSet = BookDatabaseUtil.getInstance(getApplicationContext()).queryBookInfos();

        Set<BookInfo> queue = new TreeSet<>(new Comparator<BookInfo>() {
            @Override
            public int compare(BookInfo lhs, BookInfo rhs) {
                int result = 0;
                try {
                    long diff = DATE_FORMAT.parse(lhs.getBookAlarmTime()).getTime() - DATE_FORMAT.parse(rhs.getBookAlarmTime()).getTime();
                    if (diff > 0) return 1;
                    else if (diff < 0) return -1;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return result;
            }
        });

        for (BookInfo bookInfo :
                mDataSet) {
            try {
                if ((DATE_FORMAT.parse(bookInfo.getBookAlarmTime())).after(Calendar.getInstance().getTime())) {
                    queue.add(bookInfo);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (queue.iterator().hasNext()) {
            return queue.iterator().next();
        } else {
            return null;
        }
    }

    @Override
    public void onDestroy() {
        BookDatabaseUtil.destory();
        super.onDestroy();
    }
}