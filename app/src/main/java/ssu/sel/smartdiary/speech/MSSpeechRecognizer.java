package ssu.sel.smartdiary.speech;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.microsoft.bing.speech.SpeechClientStatus;
import com.microsoft.projectoxford.speechrecognition.DataRecognitionClient;
import com.microsoft.projectoxford.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.projectoxford.speechrecognition.RecognitionResult;
import com.microsoft.projectoxford.speechrecognition.RecognitionStatus;
import com.microsoft.projectoxford.speechrecognition.SpeechRecognitionMode;
import com.microsoft.projectoxford.speechrecognition.SpeechRecognitionServiceFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * Created by hanter on 2016. 10. 15..
 */
public class MSSpeechRecognizer {
    private final static String PRIMARY_KEY = "af104e9ebb5f47a195751fb24680805a";
    private final static String SECONDARY_KEY = "3196a12b1ce94fa1b37369ea991e4f9c";

    private Activity activity;
    private ISpeechRecognitionServerEvents recogEvents;

    private OnRecognizeDoneListener listener = null;
    private SpeechResponseStatus speechResponseStatus
            = SpeechResponseStatus.NotReceived;

    private int recordedCount = 0;
    private int recognizingRecordCount;

    private DataRecognitionClient dataClient = null;

    public interface OnRecognizeDoneListener {
        void onRecognizeDone();
        void onPartialRecognizeDone(String text);
        void onPartialRecognize(String text);
        void onFail(String message);
    }
    public enum SpeechResponseStatus { NotReceived, OK, Failed, Timeout }


    //waitsecnods 200
    public MSSpeechRecognizer(Activity activity, OnRecognizeDoneListener l) {
        this.activity = activity;
        this.listener = l;

        recogEvents = new ISpeechRecognitionServerEvents() {
            @Override
            public void onPartialResponseReceived(final String s) {
//                Log.d("Recognizer", "Partial Response: " + s);
                listener.onPartialRecognize(s);
            }

            @Override
            public void onFinalResponseReceived(final RecognitionResult response) {
//                boolean isFinalDictationMessage =
//                        (response.RecognitionStatus == RecognitionStatus.EndOfDictation ||
//                                response.RecognitionStatus == RecognitionStatus.DictationEndSilenceTimeout);
//
//                boolean isFailed = response.RecognitionStatus == RecognitionStatus.InitialSilenceTimeout ||
//                        response.RecognitionStatus == RecognitionStatus.BabbleTimeout ||
//                        response.RecognitionStatus == RecognitionStatus.RecognitionError ||
//                        response.RecognitionStatus == RecognitionStatus.None;

                Log.d("Recognizer", "Final Response: " + response.RecognitionStatus);
//                if (response.Results != null) {
//                    Log.d("Recognizer", "Final Response Results:");
//                    for (int i = 0; i < response.Results.length; i++) {
//                        Log.d("Recognizer", " - " + response.Results[i].DisplayText);
//                    }
//                }

                //error
//                if (isFailed) {
//                    listener.onFail("Final Response Error: " + response.RecognitionStatus.toString());
//                    speechResponseStatus = SpeechResponseStatus.Failed;
//                }
                if (response.RecognitionStatus == RecognitionStatus.RecognitionSuccess) {
                    Log.d("Recognizer", "Partial Response Results:");
                    if (response.Results != null) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < response.Results.length; i++) {
                            if (i>0) sb.append('\n');
                            sb.append(response.Results[i].DisplayText);
                            Log.d("Recognizer", " - " + response.Results[i].DisplayText);
                        }
                        listener.onPartialRecognizeDone(sb.toString());
                    } else {
                        listener.onPartialRecognizeDone("");
                    }

                } else if (response.RecognitionStatus == RecognitionStatus.EndOfDictation ||
                        response.RecognitionStatus == RecognitionStatus.DictationEndSilenceTimeout) {
                    if (recognizingRecordCount >= recordedCount) {
                        Log.d("Recognizer", "All Audio Recognized. ["
                                + recognizingRecordCount + "/" + recordedCount + "]");
                        listener.onRecognizeDone();
                        speechResponseStatus = SpeechResponseStatus.OK;
                        dataClient = null;

                    } else {
                        recognizingRecordCount++;
                        Log.d("Recognizer", "Start Transmit Next Audio ["
                                + recognizingRecordCount + "/" + recordedCount + "]");
                        transmitAudio(recognizingRecordCount);
                    }

                } else {
                    listener.onFail("Final Response Error: " + response.RecognitionStatus.toString());
                    speechResponseStatus = SpeechResponseStatus.Failed;
                    dataClient = null;
                }
            }

