package ssu.sel.smartdiary.view;

import android.content.Context;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;

import ssu.sel.smartdiary.R;

/**
 * Created by hanter on 2016. 11. 10..
 */

public class MediaContextLoadingView extends RelativeLayout {
    protected TextView tvMediaContextLoading = null;

    public MediaContextLoadingView(Context context) {
        super(context);
        initView(context);
    }

    public MediaContextLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public MediaContextLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    protected void initView(Context context) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_media_context_loading, this, true);

        tvMediaContextLoading = (TextView) findViewById(R.id.tvMediaContextLoading);
    }

    public void setLoadingMessage(String msg) {
        tvMediaContextLoading.setText(msg);
    }
}
