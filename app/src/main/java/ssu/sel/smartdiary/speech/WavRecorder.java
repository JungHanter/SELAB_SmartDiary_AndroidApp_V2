package ssu.sel.smartdiary.speech;

/**
 * Created by hanter on 2016. 10. 15..
 */
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by hanter on 16. 10. 12..
 */
public class WavRecorder {
    private final static int HEADER_WAVE_CHANNEL_MONO = 1;  //wav
    private final static int HEADER_SIZE = 0x2c;
    private final static int HEADER_RECORDER_BPP = 16;

    private final static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private final static int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private final static int RECORDER_SAMPLERATE = 16000;

    private final int BUFFER_SIZE;

    private AudioRecord recorder;
    private Thread recordingThread;
    private boolean isRecording;
    private boolean isFullRecording;
    private BufferedOutputStream fullFileWriteBos;
    private int audioLen = 0;

    private File recordFile = null;
    private File tempFile = new File(RECORDING_TEMP_FILE_DIR + "/temp.raw");
    private File fullTempFile = new File(RECORDING_TEMP_FILE_DIR + "/record.raw");

    public static final File RECORDING_TEMP_FILE_DIR =
            new File (Environment.getExternalStorageDirectory().getPath() + "/smartdiary/recording");
    public static final File RECORDED_TEMP_FILE_DIR =
            new File (Environment.getExternalStorageDirectory().getPath() + "/smartdiary/recorded");
    public static final File RECORDED_TEMP_PART_FILE_DIR =
            new File (Environment.getExternalStorageDirectory().getPath() + "/smartdiary/recorded/temp_parts");

    private static final float MAX_REPORTABLE_AMP = 32767f;
    private static final float MAX_REPORTABLE_DB = 90.3087f;
    private static final double THRESHOLD_SILENCE = 40;
    private static final double THRESHOLD_SILENCE_CNT = (RECORDER_SAMPLERATE / 8000) * 4;
    private static final double THRESHOLD_SILENCE_SUM = THRESHOLD_SILENCE*THRESHOLD_SILENCE_CNT*0.9;

    private int silenceCnt = 0;
    private int recordCnt = 0;

    public WavRecorder() {
        if (!RECORDING_TEMP_FILE_DIR.exists()) {
            RECORDING_TEMP_FILE_DIR.mkdirs();
        }
        if (!RECORDED_TEMP_FILE_DIR.exists()) {
            RECORDED_TEMP_FILE_DIR.mkdirs();
        }

        BUFFER_SIZE = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        isRecording = false;


        removeRecordedTempFiles();
    }

    public File getRecordFile() {
        return recordFile;
    }

