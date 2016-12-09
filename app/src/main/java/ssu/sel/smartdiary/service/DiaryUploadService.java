package ssu.sel.smartdiary.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import ssu.sel.smartdiary.GlobalUtils;
import ssu.sel.smartdiary.R;
import ssu.sel.smartdiary.WriteDiaryActivity;
import ssu.sel.smartdiary.model.MediaContext;
import ssu.sel.smartdiary.model.UserProfile;
import ssu.sel.smartdiary.network.DiaryUploadRestConnector;
import ssu.sel.smartdiary.speech.WavRecorder;

/**
 * Created by hanter on 2016. 12. 5..
 */

public class DiaryUploadService extends IntentService {
    public static String EXTRA_NAME_DIARY_TITLE = "DIARY_TITLE";
    public static String EXTRA_NAME_RECORDED_FILE = "RECORDED_FILE";
    public static String EXTRA_NAME_RECORDED_TEMP_FILE = "TEMP_FILE";
    public static String EXTRA_NAME_MEDIA_CONTEXTS = "MEDIA_CONTEXTS";
    public static String EXTRA_NAME_REQUEST_JSON = "REQUEST_JSON";

    private static int notificationID = 1;

    private boolean isUploading = true;
    private boolean isCanceled = false;
    private boolean isFailed = false;

    private DiaryUploadRestConnector saveDiaryConnector = null;

    public DiaryUploadService() {
        super("DiaryUploadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String diaryTitle = (String)intent.getSerializableExtra(EXTRA_NAME_DIARY_TITLE);
        final File recordedFile = (File)intent.getSerializableExtra(EXTRA_NAME_RECORDED_FILE);
        final File tempFile = (File)intent.getSerializableExtra(EXTRA_NAME_RECORDED_TEMP_FILE);
        final ArrayList<MediaContext> mediaContextList = (ArrayList<MediaContext>)intent.getSerializableExtra(EXTRA_NAME_MEDIA_CONTEXTS);
        final String jsonString = (String)intent.getSerializableExtra(EXTRA_NAME_REQUEST_JSON);

        Log.d("DiaryUploadService", "onHandleIntent!");

        NotificationManager ntfManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        //Create Notification
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.iconmonstr_diary_icon_small_white);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.iconmonstr_diary_icon_medium_white));

        builder.setAutoCancel(false).setOngoing(true)
                .setContentTitle("Uploading Diary \"" + diaryTitle + "\" ...")
                .setContentText("Press to cancel uploading.");

        //Add click event
        Intent notifiIntent = new Intent(Intent.ACTION_VIEW);
        notifiIntent.putExtra(EXTRA_NAME_DIARY_TITLE, diaryTitle);
        Intent cancelIntent = new Intent(getApplicationContext(),
                UploadCancelDialogActivity.class);
        cancelIntent.putExtra("SERVICE_ID", notificationID);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0, cancelIntent, PendingIntent.FLAG_ONE_SHOT);
//        try {
//            pendingIntent.send();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        builder.setContentIntent(pendingIntent);

        //upload diary to server
        saveDiaryConnector = new DiaryUploadRestConnector("diary", "POST",
                new DiaryUploadRestConnector.OnConnectListener() {
                    @Override
                    public void onDone(JSONObject resJson) {
                        if (resJson != null) {
                            Log.d("WriteDiary - Json", resJson.toString());
                            try {
                                boolean success = resJson.getBoolean("create_diary");
                                if (success) {
                                    int diaryId = resJson.getInt("audio_diary_id");
                                    WavRecorder.removeRecordedTempFiles();

                                    //Copy the temp file to diary audio cache file
                                    if(tempFile != null) {
                                        try {
                                            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(diaryAudioFile));
                                            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(
                                                    GlobalUtils.getDiaryFile(
                                                            UserProfile.getUserProfile().getUserID(), diaryId)));

                                            byte[] buf = new byte [1024*10];
                                            int byteLen;
                                            while ((byteLen = bis.read(buf)) > 0) {
                                                bos.write(buf, 0 ,byteLen);
                                            }
                                            bis.close();
                                            bos.close();

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Log.d("WriteDiaryAcitivity", "Audio Copy Failed");
                                            try {
                                                GlobalUtils.getDiaryFile(
                                                        UserProfile.getUserProfile().getUserID(), diaryId)
                                                        .delete();
                                            } catch (Exception e2) {}
                                        }
                                    }

                                    if(mediaContextList.size() > 0) {
                                        for (MediaContext mediaContext : mediaContextList) {
                                            File mediaFile = mediaContext.getFile();
                                            try {
                                                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(mediaFile));
                                                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(
                                                        GlobalUtils.getDiaryMediaContext(
                                                                UserProfile.getUserProfile().getUserID(), diaryId, mediaFile.getName())));

                                                byte[] buf = new byte [1024*100];
                                                int byteLen;
                                                while ((byteLen = bis.read(buf)) > 0) {
                                                    bos.write(buf, 0 ,byteLen);
                                                }
                                                bis.close();
                                                bos.close();
                                            } catch (Exception e) {
                                                try {
                                                    GlobalUtils.getDiaryMediaContext(
                                                            UserProfile.getUserProfile().getUserID(), diaryId, mediaFile.getName())
                                                            .delete();
                                                } catch (Exception e2) {}
                                            }
                                        }
                                    }

                                    WriteDiaryActivity.this.finish();
                                } else {
                                    openAlertModal("Diary Create Failed.\n Please Retry Again.");
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d("Main - Json", "Json parsing error");
                                openAlertModal("Json parsing error.", "Error");
                            }
                        } else {
                            Log.d("WriteDiary - Json", "No response");
                            openAlertModal("There is no reponse...");
                        }

                        showProgress(false);
                    }
                });

        //notify the notification
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        ntfManager.notify(notificationID, notification);

        while(isUploading) {
            try{
                Thread.sleep(200);
            } catch (Exception e){}
        }

        //Update Notification
        builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.iconmonstr_diary_icon_small_white);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.iconmonstr_diary_icon_medium_white));
        if (isCanceled) {
            if (isFailed) {
                builder.setContentTitle("Diary \"" + diaryTitle + "\"")
                        .setContentText("Uploading diary is canceled.");
            } else {
                builder.setContentTitle("Diary \"" + diaryTitle + "\"")
                        .setContentText("Uploading diary is failed.");
            }
        } else {
            builder.setContentTitle("Diary \"" + diaryTitle + "\"")
                    .setContentText("Uploading diary is done.");
        }

        notification = builder.build();
        ntfManager.cancel(notificationID);
        ntfManager.notify(notificationID+1, notification);
        notificationID += 2;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Add broadcast cancel receiver
        registerReceiver(cancelReceiver, cancelFilter);
        Log.d("DiaryUploadService", "onCreate!");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d("DiaryUploadService", "onStart!");
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(cancelReceiver);
        super.onDestroy();
        Log.d("DiaryUploadService", "onDestroy!");
    }

    private BroadcastReceiver cancelReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean stop = intent.getBooleanExtra("STOP", false);
            boolean fail = intent.getBooleanExtra("FAIL", false);
            int serviceId = intent.getIntExtra("SERVICE_ID", -1);

            if (notificationID == serviceId && stop) {
                isUploading = false;
                isCanceled = true;
                isFailed = fail;
            }
        }
    };
    private IntentFilter cancelFilter = new IntentFilter("ssu.sel.smartdiary.UPLOAD.CANCEL");
}
