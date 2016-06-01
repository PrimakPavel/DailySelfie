package su.moy.chernihov.dailyselfie;

import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class PhotoListActivity extends ListActivity {

    private static final long INITIAL_ALARM_DELAY = 5 * 1000L;
    private static final long ALARM_DELAY_TWO_MINUTES = 2 * 60 * 1000L;
    protected static final long JITTER = 5000L;
    public static final String INTENT_FOR_OPEN_FILE = "file_for_open";
    private static final String TAG = "PhotoListActivityTag";

    private LabPhotoFiles mLabPhotoFiles;
    private ArrayList<File> mPhotoFilesList;
    private PhotoFileAdapter mAdapter;

    private AlarmManager mAlarmManager;
    private Intent mNotificationReceiverIntent, mLoggerReceiverIntent;
    private PendingIntent mNotificationReceiverPendingIntent, mLoggerReceiverPendingIntent;
    private boolean isStartAlarm = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_photo_list);


        configureAlarm();
        startRepeatingAlarm();

        configureListView();

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                File file = mAdapter.getItem(position);
                if (file.delete()) {
                    Toast.makeText(PhotoListActivity.this, getString(R.string.photo_list_activity_delete_message), Toast.LENGTH_SHORT).show();
                    mPhotoFilesList = mLabPhotoFiles.getPhotoFilesList();
                    mAdapter.notifyDataSetChanged();
                }

                return true;
            }
        });
        Log.d(TAG,"onCreate");

    }



    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        File photoFile = mAdapter.getItem(position);
        Intent intent = new Intent(PhotoListActivity.this, OpenFileActivity.class);
        intent.putExtra(INTENT_FOR_OPEN_FILE, photoFile);
        startActivity(intent);
        Log.d(TAG, "onListItemClick" + position);
    }


    @Override
    protected void onResume() {
        mPhotoFilesList = mLabPhotoFiles.getPhotoFilesList();
        mAdapter.notifyDataSetChanged();
        super.onResume();
        Log.d(TAG, "OnResume" + mPhotoFilesList.size());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "OnPause" + mPhotoFilesList.size());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_photo:
                Intent intent = new Intent(PhotoListActivity.this, CameraActivity.class);
                startActivity(intent);
                Log.d(TAG, "btn_photo click");
                break;
            case R.id.btn_disable_repeat_alarm:
                if (isStartAlarm) {
                    stopRepeatingAlarm();
                    item.setIcon(R.drawable.ic_alarm_add_white_36dp);
                }
                else {
                    startRepeatingAlarm();
                    item.setIcon(R.drawable.ic_alarm_off_white_36dp);
                }
                Log.d(TAG, "btn_disable_repeat_alarm click");
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return false;

    }


    private void configureListView() {
        // библиотека файлов.
        mLabPhotoFiles = LabPhotoFiles.getInstance();

        // получаю список всех файлов
        mPhotoFilesList = mLabPhotoFiles.getPhotoFilesList();

        // настраиваю адаптер, передаю в него список файлов
        mAdapter = new PhotoFileAdapter(getApplicationContext(), mPhotoFilesList);

        // устанавливаю адаптер
        setListAdapter(mAdapter);

    }

    private void configureAlarm(){
        // Get the AlarmManager Service
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // Create an Intent to broadcast to the AlarmNotificationReceiver
        mNotificationReceiverIntent = new Intent(PhotoListActivity.this,
                AlarmNotificationReceiver.class);

        // Create an PendingIntent that holds the NotificationReceiverIntent
        mNotificationReceiverPendingIntent = PendingIntent.getBroadcast(
                PhotoListActivity.this, 0, mNotificationReceiverIntent, 0);

        // Create an Intent to broadcast to the AlarmLoggerReceiver
        mLoggerReceiverIntent = new Intent(PhotoListActivity.this,
                AlarmLoggerReceiver.class);

        // Create PendingIntent that holds the mLoggerReceiverPendingIntent
        mLoggerReceiverPendingIntent = PendingIntent.getBroadcast(
                PhotoListActivity.this, 0, mLoggerReceiverIntent, 0);
    }

    private void startRepeatingAlarm() {
        // Set repeating alarm
        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + INITIAL_ALARM_DELAY,
                ALARM_DELAY_TWO_MINUTES,
                mNotificationReceiverPendingIntent);
        // Set repeating alarm to fire shortly after previous alarm
        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + INITIAL_ALARM_DELAY
                        + JITTER,
                ALARM_DELAY_TWO_MINUTES,
                mLoggerReceiverPendingIntent);
        isStartAlarm = true;

        // Show Toast message
        Toast.makeText(getApplicationContext(), getString(R.string.alarm_start),
                Toast.LENGTH_LONG).show();

    }

    private void stopRepeatingAlarm () {
        // Cancel all alarms using mNotificationReceiverPendingIntent
        mAlarmManager.cancel(mNotificationReceiverPendingIntent);
        mAlarmManager.cancel(mLoggerReceiverPendingIntent);
        isStartAlarm = false;
        Toast.makeText(getApplicationContext(), getString(R.string.alarm_stop),
                Toast.LENGTH_LONG).show();
    }

}
