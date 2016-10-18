//package ssu.sel.smartdiary.speech;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.media.AudioFormat;
//import android.media.AudioManager;
//import android.media.AudioRecord;
//import android.media.MediaPlayer;
//import android.media.MediaRecorder;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.Environment;
//import android.os.Handler;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
//import android.util.Log;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.microsoft.bing.speech.SpeechClientStatus;
//import com.microsoft.projectoxford.speechrecognition.DataRecognitionClient;
//import com.microsoft.projectoxford.speechrecognition.ISpeechRecognitionServerEvents;
//import com.microsoft.projectoxford.speechrecognition.RecognitionResult;
//import com.microsoft.projectoxford.speechrecognition.RecognitionStatus;
//import com.microsoft.projectoxford.speechrecognition.SpeechRecognitionMode;
//import com.microsoft.projectoxford.speechrecognition.SpeechRecognitionServiceFactory;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//import java.util.concurrent.TimeUnit;
//
//import ssu.sel.smartdiary.R;
//
//public class MainActivityMK extends AppCompatActivity implements ISpeechRecognitionServerEvents {
//    private static final String TAG = "MainActivityMK";
//
//    private static final int RECORDER_BPP = 16;
//    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
//    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
//    private static final int RECORDER_SAMPLERATE = 16000; // 8000, 11025, 16000, 22050, 44100
//    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
//    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
//
//    private Button btRecord;
//    private Button btPlay;
//    private Button btConvert;
//    private Button btClear;
//    private TextView tvIntrResult;
//    private EditText etResult;
//    private EditText etLog;
//
//    private Handler mHandler;
//    private SharedPreferences sharedPref;
//    private static final String RECORD_CNT_KEY = "recordCnt";
//
//    private AudioRecord recorder;
//    private int bufferSize;
//    private Thread recordingThread;
//    private boolean isRecording = false;
//    private String storageDir;
//    private String speechFileName = "speech";
//    private String speechFileExtName = ".wav";
//    private int silenceCnt = 0;
//    private int recordCnt = 0;
//    private static final float MAX_REPORTABLE_AMP = 32767f;
//    private static final float MAX_REPORTABLE_DB = 90.3087f;
//    private static final double THRESHOLD_SILENCE = 40;
//    private static final double THRESHOLD_SILENCE_CNT = (RECORDER_SAMPLERATE / 8000) * 4;
//    private static final double THRESHOLD_SILENCE_SUM = THRESHOLD_SILENCE*THRESHOLD_SILENCE_CNT*0.9;
//
//    private boolean isPlaying = false;
//    private MediaPlayer player;
//
//    private int waitSeconds = 0;
//    private DataRecognitionClient dataClient = null;
//    public enum FinalResponseStatus { NotReceived, OK, Timeout }
//    private FinalResponseStatus isReceivedResponse = FinalResponseStatus.NotReceived;
//    private int convertingRecordNum;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
////        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
////        fab.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View view) {
////                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
////                        .setAction("Action", null).show();
////            }
////        });
//
//        mHandler = new Handler();
//        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
//        recordCnt = sharedPref.getInt(RECORD_CNT_KEY, 0);
//
//        storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
//
//        bufferSize = AudioRecord.getMinBufferSize(
//                RECORDER_SAMPLERATE,
//                RECORDER_CHANNELS,
//                RECORDER_AUDIO_ENCODING) * 3;
//
//        btRecord = (Button) findViewById(R.id.bt_record);
//        btPlay = (Button) findViewById(R.id.bt_play);
//        btConvert = (Button) findViewById(R.id.bt_convert);
//        btClear = (Button) findViewById(R.id.bt_clear);
//
//        tvIntrResult = (TextView) findViewById(R.id.tv_intr_result);
//        etResult = (EditText) findViewById(R.id.et_result);
//        etLog = (EditText) findViewById(R.id.et_log);
//
//        btRecord.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (!isRecording) {
//                    isRecording = true;
//                    btRecord.setText("Stop Recording");
//                    recordCnt = 0;
//                    startRecording();
//                } else {
//                    isRecording = false;
//                    btRecord.setText("Record");
//                    stopRecording();
//                    return;
//                }
//            }
//        });
//
//        btPlay.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (!isPlaying) {
//                    if (recordCnt <= 0) {
//                        Toast.makeText(MainActivityMK.this, "No records exist.", Toast.LENGTH_LONG).show();
//                        return;
//                    }
//                    isPlaying = true;
//                    btPlay.setText("Stop Playing");
//                    try {
//                        startPlaying(1);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    try {
//                        stopPlaying();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    btPlay.setText("Play");
//                    isPlaying = false;
//                }
//            }
//        });
//
//        btConvert.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                btConvert.setEnabled(false);
//                btConvert.setText("Converting...");
//                startConverting();
//            }
//        });
//
//        btClear.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                try {
//                    tvIntrResult.setText("");
//                    etResult.setText("");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        writeLog("# of existing records: " + recordCnt);
//    }
//
//    private void writeLog(final String log) {
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                etLog.append(log);
//                etLog.append("\n");
//            }
//        });
//        Log.d(TAG, log);
//    }
//
//    private String getPrimaryKey() {
//        return getString(R.string.primaryKey);
//    }
//
//    private String getSecondaryKey() {
//        return getString(R.string.secondaryKey);
//    }
//
//    private SpeechRecognitionMode getMode() {
//        return SpeechRecognitionMode.LongDictation;
//    }
//
//    private String getDefaultLocale() {
//        return "en-us";
//    }
//
//    private void startPlaying(final int recordNum) {
//        try {
//            if (!isPlaying) return;
//            File speechFile = new File(getAbsFilePath(recordNum));
//            if (speechFile.exists()) {
//                FileInputStream speechFileIS = new FileInputStream(speechFile);
//                player = new MediaPlayer();
//                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                player.setDataSource(speechFileIS.getFD());
//                player.prepare();
//                writeLog("Playing the record #" + recordNum);
//
//                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion(MediaPlayer mediaPlayer) {
//                        if (recordNum >= recordCnt) {
//                            isPlaying = false;
//                            btPlay.setText("Play");
//                        } else {
//                            startPlaying(recordNum+1);
//                        }
//                    }
//                });
//                if (player != null) player.start();
//            } else {
//                throw new FileNotFoundException();
//            }
//        } catch (FileNotFoundException e) {
//            Toast.makeText(MainActivityMK.this, "No recordings exist.", Toast.LENGTH_LONG).show();
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void stopPlaying() {
//        if (isPlaying) {
//            player.stop();
//        }
//    }
//
//    private String getAbsFilePath(int fileNum) {
//        File file = new File(storageDir, AUDIO_RECORDER_FOLDER);
//        if (!file.exists()) file.mkdirs();
//        return file.getAbsolutePath() + "/" + getFilename(fileNum);
//    }
//
//    private String getFilename(int fileNum) {
//        return speechFileName+fileNum+speechFileExtName;
//    }
//
//    private String getAbsTempFilePath() {
//        File file = new File(storageDir, AUDIO_RECORDER_FOLDER);
//        if (!file.exists()) file.mkdirs();
//        File tempFile = new File(file.getAbsolutePath(), AUDIO_RECORDER_TEMP_FILE);
//        return tempFile.getAbsolutePath();
//    }
//
//    private void writeAudioDataToFile() {
//        byte data[] = new byte[bufferSize];
//        File tempFile = new File(getAbsTempFilePath());
//        if (tempFile.exists()) tempFile.delete();
//
//        FileOutputStream os = null;
//        try {
//            os = new FileOutputStream(tempFile);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        int read = 0;
//        if (null != os) {
//            silenceCnt = 0;
//            double levelSum = 0;
//            double startTime = System.currentTimeMillis();
//            boolean isFinishedForNext = false;
//            while(isRecording && !isFinishedForNext) {
//                read = recorder.read(data, 0, bufferSize);
//
//                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
//                    try {
//                        os.write(data);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                double level = calcAmplitude(data);
//                if (level <= THRESHOLD_SILENCE) {
//                    silenceCnt++;
//                    levelSum += level;
//                } else {
//                    silenceCnt = 0;
//                    levelSum = 0;
//                }
//
//                if (silenceCnt >= THRESHOLD_SILENCE_CNT) {
//                    if (levelSum <= THRESHOLD_SILENCE_SUM) {
//                        writeLog("Silence Detected. Level Sum: " + levelSum);
//                        double endTime = System.currentTimeMillis();
//                        if (endTime - startTime > 60 * 1000) {
//                            isFinishedForNext = true;
//                        }
//                    }
//                    silenceCnt = 0;
//                    levelSum = 0;
//                }
//            }
//
//            try {
//                os.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            if (isFinishedForNext) {
//                startNextRecording();
//            }
//        }
//    }
//
//    private void startRecording() {
//        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
//                RECORDER_SAMPLERATE,
//                RECORDER_CHANNELS,
//                RECORDER_AUDIO_ENCODING,
//                bufferSize);
//        int i = recorder.getState();
//        if (i==1) recorder.startRecording();
//
//        startRecordingTask();
//    }
//
//    private void startRecordingTask() {
//        recordCnt++;
//        writeLog("Start recording #"+recordCnt);
//        isRecording = true;
//        recordingThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                writeAudioDataToFile();
//            }
//        }, "AudioRecorder Thread");
//        recordingThread.start();
//    }
//
//    private void stopRecording() {
//        stopRecordingTask();
//
//        if (recorder != null){
//            int i = recorder.getState();
//            if (i==1) recorder.stop();
//            recorder.release();
//
//            recorder = null;
//            recordingThread = null;
//        }
//
//        sharedPref.edit().putInt(RECORD_CNT_KEY, recordCnt).commit();
//    }
//
//    private void stopRecordingTask() {
//        isRecording = false;
//        copyWaveFile(getAbsTempFilePath(), getAbsFilePath(recordCnt));
//        deleteTempFile();
//    }
//
//    private void startNextRecording() {
//        stopRecordingTask();
//        startRecordingTask();
//    }
//
//    private double calcAmplitude(byte[] data) {
//        int shortsSize = bufferSize / 2;
//        short[] shorts = new short[shortsSize];
//        ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
//
//        int sum = 0;
//        for (int i = 0; i < shortsSize; i++) {
//            sum += Math.abs(shorts[i]);
//        }
//
//        if (bufferSize > 0) {
//            return (float) (MAX_REPORTABLE_DB + (20 * Math.log10((sum / shortsSize) / MAX_REPORTABLE_AMP)));
//        }
//
//        return 0;
//    }
//
//    private void deleteTempFile() {
//        File file = new File(getAbsTempFilePath());
//        file.delete();
//    }
//
//    private void copyWaveFile(String inFilename,String outFilename){
//        FileInputStream in;
//        FileOutputStream out;
//        long totalAudioLen;
//        long totalDataLen;
//        long longSampleRate = RECORDER_SAMPLERATE;
//        int channels = 2;
//        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8;
//
//        byte[] data = new byte[bufferSize];
//
//        try {
//            in = new FileInputStream(inFilename);
//            out = new FileOutputStream(outFilename);
//            totalAudioLen = in.getChannel().size();
//            totalDataLen = totalAudioLen + 36;
//
//            writeWaveFileHeader(out, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate);
//
//            while(in.read(data) != -1) {
//                out.write(data);
//            }
//
//            in.close();
//            out.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void writeWaveFileHeader(
//            FileOutputStream out, long totalAudioLen,
//            long totalDataLen, long longSampleRate, int channels,
//            long byteRate) throws IOException {
//
//        byte[] header = new byte[44];
//
//        header[0] = 'R';  // RIFF/WAVE header
//        header[1] = 'I';
//        header[2] = 'F';
//        header[3] = 'F';
//        header[4] = (byte) (totalDataLen & 0xff);
//        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
//        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
//        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
//        header[8] = 'W';
//        header[9] = 'A';
//        header[10] = 'V';
//        header[11] = 'E';
//        header[12] = 'f';  // 'fmt ' chunk
//        header[13] = 'm';
//        header[14] = 't';
//        header[15] = ' ';
//        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
//        header[17] = 0;
//        header[18] = 0;
//        header[19] = 0;
//        header[20] = 1;  // format = 1
//        header[21] = 0;
//        header[22] = (byte) channels;
//        header[23] = 0;
//        header[24] = (byte) (longSampleRate & 0xff);
//        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
//        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
//        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
//        header[28] = (byte) (byteRate & 0xff);
//        header[29] = (byte) ((byteRate >> 8) & 0xff);
//        header[30] = (byte) ((byteRate >> 16) & 0xff);
//        header[31] = (byte) ((byteRate >> 24) & 0xff);
//        header[32] = (byte) (2 * 16 / 8);  // block align
//        header[33] = 0;
//        header[34] = RECORDER_BPP;  // bits per sample
//        header[35] = 0;
//        header[36] = 'd';
//        header[37] = 'a';
//        header[38] = 't';
//        header[39] = 'a';
//        header[40] = (byte) (totalAudioLen & 0xff);
//        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
//        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
//        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
//
//        out.write(header, 0, 44);
//    }
//
//    private void startConverting() {
//        waitSeconds = getMode() == SpeechRecognitionMode.ShortPhrase ? 20 : 200;
//
//        if (null == dataClient) {
//            dataClient = SpeechRecognitionServiceFactory.createDataClient(
//                    this,
//                    getMode(),
//                    getDefaultLocale(),
//                    this,
//                    getPrimaryKey(),
//                    getSecondaryKey());
//        }
//
//        convertingRecordNum = 1;
//        transmitAudio(convertingRecordNum);
//    }
//
//    private void transmitAudio(int recordNum) {
//        writeLog("Converting the record #" + recordNum);
//        RecognitionTask doDataReco = new RecognitionTask(dataClient, getMode(), getAbsFilePath(recordNum));
//        try {
//            doDataReco.execute().get(waitSeconds, TimeUnit.SECONDS);
//        } catch (Exception e) {
//            doDataReco.cancel(true);
//            isReceivedResponse = FinalResponseStatus.Timeout;
//        }
//    }
//
//    public void onFinalResponseReceived(final RecognitionResult response) {
//        boolean isFinalDicationMessage = getMode() == SpeechRecognitionMode.LongDictation &&
//                (response.RecognitionStatus == RecognitionStatus.EndOfDictation ||
//                        response.RecognitionStatus == RecognitionStatus.DictationEndSilenceTimeout);
//
//        boolean isFailed = response.RecognitionStatus == RecognitionStatus.InitialSilenceTimeout ||
//                response.RecognitionStatus == RecognitionStatus.BabbleTimeout ||
//                response.RecognitionStatus == RecognitionStatus.RecognitionError ||
//                response.RecognitionStatus == RecognitionStatus.None;
//
//        writeLine("[" + response.RecognitionStatus + "] ");
//
//        if (isFinalDicationMessage || isFailed) {
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    if (convertingRecordNum >= recordCnt) {
//                        btConvert.setEnabled(true);
//                        btConvert.setText("Convert");
//                    } else {
//                        convertingRecordNum++;
//                        transmitAudio(convertingRecordNum);
//                    }
//                }
//            });
//            isReceivedResponse = FinalResponseStatus.OK;
//        }
//
//        if (!isFinalDicationMessage) {
//            for (int i = 0; i < response.Results.length; i++) {
//                writeLine(response.Results[i].DisplayText);
//            }
//            writeLine("\n");
//        } else {
//            writeLine("\n");
//        }
//    }
//
//    public void onIntentReceived(final String payload) {
//    }
//
//    public void onPartialResponseReceived(final String response) {
//        tvIntrResult.setText(response);
//    }
//
//    public void onError(final int errorCode, final String response) {
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                btConvert.setEnabled(true);
//                btConvert.setText("Convert");
//            }
//        });
//        writeLine("Error code: " + SpeechClientStatus.fromInt(errorCode) + " " + errorCode);
//        writeLine("Error text: " + response);
//        writeLine("\n");
//    }
//
//    public void onAudioEvent(boolean recording) {
//    }
//
//    private void writeLine(final String text) {
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                etResult.append(text);
//            }
//        });
//    }
//
//    private class RecognitionTask extends AsyncTask<Void, Void, Void> {
//        DataRecognitionClient dataClient;
//        SpeechRecognitionMode recoMode;
//        String speechFilePath;
//
//        RecognitionTask(DataRecognitionClient dataClient, SpeechRecognitionMode recoMode, String speechFilePath) {
//            this.dataClient = dataClient;
//            this.recoMode = recoMode;
//            this.speechFilePath = speechFilePath;
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            try {
//                InputStream fileStream = new FileInputStream(new File(getAbsFilePath(convertingRecordNum)));
//                int bytesRead = 0;
//                byte[] buffer = new byte[1024];
//
//                do {
//                    bytesRead = fileStream.read(buffer);
//                    if (bytesRead > -1) {
//                        dataClient.sendAudio(buffer, bytesRead);
//                    }
//                } while (bytesRead > 0);
//            } catch (Throwable throwable) {
//                throwable.printStackTrace();
//            }
//            finally {
//                dataClient.endAudio();
//            }
//
//            return null;
//        }
//    }
//}
