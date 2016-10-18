package ssu.sel.smartdiary;

import android.os.Environment;

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

    public static final DateFormat DIARY_DATE_FORMAT =
            new SimpleDateFormat("yyyy. MM. dd.", Locale.getDefault());
    public static final DateFormat DIARY_DATETIME_FORMAT =
            new SimpleDateFormat("yyyy. MM. dd. HH:mm:ss", Locale.getDefault());

    public static final File RECORDED_TEMP_FILE_DIR =
            new File (Environment.getExternalStorageDirectory().getPath() + "/smartdiary/tmp/");

    public static void removeTempFiles() {
        String[] fileList = RECORDED_TEMP_FILE_DIR.list();
        if (fileList != null) {
            for (int i = 0; i < fileList.length; i++) {
                String filename = fileList[i];
                File f = new File(RECORDED_TEMP_FILE_DIR.getPath() + "/" + filename);
                try {
                    if (f.exists()) {
                        f.delete();
                    }
                } catch (Exception e) {}
            }
        }
    }
}