            @Override
            public void onIntentReceived(String s) {
                Log.d("Recognizer", "Intent Received: " + s);
            }

            @Override
            public void onError(final int errorCode, final String response) {
                Log.d("Recognizer", "Error: [" + errorCode + "] " + response);
                listener.onFail("ErrorCode: " + errorCode
                        + "\nStatus: "
                        + SpeechClientStatus.fromInt(errorCode)
                        + "\nResponse Message: " + response);
            }

            @Override
            public void onAudioEvent(boolean recording) {}
        };
    }

    public void startRecognize() {
        dataClient = SpeechRecognitionServiceFactory.createDataClient(
                activity, SpeechRecognitionMode.LongDictation,
                "en-us", recogEvents, PRIMARY_KEY, SECONDARY_KEY);

        recordedCount = WavRecorder.getRecordedFileNum();
        Log.d("Recognizer", "# of Audio files: " + recordedCount);

        if (recordedCount < 1) {
            listener.onFail("There is no recorded files.");
        } else {
            recognizingRecordCount = 1;
            transmitAudio(recognizingRecordCount);
        }
    }

    public void cancelRecognize() {
        if (lastRecogReqTask != null)
            try { lastRecogReqTask.cancel(true); } catch (Exception e) {}
        if (dataClient != null) {
            try { dataClient.audioStop(); } catch (Exception e) {}
            try { dataClient.endAudio(); } catch (Exception e) {}
            dataClient = null;
        }
    }

    private RecognitionReqTask lastRecogReqTask = null;
    private void transmitAudio(int recordNum) {
//        Log.d("Recognizer", "Converting the record #" + recordNum);
        Log.d("Recognizer", "Converting the record [" + recordNum + "/" + recordedCount + "]");
        File recoredeFile = WavRecorder.getRecordedFile(recordNum);
        Log.d("Recognizer", "Transmit audio file \"" + recoredeFile + "\"");
        lastRecogReqTask = new RecognitionReqTask(dataClient,
                SpeechRecognitionMode.LongDictation,
                recoredeFile);
        try {
            lastRecogReqTask.execute().get(500, TimeUnit.SECONDS);
        } catch (Exception e) {
            lastRecogReqTask.cancel(true);
            speechResponseStatus = SpeechResponseStatus.Timeout;
        }
    }

    private class RecognitionReqTask extends AsyncTask<Void, Void, Void> {
        DataRecognitionClient dataClient;
        SpeechRecognitionMode mode;
        File recordfile;

        RecognitionReqTask(DataRecognitionClient dataClient,
                           SpeechRecognitionMode mode,
                           File recordfile) {
            this.dataClient = dataClient;
            this.mode = mode;
            this.recordfile = recordfile;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                InputStream fileStream
                        = new FileInputStream(WavRecorder.getRecordedFile(recognizingRecordCount));
                int bytesRead = 0;
                byte[] buffer = new byte[16384];

                do {
                    bytesRead = fileStream.read(buffer);
                    if (bytesRead > -1) {
                        dataClient.sendAudio(buffer, bytesRead);
                    }
                } while (bytesRead > 0);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            finally {
                dataClient.endAudio();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d("Recognizer", "send recognition request");
            lastRecogReqTask = null;
        }
    }
}
