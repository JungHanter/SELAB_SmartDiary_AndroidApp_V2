package ssu.sel.smartdiary;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by hanter on 16. 9. 29..
 */
public class GlobalUtils {
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        Locale.setDefault(new Locale("ko", "KR"));
    }

    public static final String SERVER_URL = "http://203.253.23.17:8000/api/";

    public static final DateFormat DIARY_DATE_FORMAT =
            new SimpleDateFormat("yyyy. MM. dd.", Locale.getDefault());
    public static final DateFormat DIARY_DATETIME_FORMAT =
            new SimpleDateFormat("yyyy. MM. dd. HH:mm", Locale.getDefault());
    public static final DateFormat DIARY_TIME_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.getDefault());

    public static final File AUDIO_DIARY_DIR =
            new File (Environment.getExternalStorageDirectory().getPath() + "/smartdiary/audio/");

    public static File getAudioDiaryFile(String userId, int audioDiaryId) {
        File userFolder = new File(AUDIO_DIARY_DIR.getPath() + "/" + userId + "/");
        if (!userFolder.exists()) userFolder.mkdirs();
        return new File( AUDIO_DIARY_DIR.getPath() + "/" + userId + "/" + audioDiaryId + ".wav");
    }
    public static boolean existsAudioDiaryFile(String userId, int audioDiaryId) {
        File audioFile = getAudioDiaryFile(userId, audioDiaryId);
        return audioFile.exists();
    }

    public static final File DIARY_DIR =
            new File (Environment.getExternalStorageDirectory().getPath() + "/smartdiary/.diary/");

    public static File getDiaryFolder(String userId, int audioDiaryId) {
        File diaryFolder = new File( DIARY_DIR.getPath() + "/" + userId + "/" + audioDiaryId + "/");
        if (!diaryFolder.exists()) diaryFolder.mkdirs();
        return diaryFolder;
    }

    public static File getDiaryFile(String userId, int audioDiaryId) {
        File diaryFolder = getDiaryFolder(userId, audioDiaryId);
        return new File( diaryFolder.getPath() + "/" + "record.wav");
    }

    public static File getDiaryMediaContext(String userId, int audioDiaryId, String fileName) {
        File diaryFolder = getDiaryFolder(userId, audioDiaryId);
        return new File( diaryFolder.getPath() + "/" + fileName);
    }

//    public static void removeTempFiles() {
//        String[] fileList = RECORDED_TEMP_FILE_DIR.list();
//        if (fileList != null) {
//            for (int i = 0; i < fileList.length; i++) {
//                String filename = fileList[i];
//                File f = new File(RECORDED_TEMP_FILE_DIR.getPath() + "/" + filename);
//                try {
//                    if (f.exists()) {
//                        f.delete();
//                    }
//                } catch (Exception e) {}
//            }
//        }
//    }

    public static float pixelToDp(Context context, int px) {
        return px / ((float)context.getResources().getDisplayMetrics().densityDpi
                / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static float dpToPixel(Context context, int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, context.getResources().getDisplayMetrics());
    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        String path = null;
        String[] proj = { MediaStore.MediaColumns.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            path = cursor.getString(column_index);
        }
        cursor.close();
        return path;
    }
}
