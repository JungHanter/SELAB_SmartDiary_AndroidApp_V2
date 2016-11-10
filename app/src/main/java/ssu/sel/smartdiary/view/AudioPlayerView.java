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
import java.io.FileNotFoundException;
import java.io.IOException;

import ssu.sel.smartdiary.R;

/**
 * Created by hanter on 2016. 11. 10..
 */

public class AudioPlayerView extends RelativeLayout implements RemovableView {
    protected MediaPlayer mediaPlayer = null;

    protected TextView tvAudioPlayerDownloading = null;
    protected View layoutDiaryAudioPlayer = null;
    protected TextView tvAudioPlayerName = null;
    protected Button btnAudioPlayerPlay = null;
    protected Button btnAudioPlayerPause = null;
    protected Button btnAudioPlayerForward = null;
    protected Button btnAudioPlayerBackward = null;
    protected SeekBar progressAudioPlayer = null;
    protected TextView tvAudioPlayerNowLength = null;
    protected TextView tvAudioPlayerMaxLength = null;

    public AudioPlayerView(Context context) {
        super(context);
        initView(context);
    }

    public AudioPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public AudioPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    protected void initView(Context context) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_audio_player, this, true);

        tvAudioPlayerDownloading = (TextView) findViewById(R.id.tvAudioPlayerDownloading);
        layoutDiaryAudioPlayer = findViewById(R.id.layoutDiaryAudioPlayer);
        tvAudioPlayerName = (TextView) findViewById(R.id.tvAudioPlayerName);
        btnAudioPlayerPlay = (Button) findViewById(R.id.btnAudioPlayerPlay);
        btnAudioPlayerPause = (Button) findViewById(R.id.btnAudioPlayerPause);
        btnAudioPlayerForward = (Button) findViewById(R.id.btnAudioPlayerForward);
        btnAudioPlayerBackward = (Button) findViewById(R.id.btnAudioPlayerBackward);
        progressAudioPlayer = (SeekBar) findViewById(R.id.progressAudioPlayer);
        tvAudioPlayerNowLength = (TextView) findViewById(R.id.tvAudioPlayerNowLength);
        tvAudioPlayerMaxLength = (TextView) findViewById(R.id.tvAudioPlayerMaxLength);

        progressAudioPlayer.getProgressDrawable().setColorFilter(
                ContextCompat.getColor(context, R.color.pink_A200),
                PorterDuff.Mode.SRC_IN);
        progressAudioPlayer.setProgress(0);
        progressAudioPlayer.setMax(1);

        tvAudioPlayerDownloading.setText("Audio File is Loading");
        tvAudioPlayerDownloading.setVisibility(View.VISIBLE);
        layoutDiaryAudioPlayer.setVisibility(View.GONE);
    }

    public void setDiaryAudioName(Context context) {
        tvAudioPlayerName.setTextColor(ContextCompat.getColor(context, R.color.pink_A200));
    }

    public boolean setAudio(String name, final File audioFile) {
        return setAudio(name, audioFile, null);
    }

    public boolean setAudio(String name, final File audioFile, final OnLoadedListener onLoadedListener) {
        try {
            tvAudioPlayerName.setText(name);

            FileInputStream fis = new FileInputStream(audioFile);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(fis.getFD());
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.seekTo(0);
                    progressAudioPlayer.setProgress(0);
                    setAudioPlayerNowLengthText(0);
                    btnAudioPlayerPause.setVisibility(View.INVISIBLE);
                    btnAudioPlayerPlay.setVisibility(View.VISIBLE);
                }
            });
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    int duration = mp.getDuration();
                    if (duration < 1000) {
                        tvAudioPlayerMaxLength.setText("00:01");
                    } else {
                        int sec = duration / 1000;
                        int minute = sec / 60;
                        sec = sec % 60;
                        tvAudioPlayerMaxLength.setText(String.format("%02d:%02d", minute, sec));
                    }

                    View.OnClickListener audioControlClick = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            switch (v.getId()) {
                                case R.id.btnAudioPlayerPlay:
                                    play();
                                    return;

                                case R.id.btnAudioPlayerPause:
                                    pause();
                                    return;

                                case R.id.btnAudioPlayerForward:
                                    if(mediaPlayer != null) {
                                        int duration = mediaPlayer.getDuration();
                                        if (duration < 10000) {
                                            setAudioPlayerSeek(mediaPlayer.getCurrentPosition() + 1000);
                                        } else if (duration < 30000) {
                                            setAudioPlayerSeek(mediaPlayer.getCurrentPosition() + 3000);
                                        } else if (duration < 60000) {
                                            setAudioPlayerSeek(mediaPlayer.getCurrentPosition() + 5000);
                                        } else {
                                            setAudioPlayerSeek(mediaPlayer.getCurrentPosition() + 10000);
                                        }
                                    }
                                    return;

                                case R.id.btnAudioPlayerBackward:
                                    if(mediaPlayer != null) {
                                        int duration = mediaPlayer.getDuration();
                                        if (duration < 10000) {
                                            setAudioPlayerSeek(mediaPlayer.getCurrentPosition() - 1000);
                                        } else if (duration < 30000) {
                                            setAudioPlayerSeek(mediaPlayer.getCurrentPosition() - 3000);
                                        } else if (duration < 60000) {
                                            setAudioPlayerSeek(mediaPlayer.getCurrentPosition() - 5000);
                                        } else {
                                            setAudioPlayerSeek(mediaPlayer.getCurrentPosition() - 10000);
                                        }
                                    }
                                    return;
                            }
                        }
                    };
                    btnAudioPlayerPlay.setOnClickListener(audioControlClick);
                    btnAudioPlayerPause.setOnClickListener(audioControlClick);
                    btnAudioPlayerForward.setOnClickListener(audioControlClick);
                    btnAudioPlayerBackward.setOnClickListener(audioControlClick);

                    progressAudioPlayer.setMax(mp.getDuration());
                    progressAudioPlayer.setProgress(0);
                    setAudioPlayerNowLengthText(0);
                    progressAudioPlayer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (fromUser && mediaPlayer != null) {
                                mediaPlayer.seekTo(progress);
                                setAudioPlayerNowLengthText(progress);
                            }
                        }
                        public void onStartTrackingTouch(SeekBar seekBar) {}
                        public void onStopTrackingTouch(SeekBar seekBar) {}
                    });

                    audioCheckThread = new Thread() {
                        @Override
                        public void run() {
                            while(true) {
                                if (mediaPlayer == null) break;
                                if (mediaPlayer.isPlaying()) {
                                    Message msg = Message.obtain(audioCheckHandler);
                                    msg.what = 0;
                                    msg.arg1 = mediaPlayer.getCurrentPosition();
                                    audioCheckHandler.sendMessage(msg);
                                }
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException ie) {}
                            }
                        }
                    };
                    audioCheckThread.start();

                    btnAudioPlayerPlay.setVisibility(View.VISIBLE);
                    btnAudioPlayerPause.setVisibility(View.INVISIBLE);
                    tvAudioPlayerDownloading.setVisibility(View.GONE);
                    layoutDiaryAudioPlayer.setVisibility(View.VISIBLE);

                    if (onLoadedListener != null)
                        onLoadedListener.onLoaded();
                }
            });
            mediaPlayer.prepareAsync();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    protected Thread audioCheckThread = null;
    protected Handler audioCheckHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if(mediaPlayer != null) {
                        progressAudioPlayer.setProgress(msg.arg1);
                        setAudioPlayerNowLengthText(msg.arg1);
                    }
            }
        }
    };
    protected void setAudioPlayerSeek(int progress) {
        if (mediaPlayer != null) {
            int duration = mediaPlayer.getDuration();
            if (progress >= duration) progress = duration - 1;
            else if (progress < 0) progress = 0;
            mediaPlayer.seekTo(progress);
            progressAudioPlayer.setProgress(progress);
            setAudioPlayerNowLengthText(progress);
        }
    }
    protected void setAudioPlayerNowLengthText(int progress) {
        int sec = progress / 1000;
        int minute = sec / 60;
        sec = sec % 60;
        tvAudioPlayerNowLength.setText(String.format("%02d:%02d", minute, sec));
    }

    public void play() {
        if(mediaPlayer == null) return;
        mediaPlayer.start();
        btnAudioPlayerPlay.setVisibility(View.INVISIBLE);
        btnAudioPlayerPause.setVisibility(View.VISIBLE);
    }

    public void pause() {
        if(mediaPlayer == null) return;
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            progressAudioPlayer.setProgress(mediaPlayer.getCurrentPosition());
            setAudioPlayerNowLengthText(mediaPlayer.getCurrentPosition());
        }
        btnAudioPlayerPause.setVisibility(View.GONE);
        btnAudioPlayerPlay.setVisibility(View.VISIBLE);
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void setAudioFail(String msg) {
        tvAudioPlayerDownloading.setText(msg);
        tvAudioPlayerDownloading.setVisibility(View.VISIBLE);
        layoutDiaryAudioPlayer.setVisibility(View.GONE);
    }

    public interface OnLoadedListener {
        void onLoaded();
    }

    @Override
    public void remove() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        tvAudioPlayerDownloading.setText("Audio file is removed");
        tvAudioPlayerDownloading.setVisibility(View.VISIBLE);
        layoutDiaryAudioPlayer.setVisibility(View.GONE);
    }
}
