package ssu.sel.smartdiary.model;

import android.content.Context;
import android.net.Uri;

import java.io.File;

import ssu.sel.smartdiary.GlobalUtils;

/**
 * Created by hanter on 2016. 11. 10..
 */

public class MediaContext {
    public static final int MEDIA_TYPE_IMAGE = 0;
    public static final int MEDIA_TYPE_VIDEO = 1;
    public static final int MEDIA_TYPE_AUDIO = 2;

    private Uri uri;
    private File file;
    private int mediaType;

    public MediaContext(Context context, File file, int mediaType) {
        this.file = file;
        this.uri = Uri.fromFile(file);
        this.mediaType = mediaType;
    }

    public MediaContext(Context context, Uri uri, int mediaType) {
        this.uri = uri;
//        this.file = new File(uri.getPath());
        this.file = new File(GlobalUtils.getRealPathFromUri(context, uri));
        this.mediaType = mediaType;
    }


    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }
}