    public boolean startRecord() {
        if(fullTempFile.exists()){
            fullTempFile.delete();
        }

        isFullRecording = true;
        try {
            fullFileWriteBos = new BufferedOutputStream(new FileOutputStream(fullTempFile));
            startRecording();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void stopRecord() {
        isFullRecording = false;
        stopRecording();
        try {
            fullFileWriteBos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        recordFile = new File(RECORDED_TEMP_FILE_DIR + "/"
                + System.currentTimeMillis() + ".wav");

        copyWaveFile(fullTempFile, recordFile);
        fullTempFile.delete();
    }

    private void startRecording() {
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING,
                BUFFER_SIZE);
        int i = recorder.getState();
        if (i==1) recorder.startRecording();

        startRecordingTask();
    }

    private void startRecordingTask() {
        recordCnt++;
        Log.d("WavRecorder", "Start recording #"+recordCnt);
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    private void stopRecording() {
        stopRecordingTask();
        if (recorder != null){
            int i = recorder.getState();
            if (i==1) recorder.stop();
            recorder.release();

            recorder = null;
            recordingThread = null;
        }

        //TODO delete
//        sharedPref.edit().putInt(RECORD_CNT_KEY, recordCnt).commit();
    }

    private void stopRecordingTask() {
        isRecording = false;
        copyWaveFile(tempFile, getRecordedFile(recordCnt));
        deleteTempFile();
    }

    private void startNextRecording() {
        stopRecordingTask();
        startRecordingTask();
    }

    private void writeAudioDataToFile() {
        byte[] buffer = new byte[BUFFER_SIZE];

        if (tempFile.exists()) tempFile.delete();

        BufferedOutputStream bos = null;

        try {
            bos = new BufferedOutputStream(new FileOutputStream(tempFile));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        int read = 0;
        if (null != bos) {
            silenceCnt = 0;
            double levelSum = 0;
            double startTime = System.currentTimeMillis();
            boolean isFinishedForNext = false;
            while(isRecording && !isFinishedForNext) {
                read = recorder.read(buffer, 0, BUFFER_SIZE);
                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    try {
                        bos.write(buffer);
                        fullFileWriteBos.write(buffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                double level = calcAmplitude(buffer);
                if (level <= THRESHOLD_SILENCE) {
                    silenceCnt++;
                    levelSum += level;
                } else {
                    silenceCnt = 0;
                    levelSum = 0;
                }

                if (silenceCnt >= THRESHOLD_SILENCE_CNT) {
                    if (levelSum <= THRESHOLD_SILENCE_SUM) {
                        Log.d("WavRecorder", "Silence Detected. Level Sum: " + levelSum);
                        double endTime = System.currentTimeMillis();
                        if (endTime - startTime > 60 * 1000) {
                            isFinishedForNext = true;
                        }
                    }
                    silenceCnt = 0;
                    levelSum = 0;
                }
            }

            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (isFinishedForNext) {
                startNextRecording();
            }
        }
    }

    private void deleteTempFile() {
        tempFile.delete();
    }

    private void copyWaveFile(File inFile, File outFile){
        FileInputStream in;
        FileOutputStream out;
        long totalAudioLen;
        long totalDataLen;
        long longSampleRate = RECORDER_SAMPLERATE;
        long byteRate = HEADER_RECORDER_BPP * RECORDER_SAMPLERATE *
                HEADER_WAVE_CHANNEL_MONO /8;

        byte[] data = new byte[BUFFER_SIZE];

        try {
            in = new FileInputStream(inFile);
            out = new FileOutputStream(outFile);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            writeWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, HEADER_WAVE_CHANNEL_MONO, byteRate);

            while(in.read(data) != -1) {
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8);  // block align
        header[33] = 0;
        header[34] = HEADER_RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }


    private double calcAmplitude(byte[] data) {
        int shortsSize = BUFFER_SIZE / 2;
        short[] shorts = new short[shortsSize];
        ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);

        int sum = 0;
        for (int i = 0; i < shortsSize; i++) {
            sum += Math.abs(shorts[i]);
        }

        if (BUFFER_SIZE > 0) {
            return (float) (MAX_REPORTABLE_DB + (20 * Math.log10((sum / shortsSize) / MAX_REPORTABLE_AMP)));
        }

        return 0;
    }

    public static File getRecordedFile(int count) {
        if (!RECORDED_TEMP_PART_FILE_DIR.exists()) {
            RECORDED_TEMP_PART_FILE_DIR.mkdirs();
        }
        return new File(RECORDED_TEMP_PART_FILE_DIR + "/" + count + ".wav");
    }

    public static int getRecordedFileNum() {
        if (!RECORDED_TEMP_PART_FILE_DIR.exists()) {
            String[] fileList = RECORDED_TEMP_PART_FILE_DIR.list();
            return fileList.length;
        } else return 0;
    }

    public static void removeRecordedTempFiles() {
        String[] fileList = RECORDED_TEMP_PART_FILE_DIR.list();
        if (fileList != null) {
            for (int i = 0; i < fileList.length; i++) {
                String filename = fileList[i];
                File f = new File(RECORDED_TEMP_PART_FILE_DIR.getPath() + "/" + filename);
                try {
                    if (f.exists()) {
                        f.delete();
                    }
                } catch (Exception e) {}
            }
        }
    }
}